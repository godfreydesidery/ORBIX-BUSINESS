# ORBIX Business: audit log design

**Status:** design agreed on 5 October 2026, with the decisions in section 12. All four phases are implemented on branch `performance-improvements`.

## 1. Goal

Answer, from one place:
- **who signed in, when and from where**, including failed attempts;
- **who did which critical action, when, and on which record**;
- **for edits to money, prices, stock, discounts and access rights, what the value was before and after.**

The log is written by the server only. It cannot be edited or deleted through the application.

## 2. What exists today

- **No login history.** `CustomAuthenticationFilter.successfulAuthentication` issues tokens and records nothing; a comment there reads "can use this segment to register login activity". Failed logins are not handled, because there is no `unsuccessfulAuthentication` override.
- **Per-record stamps only.** 59 of the 81 tables carry a "who" field: `createdByUser` on 54 tables, plus `approvedByUser`, `discountApprovedByUser`, `rejectedByUser`, `canceledByUser`, `checkedInByUser`, `checkedOutByUser`, `confirmedByUser`, `closedByUser`, `archivedByUser`, and `collectedByUser` on payments. They have three gaps:
  - each holds only the **last** person to do that action on that record;
  - there is **no "updated by" anywhere**, so edits leave no trace;
  - deleted rows are gone (except removed vehicles and goods).
- **Stock movements** are logged in `shop_product_logs` and `restaurant_product_logs`: quantity in, quantity out and balance per movement.
- **Security and access changes leave no trace:** users, roles, privileges, passwords and activation.
- **`@EnableJpaAuditing` is on** in `MainApplication`, but no entity uses it.

## 3. Prerequisites (same update, before the audit log)

| # | Item | Why |
|---|---|---|
| P1 | **Token signing key.** Login tokens were signed with the hard-coded key `"secret"` in `CustomAuthenticationFilter`, `CustomAuthorizationFilter` and `UserResource` (two places); the `jwt.secret` property is not used. **Done:** the key is now read in one place, `security/JwtKey`, from the `ORBIX_JWT_SECRET` environment variable. While the variable is not set, the previous key still applies, so deploying changes nothing. Setting the variable rotates the key. | Anyone who knows the key can create a token for any user, and an audit trail is only as trustworthy as the identity behind it. Rotating the key signs everyone out once. |
| P2 | **One clock.** `DayServiceController.getTimeStamp()` returns `now + 3h`, while other code uses `now()`. Store audit times in UTC (`Instant`), and convert to local business time only on screen. | Audit times must be unambiguous and comparable. |
| P3 | **End of day becomes a POST.** `/days/end_day` is a `GET` that changes state. **Done:** it now accepts POST, and still accepts GET so that nothing calling it today breaks. | Browsers and proxies may repeat or prefetch GETs, and logging the action needs a deliberate request. |

## 4. Data model

One **new** table. No existing table changes.

### 4.1 `audit_logs`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT, PK, auto | |
| `occurred_at` | DATETIME(3), not null | UTC, set by the server |
| `category` | VARCHAR(20), not null | `AUTH`, `SECURITY`, `FINANCE`, `OPERATIONS`, `INVENTORY`, `PROCUREMENT`, `SALES`, `SETTINGS` |
| `action` | VARCHAR(60), not null | e.g. `LOGIN_SUCCESS`, `PAYMENT_CONFIRMED`, `DISCOUNT_APPROVED` (catalogue in section 6) |
| `outcome` | VARCHAR(10), not null | `SUCCESS` or `FAILURE` |
| `user_id` | BIGINT, null | Acting user. No foreign key, so log rows survive user deletion and failed logins with unknown names can be stored. |
| `username` | VARCHAR(100), null | Copied at the time of the action. For a failed login, it is the name that was typed. |
| `company_id`, `branch_id` | BIGINT, null | The user's company and branch at that time, for scoping and filtering |
| `entity_type` | VARCHAR(60), null | e.g. `Parking`, `BillReceivable`, `User`, `Role` |
| `entity_id` | VARCHAR(40), null | Primary key of the record acted on |
| `entity_ref` | VARCHAR(100), null | Human reference: parking/bond/storage no., bill no., LPO/GRN no., username |
| `summary` | VARCHAR(255), not null | One readable line, e.g. "Confirmed payment of 150,000 for 3 bills (cash, ref 123)" |
| `details` | TEXT, null | JSON with key values, and `before`/`after` for edits (section 7). Never passwords or tokens. |
| `ip_address` | VARCHAR(45), null | `X-Forwarded-For` (first address) when behind a proxy, otherwise the remote address |
| `user_agent` | VARCHAR(255), null | Browser, truncated |

**No foreign keys.** `user_id`, `company_id`, `branch_id` and `entity_id` are plain columns, by design:
- **The log outlives what it describes.** Users, branches and records can be deleted or archived, and their log entries must stay.
- **Failed logins** can name users that don't exist.
- **One column refers to many tables:** `entity_id` points at whichever table `entity_type` names, and a foreign key can't do that.
- **No locks or checks on the referenced tables** at insert time, so writing the log stays cheap.

The readable values (`username`, `entity_ref`, `summary`) are copied at the time of the action. Each entry therefore still reads correctly after the referenced rows change or disappear.

In the `AuditLog` entity these are plain `Long` / `String` fields, **not** `@ManyToOne` relations, so `ddl-auto=update` creates no foreign keys either. The audit screen looks up the current user and branch names separately when it displays an entry.

Indexes:
- `ix_audit_logs_occurred (occurred_at)`
- `ix_audit_logs_user_occurred (user_id, occurred_at)`
- `ix_audit_logs_action_occurred (action, occurred_at)`
- `ix_audit_logs_entity (entity_type, entity_id)`
- `ix_audit_logs_branch_occurred (branch_id, occurred_at)`

### 4.2 Login history

There is no separate table. Logins are `AUTH` rows in `audit_logs`. "Last login" and "failed attempts in the last hour" come from the action and user indexes.

### 4.3 Creating the table

The `AuditLog` entity is created automatically, with its indexes, by `ddl-auto=update` at start-up. It is a new table, so there is nothing to migrate and no script is needed.

## 5. How entries are written

### 5.1 One service

`AuditLogService` (implemented by `AuditLogServiceController`), in the new package `com.orbix.api.modules.audit`:

```java
// A critical action of the current user, saved in the background once the action's transaction (if any) commits
void recordAction(AuditLog auditLog);
// Sign-in events and refused requests, saved in the background
void recordAuth(String action, String outcome, String username, String reason, String ipAddress, String forwardedFor, String userAgent);
void recordAccessDenied(String username, String path, String ipAddress, String forwardedFor, String userAgent);
```

- **No part in the action's transaction:** the service is deliberately not transactional at class level. Each entry is saved in a transaction of its own, and any failure there is logged and never reaches the action.
- **Acting user:** the username is taken from the request. The user's record (id, company, branch) is looked up when the entry is saved, in the entry's own transaction, so a missing or renamed user can never affect the action.
- **IP address and browser:** the connection's remote address, which the client cannot fake. Any `X-Forwarded-For` header is kept in the details as `forwardedFor`, not trusted as the address.
- **Insert only:** `AuditLogRepository` has no update, and no endpoint changes or removes entries. The only delete is the clean-up of entries older than 90 days (section 9).

### 5.2 Logins (`AUTH`)

| Event | Where | Transaction |
|---|---|---|
| `LOGIN_SUCCESS` | `CustomAuthenticationFilter.successfulAuthentication` | Its own, since filters run outside service transactions |
| `LOGIN_FAILED` (reason: bad credentials, disabled, unknown user) | **New** `unsuccessfulAuthentication` override in the same filter; the response stays 401 | Its own |
| `TOKEN_REFRESHED` | `UserResource` `/token/refresh` | Its own |
| `LOGOUT` | **New** `POST /orbix-business-api/logout`, called by the frontend's `logout()` before it clears local storage | Its own |

Tokens are stateless, so a user who closes the browser without logging out leaves no `LOGOUT` row. The session then ends when the token expires, after 8 hours.

### 5.3 Critical actions

An `@Audited` annotation goes on the **resource** methods (the endpoints) that perform the actions in section 6:

```java
@Audited(category = "FINANCE", action = "PAYMENT_CONFIRMED", entityType = "BillReceivable", summary = "...")
public ResponseEntity<...> confirmBillPayment(...) { ... }
```

`AuditAspect` (Spring AOP, already on the classpath) records the entry **after the method returns successfully**:
- The aspect runs **around the action's transaction** (it is ordered before the transaction), so the method returns to it only once the transaction has committed. A rolled back action leaves no entry.
- The entry is then **written by the audit thread** (`auditExecutor`), in a transaction of its own. The request never waits on it, and never holds a second database connection for it: with open-in-view the request keeps its connection until it ends, so a write on the request itself would take another, and many audited changes at once could use up the pool.
- A failure to write the entry is logged and never fails the action, so the audit log cannot break business operations. The only gap: an entry is lost if the server stops between the action's commit and the entry's write. If ever more than 1000 entries are waiting, the next one is written on the request itself instead of being dropped.
- **Record id and reference** come from the returned DTO (`getId()`, `getNo()`) or from a named argument (`@Audited(entityId = "discountRequest.id")`).
- **Summary and key values** come from expressions in the annotation, e.g. `summary = "Confirmed payment of {totalAmount} for {billReceivableRequests.size} bill(s)"`.

**Sign-in events and access denials are written in the background** (`@Async`, on a small pool named `auditExecutor`), so signing in never waits on the audit log.

Failed critical actions are not logged (decision D5); validation failures such as "bills not cleared" would only add noise. The one exception is `ACCESS_DENIED`: a request refused by `@PreAuthorize` is recorded as `SECURITY` / `FAILURE` by the application's global exception handler, which already answers it, so the response is unchanged.

### 5.4 Before and after values

The same annotation handles edits, with no change to the service code:

```java
@Audited(category = "SETTINGS", action = "RECORD_UPDATED", entityType = "BondItemType", summary = "Updated bond item type {ref}",
		changeOf = BondItemType.class, changeId = "bondItemTypeRequest.id",
		changedFieldPattern = "(?i).*price.*", changedAction = "PRICE_CHANGED")
```

Some screens name the record by other keys than its id; the shop, restaurant and supplier product screens send the shop (or restaurant, or supplier) and the product. For these, `changeQuery` gives the ids of the matching records, with its parameters taken from `changeKeys`:

```java
@Audited(category = "INVENTORY", action = "RECORD_UPDATED", entityType = "ShopProduct", entityRef = "result.productCode",
		summary = "Updated shop product {ref}", changeOf = ShopProduct.class, changeId = "shopProductRequest.id",
		changeQuery = "select p.id from ShopProduct p where p.shop.id = ?1 and p.product.id = ?2",
		changeKeys = {"shopProductRequest.shopId", "shopProductRequest.productId"},
		changedFieldPattern = "(?i).*price.*", changedAction = "PRICE_CHANGED")
```

How it works:
- **Before the action**, the aspect reads the record named by `changeOf` and `changeId` (or, without an id, the records matching `changeQuery`), through a short-lived EntityManager of its own, before the action's transaction starts and while the request holds no database connection. It takes the record's own columns, plus the ids of the records it points to, from the JPA metamodel. When the keys match several records (a supplier's product in several branches), the one whose id the action returns is used.
- **After the action has committed**, it reads them again through the request's own EntityManager, which already holds the request's connection and has the changed record in memory (usually no query is run). It keeps only the columns that changed, as `before` and `after`, listed by column name; a record saved unchanged adds neither. If the record was deleted, everything it held is kept as `before`. Neither read takes part in the action's transaction, so neither can affect it; if the second read fails, the entry is still written, without the values.
- **Size:** the details are kept within the `TEXT` column (16,000 characters), so a very large action still gets its entry.
- **Price changes:** when a changed column matches `changedFieldPattern`, the entry is recorded as `changedAction` (for example `PRICE_CHANGED`) instead of `action`.
- **Reference:** `{ref}` in a summary is the record's number, username, code or name, or `#` and its id when it has none of these.
- **Safety:** reading the record before and after never throws, and the action's result and exceptions pass through unchanged.

## 6. Action catalogue

Endpoint paths are relative to `/orbix-business-api`.

### AUTH
`LOGIN_SUCCESS`, `LOGIN_FAILED`, `TOKEN_REFRESHED`, `LOGOUT`

### SECURITY (access rights)
| Action | Endpoint | Before/after |
|---|---|---|
| `USER_CREATED` / `USER_UPDATED` / `USER_DELETED` | `/users/create`, `/users/update`, `/users/delete` | Yes, for updates: name, type, company, branch; "password changed" as a flag only |
| `USER_ACTIVATED` / `USER_DEACTIVATED` | `/users/activate`, `/users/deactivate` | |
| `ROLE_CREATED` / `ROLE_UPDATED` / `ROLE_DELETED` | `/roles/create`, `/roles/create_role`, `/roles/update`, `/roles/delete` | |
| `ROLE_ASSIGNED` | `/roles/addtouser` | Roles before/after |
| `PRIVILEGES_CHANGED` | `/privileges/addtorole` | Privileges added and removed |
| `ACCESS_DENIED` | Any `@PreAuthorize` refusal | |

### FINANCE
| Action | Endpoint | Before/after |
|---|---|---|
| `PAYMENT_CONFIRMED` | `/bill_receivables/confirm_bills_payment`, `/bills/confirm_bills_payment` | Details: total, pay code, ref no., bill ids |
| `BILL_CREATED` / `BILL_UPDATED` / `BILL_DELETED` | Parking, service, storage, bond item, maintenance and weigh bill endpoints: `*_bill_receivables/create_*`, `update_*`, `delete_*`, `create_*_custom_bill_receivable`, `/weigh_bills/add_bill`, `/weigh_bills/remove` | Yes, for updates: qty, price, discount, period |
| `DISCOUNT_REQUESTED` / `DISCOUNT_APPROVED` / `DISCOUNT_REJECTED` | `/discount_requests/create`, `/approve`, `/reject` | Details: bill, amounts, comments |
| `DAY_ENDED` | `/days/end_day` | Details: business date closed |
| `CURRENCY_RATES_CHANGED` | `/currency_conversions/create`, `/update`, `/currency-conversions/reset-rates` | Yes |

### OPERATIONS
| Action | Endpoint |
|---|---|
| `CHECKED_IN` / `CHECKED_OUT` | `check_in` and `check_out` under `/parkings/`, `/bond_items/`, `/storages/` and `/maintenances/` |
| `RECORD_MODIFIED` | `/parkings/modify`, `/bond_items/modify` (before/after) |
| `VEHICLE_REMOVED` / `GOODS_REMOVED` / `BOND_ITEM_ARCHIVED` | `/parkings/remove`, `/storages/remove`, `/bond_items/archive` |
| `GOODS_RELEASED` | `/storage_good_releases/create_storage_good_release` |
| `WEIGH_RECHECKED` | `/weighs/recheck` |
| `JOB_ISSUE_CLOSED` / `JOB_ISSUE_REMOVED` | `/maintenance_job_card_issues/close`, `/remove` |

### INVENTORY
| Action | Endpoint | Before/after |
|---|---|---|
| `STOCK_ADJUSTED` | `/shop_products/adjust_stock`; `/restaurant_products/adjust_stock`, `add_stock`, `deduct_stock`; `/restaurant_dineables/adjust_stock` | Stock before/after, reason |
| `PRICE_CHANGED` | `/services/update`, `/shop_products/update`, `/restaurant_products/update`, `/restaurant_dineables/update`, `/supplier_products/update`, `/bond_item_types/update`, `/vehicle_equipment_types/update`, `/good_types/update` (only when a price field changes) | Prices before/after |

### PROCUREMENT
`LPO_APPROVED`, `LPO_CANCELLED`, `LPO_ARCHIVED`, `GRN_APPROVED`, `GRN_CANCELLED`, `GRN_ARCHIVED` (approve, cancel and archive under `/lpos/` and `/grns/`)

### SALES
`SALES_ORDER_CONFIRMED` and `SALES_ORDER_CANCELLED` (confirm and cancel under `/shop_sales_orders/` and `/restaurant_sales_orders/`); `MACHINE_SERVICE_CONFIRMED` (`/machine-services/confirm`)

### SETTINGS (phase 4, decision D4)
`RECORD_CREATED`, `RECORD_UPDATED`, `RECORD_ACTIVATED`, `RECORD_DEACTIVATED`. `entityType` names the kind of record.
- **Covers:** companies, branches, shops, restaurants, warehouses, workshops, parking and bond zones, vehicle/equipment, bond item, good and maintenance issue types, units of measure, suppliers, service specialists, restaurant agents and badges.
- **Badges:** `BADGE_ASSIGNED` / `BADGE_UNASSIGNED`.
- **Logo:** `LOGO_CHANGED` (`/company_profile/save_logo`; the logo itself is never stored).
- **Catalogue records** (products, dineables, services, shop/restaurant/supplier products, restaurant dineables) use the same actions under the INVENTORY category.
- **Updates** carry before/after values. A change to a price column is recorded as `PRICE_CHANGED`.

**Not logged:** reads, reports, lists and searches.

## 7. Before and after values

`details` holds JSON, for example:

```json
{"before": {"price": 1500.0, "discount": 0.0}, "after": {"price": 1200.0, "discount": 0.0}, "reason": "..."}
```

Rules:
- record only the fields that changed;
- **never** passwords, password hashes, tokens or the company logo;
- amounts are numbers, not formatted text.

## 8. Viewing the log

- **New screen: "Audit log"**, under Identity & Access.
  - Paged on the server, like the other lists.
  - Filters: date range, user, category, action, outcome, branch and record reference.
  - A row expands to show the details and before/after values.
  - CSV export of the filtered result.
- **New screen: "Login history".** The same screen preset to `AUTH`, showing each user's last successful login and failed attempts.
- **Access:** a new privilege object `AUDIT` with `ACCESS`, `READ` and `UPDATE`, added to `security/Object_.java` so the start-up seeding creates `AUDIT-ACCESS`, `AUDIT-READ` and `AUDIT-UPDATE`. ROOT is granted `AUDIT-ACCESS` and `AUDIT-UPDATE`; a dedicated auditor role gets `AUDIT-READ` (D3). `AUDIT-UPDATE` turns recording on and off (D8).
- **Endpoints:**
  - `GET /audit_logs/get_page?from=&to=&user_id=&category=&action=&outcome=&branch_id=&search=&page=&size=` (read-only transaction);
  - `GET /audit_logs/get?id=`;
  - `GET /audit_logs/get_setting` (whether recording is on, and who last changed it).
  - These require `AUDIT-ACCESS`, `AUDIT-READ` or `AUDIT-UPDATE`.
  - `POST /audit_logs/enable_recording` and `POST /audit_logs/disable_recording` require `AUDIT-UPDATE`.
- **Times are shown in local business time** and stored in UTC (D7).

## 9. Protection and retention

- **No edits:** the application has no update path for `audit_logs`, and its only delete is the retention clean-up below.
- **Database account:** if the hosting allows separate accounts, the application's MySQL user gets only `INSERT, SELECT, DELETE` on `audit_logs` (D6); the delete is needed by the clean-up.
- **Turning recording off (D8):** recording is on by default. A user with `AUDIT-UPDATE` (ROOT has it) can turn it off and on again from the Audit Log screen, which asks for confirmation before turning it off. The setting is one row in the new table `audit_settings` (created automatically; no row means on) and applies to the whole system. While it is off, nothing new is recorded (no sign-ins, actions or access denials, and no extra reads of changed records); existing entries can still be viewed, and the retention clean-up still runs. Turning recording off or on is itself always recorded (`SECURITY` / `AUDIT_RECORDING_DISABLED` or `AUDIT_RECORDING_ENABLED`, with who and when), so the gap is explained. The setting is kept in memory after it is first read, so checking it costs no query; this assumes one application server, as today.
- **Retention:** entries are kept for **90 days** (D2), then deleted; there is no archive. `AuditLogCleanup` runs in the background, 10 minutes after start-up and then every 6 hours (not at a fixed night-time hour, so a server switched off at night still clears its log). It deletes in batches of 1,000, each in a short transaction of its own, using the index on `occurredAt`, and records each clearing as a `SECURITY` / `AUDIT_LOG_CLEARED` entry with the number of entries removed, so the gap is explained. Anything older than 90 days can no longer be traced, including login history.

## 10. Performance

- **Writes:** per critical action, after it commits and on the audit thread: one user look-up and one insert; for edits, one read of the record before the action (the read after it usually needs no query). Per sign-in event, the same look-up and insert in the background.
- **Reads:** only from the audit screens, which are paged and indexed.
- **Growth:** roughly logins plus critical actions per day, small next to bills and collections, and bounded by retention.

## 11. Rollout

Each phase is independent and can ship on its own.

| Phase | Content | Check before release |
|---|---|---|
| 1 | Prerequisites P1–P3; table, entity and service; login events; audit screens | Sign in, fail a sign-in, refresh and log out: rows appear with the right user, time and IP |
| 2 | SECURITY and FINANCE actions | Each action leaves one row; a failed action leaves none |
| 3 | OPERATIONS, INVENTORY, PROCUREMENT and SALES actions | Same |
| 4 | Before/after values for edits; SETTINGS actions | Old and new values match what changed |

## 12. Decisions (agreed 5 October 2026)

| # | Decision | Agreed |
|---|---|---|
| D1 | Prerequisites, including rotating the token key (everyone signs in again once) and making end of day a POST | Yes |
| D2 | Retention period | 90 days, then deleted (no archive) |
| D3 | Who may view the log | ROOT, plus a dedicated auditor role with `AUDIT-READ` |
| D4 | Log reference-data changes (SETTINGS) | Yes, in phase 4 |
| D5 | Record failed critical actions beyond logins and access denials | No |
| D6 | Restrict the database account to INSERT/SELECT on the audit table | Yes, if the hosting allows separate accounts |
| D7 | Time shown on screen | Local business time; stored in UTC |
| D8 | Allow the client to turn recording off | Yes: on by default, switched from the Audit Log screen with `AUDIT-UPDATE`; the switching itself is always recorded |
