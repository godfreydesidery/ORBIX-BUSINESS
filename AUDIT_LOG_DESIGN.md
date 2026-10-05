# ORBIX Business: audit log design

**Status:** design agreed on 5 October 2026, with the decisions in section 12. Not implemented yet; planned for the next update.

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
| P1 | **Token signing key.** Login tokens are signed with the hard-coded key `"secret"` in `CustomAuthenticationFilter`, `CustomAuthorizationFilter` and `UserResource` (two places); the `jwt.secret` property is not used. Move the key to an environment variable, read it in one place, and rotate it. | Anyone who knows the key can create a token for any user, and an audit trail is only as trustworthy as the identity behind it. Rotating the key signs everyone out once. |
| P2 | **One clock.** `DayServiceController.getTimeStamp()` returns `now + 3h`, while other code uses `now()`. Store audit times in UTC (`Instant`), and convert to local business time only on screen. | Audit times must be unambiguous and comparable. |
| P3 | **End of day becomes a POST.** `/days/end_day` is a `GET` that changes state. | Browsers and proxies may repeat or prefetch GETs, and logging the action needs a deliberate request. |

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

The same approach as the performance indexes:
- an `AuditLog` entity, picked up by `ddl-auto=update`;
- a SQL script under `Orbix-Business-API/api/api/sql/` that creates the table and its indexes before deployment.

## 5. How entries are written

### 5.1 One service

`AuditService`, in the new package `com.orbix.api.modules.audit`:

```java
void record(String category, String action, String entityType, Object entityId, String entityRef,
            String summary, Map<String, Object> details);          // SUCCESS, current user
void recordChange(String category, String action, String entityType, Object entityId, String entityRef,
            String summary, Map<String, Object> before, Map<String, Object> after);
void recordAuth(String action, String outcome, String username, User user, String reason);  // login events
```

- **Acting user:** taken from the request-scoped memo `UserServiceController.getCurrentUser(request)`, which already exists, so recording costs no extra user query.
- **IP address and browser:** taken from the current `HttpServletRequest`.
- **Insert only:** `AuditLogRepository` has no update or delete, and no endpoint changes or removes entries.

### 5.2 Logins (`AUTH`)

| Event | Where | Transaction |
|---|---|---|
| `LOGIN_SUCCESS` | `CustomAuthenticationFilter.successfulAuthentication` | Its own, since filters run outside service transactions |
| `LOGIN_FAILED` (reason: bad credentials, disabled, unknown user) | **New** `unsuccessfulAuthentication` override in the same filter; the response stays 401 | Its own |
| `TOKEN_REFRESHED` | `UserResource` `/token/refresh` | Its own |
| `LOGOUT` | **New** `POST /orbix-business-api/logout`, called by the frontend's `logout()` before it clears local storage | Its own |

Tokens are stateless, so a user who closes the browser without logging out leaves no `LOGOUT` row. The session then ends when the token expires, after 8 hours.

### 5.3 Critical actions

An `@Audited` annotation goes on the **service** methods (not the resources) that perform the actions in section 6:

```java
@Audited(category = "FINANCE", action = "PAYMENT_CONFIRMED", entity = "BillReceivable")
public BillReceivable confirmBillPayment(...) { ... }
```

`AuditAspect` (Spring AOP, already on the classpath) records the entry **after the method returns successfully**:
- It writes **inside the action's own transaction**. If the action is rolled back, its log entry is rolled back too, so the log never claims an action that did not happen.
- **Record id and reference** come from the returned DTO (`getId()`, `getNo()`) or from a named argument (`@Audited(idArg = "id")`).
- **Summary and key values** come from a small formatter per action, e.g. amount, pay code and number of bills for a payment.

Failed critical actions are not logged (decision D5); validation failures such as "bills not cleared" would only add noise. The one exception is `ACCESS_DENIED`: a 403 from `@PreAuthorize` is recorded by an `AccessDeniedHandler` as `SECURITY` / `FAILURE`.

### 5.4 Before and after values

For edits, the service method itself calls `auditService.recordChange(...)`, because only it knows the old values. It reads the fields before applying the change and records only those that changed. See section 7.

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
| `STOCK_ADJUSTED` | `/shop_products/adjust_stock`; `/restaurant_products/adjust_stock`, `add_stock`, `deduct_stock`; `/restaurant_dineables/adjust_stock`; `/supplier_products/adjust_stock` | Stock before/after, reason |
| `PRICE_CHANGED` | `/products/update`, `/services/update`, `/shop_products/update`, `/restaurant_products/update`, `/restaurant_dineables/update`, `/supplier_products/update`, `/bond_item_types/update`, `/vehicle_equipment_types/update`, `/parking_zones/update` (only when a price field changes) | Prices before/after |

### PROCUREMENT
`LPO_APPROVED`, `LPO_CANCELLED`, `LPO_ARCHIVED`, `GRN_APPROVED`, `GRN_CANCELLED`, `GRN_ARCHIVED` (approve, cancel and archive under `/lpos/` and `/grns/`)

### SALES
`SALES_ORDER_CONFIRMED` and `SALES_ORDER_CANCELLED` (confirm and cancel under `/shop_sales_orders/` and `/restaurant_sales_orders/`); `MACHINE_SERVICE_CONFIRMED` (`/machine-services/confirm`)

### SETTINGS (phase 4, decision D4)
Create, update, activate and deactivate of reference data: companies, branches, shops, restaurants, warehouses, workshops, zones, types, units, suppliers, service specialists, agents and badges; also `/company_profile/save_logo`.

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
- **Access:** a new privilege object `AUDIT` with `ACCESS` and `READ`, added to `security/Object_.java` so the start-up seeding creates `AUDIT-ACCESS` and `AUDIT-READ`. Granted to ROOT, and to a dedicated auditor role (D3).
- **Endpoints:**
  - `GET /audit_logs/get_page?from=&to=&user_id=&category=&action=&outcome=&branch_id=&search=&page=&size=` (read-only transaction);
  - `GET /audit_logs/get?id=`.
  - Both require `AUDIT-READ`.
- **Times are shown in local business time** and stored in UTC (D7).

## 9. Protection and retention

- **No edits:** the application has no update or delete path for `audit_logs`.
- **Database account:** if the hosting allows separate accounts, the application's MySQL user gets only `INSERT, SELECT` on `audit_logs` (D6). Archiving then runs under a separate administrative account.
- **Retention:** entries stay online for **2 years** (D2). After that, a scheduled job moves them to an archive table (`audit_logs_archive`), which stays readable for audits.

## 10. Performance

- **Writes:** one insert per critical action or login, inside the existing transaction, with no extra user query.
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
| D2 | Retention period online | 2 years, then archive |
| D3 | Who may view the log | ROOT, plus a dedicated auditor role with `AUDIT-READ` |
| D4 | Log reference-data changes (SETTINGS) | Yes, in phase 4 |
| D5 | Record failed critical actions beyond logins and access denials | No |
| D6 | Restrict the database account to INSERT/SELECT on the audit table | Yes, if the hosting allows separate accounts |
| D7 | Time shown on screen | Local business time; stored in UTC |
