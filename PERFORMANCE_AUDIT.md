# ORBIX-BUSINESS: Performance Audit and Remediation Plan

**Revision:** 2 (2026-10-05). Supersedes revision 1 (2026-09-12).
**Scope:** the whole system. That means `Orbix-Business-API` (Spring Boot 2.2.5, Hibernate 5.4.12, Java 11, MySQL) and `Orbix-Bussiness-Web` (Angular 18 standalone).
**Trigger:** clients report slow operations, especially discounts. Server resource use, and so the hosting bill, keeps growing.

---

## 0. Ground rules for this revision

| # | Rule | What it means here |
|---|---|---|
| R1 | **No schema changes, except adding indexes.** | No new or changed tables, columns, types, constraints or data migrations. Indexes are the only DDL this plan adds. Recommendations that move a field to `@Transient` or change a Java field type are dropped (see BE-03). |
| R2 | **Schema management stays as it is: `spring.jpa.hibernate.ddl-auto=update`.** | No Flyway, Liquibase or hand-run migration scripts. Indexes are declared on the entities with `@Table(indexes = @Index(...))`, and Hibernate's `update` creates any that are missing at startup (§4, BE-04). |
| R3 | **Slow queries may be rewritten,** as long as the structure stays the same. | JPQL or native rewrites, `exists` / `count` / `sum` queries, projections, batch fetching and join-fetching are all allowed. The rewritten query must return the same rows, in the same order, with the same response DTOs. |
| R4 | **Server-side pagination is in scope,** with the matching frontend changes. | It moved from revision 1's "excluded" appendix into the plan (BE-30 / FE-20). Rollout is coordinated so no screen ever sees a half-migrated contract. |
| R5 | **This document only recommends.** | No code was changed while producing it. Every snippet below shows the target state; none of it is applied. |

### Corrections to revision 1

Revision 1 contained some claims that turned out to be wrong when checked against the code and the built artefact. They are corrected in place below and summarised here.

| Rev-1 claim | Finding | Where corrected |
|---|---|---|
| The `Byte[] image` columns hold large images (about 1 MB each, about 20 MB of heap). | The fields have no `@Lob` and no length, so Hibernate maps them to `VARBINARY(255)`, which MySQL stores as `tinyblob`. They are also never written. Their cost is close to zero. The real BLOB cost is `Company.logo` (`@Lob`, `longblob`), which loads on every `User` load. | BE-03 |
| The fix for read paths is `@Transactional(readOnly = true)`. | All 133 `@Transactional` annotations (in 127 files) are **`javax.transaction.Transactional`**, which has **no** `readOnly` attribute. Read methods must switch to `org.springframework.transaction.annotation.Transactional`. | BE-02 |
| Configure HikariCP; the pool defaults to 10 connections. | The built jar ships **tomcat-jdbc 9.0.31** and **no HikariCP**. `spring.datasource.hikari.*` would be silently ignored. tomcat-jdbc defaults to `maxActive=100`, `initialSize=10`. | BE-14, Appendix B |
| `hibernate.jdbc.batch_size` batches inserts. | All 81 entities use `GenerationType.IDENTITY`, and Hibernate cannot batch IDENTITY inserts. Batching only helps updates and deletes. | BE-14 |
| Move to Flyway with `ddl-auto=validate`. | Rejected under R2. | BE-10 |
| Ship indexes as explicit DDL. | Rejected under R2. Use `@Table(indexes)` with `ddl-auto=update`. | BE-04 |
| `spring-boot-devtools` ships in the artefact. | The repackaged jar does **not** contain devtools (verified with `unzip -l`). It is only active if the API is started with `mvn spring-boot:run`. `hibernate-envers` **is** in the jar. | BE-16 |
| Server-side pagination is excluded. | Now in scope (R4). | BE-30, FE-20 |

---

## 1. Executive summary

The system's slowness and its growing server costs have the same cause: **the work done per request grows with the total amount of data ever stored**, not with what the user asked for. Tables that only grow (bills, parkings, bond items, storages, collections, discount requests) are scanned or walked row by row on routine screens. Every row loaded drags in a large eager object graph, every statement is logged at TRACE level, and none of the queried columns is indexed. So each day the same screens cost more CPU, memory, database I/O and disk than the day before, even with no extra users.

The discount screens show this most clearly (BE-18). To list the items that have a pending discount request, the API loads **every checked-in item in every branch**. It then loads **every bill each of them has ever had**, one query per item, and pulls an eager `BillReceivable` per bill. Only then does it filter by `discountStatus == "Requested"` **in Java**. With 300 vehicles parked for 60 days, that is roughly 18,000 bill rows and about 18,000+ SQL statements, each logged at TRACE, to show a list that usually has a handful of rows. One indexed query returns the same answer.

**Headline measurements (this revision):**

| Metric | Value | Healthy target |
|---|---|---|
| JPA relations mapped `EAGER` | **232** (vs 2 `LAZY`) | Near zero `EAGER` |
| Entities declaring an index | **0 of 81** | Every hot predicate |
| Repository query methods / indexes proposed | 245 methods across 81 repositories → **47 indexes** on 29 tables | — |
| Endpoints with pagination | **0** | Every unbounded list |
| `@Transactional` that can be read-only | **0 of 133**. All are `javax.transaction`, which has no `readOnly`. | All read paths |
| `userService.getUser*()` calls (each one a fresh query) | **255** (up to 6 per request) | 1 per request |
| SQL logging in the shipped config | `show-sql`, `format_sql`, `SQL=DEBUG`, **`type=TRACE`** | Off in production |
| `hibernate.default_batch_fetch_size` | unset (every eager association is 1 query per id) | ~100 |
| HTTP compression (API and Apache) | off | gzip on |
| Global script bundle | 4.67 MB (measured on the committed build) | < 300 KB |
| `*ngFor` with `trackBy` | **0 of 271** (`@for` blocks: 39 of 39 tracked) | All |
| Screens that hit the server on every keystroke | **16** | 0 (debounced) |
| `console.log` in source | **1,595** | 0 in production |

**Where the server resources go:**

| Resource | Main consumers | Fixed by |
|---|---|---|
| Database CPU and I/O | Per-row queries (BE-06, BE-18 to BE-23), full-table scans (BE-04), eager graphs (BE-01), whole-table lists (BE-05, BE-30) | Indexes, query rewrites, batch fetch, pagination |
| App server CPU | TRACE SQL logging (BE-08), dirty-checking on reads (BE-02), JSON serialisation of oversized payloads (BE-05) | Config, read-only transactions, pagination |
| App server memory | Eager graphs held in read-write persistence contexts (BE-01, BE-02), whole-table result sets (BE-05), `Company.logo` on every user load (BE-03) | Same as above |
| Disk | TRACE logs (BE-08), unbounded upload temp files (BE-17) | Config |
| Network | Uncompressed JSON and bundles (BE-09, FE-09), unpaged lists (BE-30) | Compression, pagination |

**Expected outcome.** Phases 1 to 3 change only configuration, indexes and query shapes; none changes a response. They should remove the large majority of statements on the hot screens and make their cost independent of history (estimate; confirm with §8). Phase 4 (pagination) bounds list payloads. The discount screens should go from thousands of statements per load to a handful.

---

## 2. Method and evidence

- **Static analysis** of the working tree on branch `feature` at `dae3ded`. Every finding cites a file and line or gives a count anyone can reproduce with `grep`.
- **The built artefact** `target/api-0.0.1-Orbix-Business.jar` was inspected for the libraries it actually ships.
- **Exhaustive passes:**
  - every repository method, mapped to its table, predicate columns and callers (index catalogue, Appendix C);
  - every service and resource method, checked for per-row queries, load-then-filter, duplicate lookups and whole-table loads;
  - every frontend screen, checked for its request sequences, reloads and list consumers.
- **Not done, and therefore not claimed:**
  - runtime profiling;
  - load tests;
  - `EXPLAIN` against production data;
  - reading the AWS or hosting bill.

  Query counts are formulas from the code and should be confirmed with the measurement plan in §8.

**Path conventions.**
- **Backend:** class names refer to files under `Orbix-Business-API/api/api/src/main/java/com/orbix/api/`.
- **Frontend:** component names refer to files under `Orbix-Bussiness-Web/src/app/pages/`.
- **Templates:** `hN` means line N of the component's `.html` template.

---

## 3. Classification

**Severity**

| Level | Meaning |
|---|---|
| **S1: Critical** | Cost grows with data volume. Already degrading, or will fail as tables grow. |
| **S2: High** | Large fixed cost on every request or page load. |
| **S3: Moderate** | Measurable waste. Adds to S1/S2 problems, but tolerable on its own. |
| **S4: Low** | Hygiene, latent risk, or developer time. |

**Behaviour tag**

| Tag | Meaning |
|---|---|
| **BP** | Behaviour-preserving. Identical output; only the cost changes. |
| **BP\*** | Behaviour-preserving given a stated precondition that has been checked. |
| **PG** | Approved contract change (pagination, R4). Backend and frontend change together. |
| **BC** | Behaviour change that needs a product decision. Recorded in Appendix E and **not** in the plan. |

**Category:** `PERSISTENCE` · `QUERY` · `INDEX` · `RUNTIME` · `TRANSPORT` · `CONFIG` · `BUNDLE` · `CLIENT-RUNTIME` · `CLIENT-NETWORK` · `HYGIENE`

---
## 4. Backend findings

Findings BE-01 to BE-17 keep their revision-1 IDs and are corrected where needed. BE-18 onward are new.

### BE-01: Universal `EAGER` fetching creates a self-referential object graph
**S1 · PERSISTENCE · BP**

**Evidence.** 232 relations are `FetchType.EAGER` and 2 are `LAZY`. The graph is cyclic and recursive:

```
DiscountRequest / ParkingBillReceivable / BondItem / …
  ├─ User × 1–4           EAGER   (createdBy, approvedBy, rejectedBy, …)
  │    ├─ roles             ManyToMany EAGER (SUBSELECT) → privileges EAGER, company EAGER
  │    ├─ company           EAGER ─┐  Company.logo  @Lob longblob
  │    │                           │  Company.branches ManyToMany EAGER
  │    └─ branch            EAGER  │
  ├─ Branch                 EAGER ─┤  Branch.company EAGER (cycle)
  │    ├─ parentBranch      EAGER  │  recursive
  │    ├─ childBranches     ManyToMany EAGER (SUBSELECT), recursive   ← Branch.java:100–102
  │    └─ createdByUser     EAGER  ← back into User
  └─ BillReceivable         EAGER → Branch …
```

Eager associations reached through a **JPQL or derived query** are not join-fetched. Hibernate issues one secondary `SELECT` per distinct id. Each bill row pulls its own `BillReceivable`, so "load the bills of X" really costs `1 + B` statements. `Branch.childBranches` (missing from revision 1) is a self-referencing eager collection, so loading any branch walks its whole subtree.

Eager `@OneToMany` detail collections are loaded for list screens whose mappers never read them:
- `Grn.grnDetails` (Grn.java:111)
- `Lpo.lpoDetails` (Lpo.java:114)
- `ShopSalesOrder` (:94)
- `RestaurantSalesOrder` (:104)
- `InvoiceReceivable` (:56)
- `Sale` (:54)
- `RestaurantSale` (:50)
- `MaintenanceJobCard` (:83)

**Fix (staged).**
1. **Now, config only:** `hibernate.default_batch_fetch_size` (BE-27). It turns the hidden per-row selects into `IN` batches with no code change.
2. **Per query:** `join fetch` on the specific associations each mapper dereferences (e.g. `select b from ParkingBillReceivable b join fetch b.billReceivable where b.parking = :p order by b.id`).
3. **Last, module by module:** switch `@ManyToOne`/`@OneToOne` to `LAZY`, with an `@EntityGraph` on the methods that need the association. Do `Branch.childBranches`, `Company.branches` and the detail collections first.

**Behaviour.** BP, provided every dereference is covered before step 3. Keep `open-in-view` at its default (`true`) until step 3 is complete. Golden-file response tests are required before step 3 (§8).

**Effort.** Steps 1–2: 3 days. Step 3: 5–8 days, can be split across people, shippable per module.

---

### BE-02: Read paths run in read-write transactions, and the annotation in use cannot be made read-only
**S1 · PERSISTENCE · BP**

**Evidence.**
- 133 `@Transactional` annotations are applied at class level on every `*ServiceController` and `*Resource`. All are `javax.transaction.Transactional` (imported in 127 files; 0 files import Spring's).
- `javax.transaction.Transactional` has **no `readOnly` attribute**, so revision 1's one-line fix does not compile as written.
- Every GET therefore:
  - keeps a loaded-state snapshot of every managed entity (about twice the memory);
  - runs a dirty-check over all of them at flush and before each auto-flushed query;
  - holds a read-write connection for the whole request.

**Fix.** On read methods only, add `@org.springframework.transaction.annotation.Transactional(readOnly = true)`, fully qualified or imported under an alias so the class-level `javax` annotation stays for writes. A method-level Spring annotation takes precedence for that method. With Spring 5.2 and Hibernate 5.4, read-only sets `FlushMode.MANUAL` and `session.setDefaultReadOnly(true)`, so there are no snapshots and no dirty-checking.

Add `useLocalSessionState=true` to the JDBC URL (Appendix B). Without it, Connector/J sends `SET SESSION TRANSACTION READ ONLY/WRITE` round trips on every read-only transaction.

**Behaviour.** BP for genuine reads. Audit each method for incidental writes first: some `get*` methods call `save()`. **Never** convert at class level.

**Effort.** 2 days.

---

### BE-03: BLOB loaded with every user (corrected)
**S2 · PERSISTENCE · BP\***

**Evidence (corrected).**
- **`Company.logo`** (`Company.java:76–77`, `@Lob byte[]`, so `longblob`) is the real cost. Every `getUser*()` call (255 sites) loads `User → company` eagerly and with it the logo. `/companies` loads a logo per row.
- **`SystemProfile.logo`** (`:66–67`) is the same, but the table has a single row.
- The **`Byte[] image`** fields on Parking (:83), VehicleEquipment (:57), BondItem (:96) and Maintenance (:86) have no `@Lob` or length. They map to `VARBINARY(255)`, which MySQL stores as `tinyblob` (at most 255 bytes), and are never written (every `setImage` call is commented out). The cost is negligible. Revision 1's "~20 MB heap per row" estimate was wrong.

**Fix (no schema change).**
- Make `Company.logo` lazy without changing the mapping: `@Basic(fetch = FetchType.LAZY)` **plus** Hibernate bytecode enhancement (`hibernate-enhance-maven-plugin` with `enableLazyInitialization`). Without enhancement the annotation is silently ignored.
- Alternatively, keep the mapping and read the logo only through a dedicated projection (`select c.logo from Company c where c.id = :id`). The eager `User → Company` path still loads it until BE-01 step 3, so enhancement is the effective fix.
- Leave the `image` fields as they are. Changing them gains nothing measurable.

**Behaviour.** BP\*. The precondition is that the logo is only read by the logo endpoint, as verified at `SystemProfileResource.java:44`. After enhancement, verify with SQL logging (in a dev profile) that `logo` has left the `users` and `companies` SELECTs.

**Effort.** 1 day including the build-plugin change.

---

### BE-04: No indexes on any queried column (rewritten for R1/R2)
**S1 · INDEX · BP**

**Evidence.**
- `@Index` appears 0 times across 81 entities.
- InnoDB indexes only primary keys, unique constraints and FK columns. The FK indexes exist because `ddl-auto=update` creates FK constraints.
- Every other predicate (`status`, `*_date_time` ranges, `chasis_no`, `service_bill_id`/`service_bill_name`, `discount_status`, `collection_date_time`, `no` on LPOs) is a full table scan.
- An exhaustive pass over all **245** repository methods gives **47 indexes on 29 tables**:
  - **23 P1:** hot lists, finance/dashboard reports, discount flow.
  - **18 P2:** secondary reports, procurement.
  - **6 P3:** optional, small tables.
- The full catalogue, with ready-to-paste annotations, is in **Appendix C**.

**The P1 set, in short:**

| Table | Index | Columns | Main beneficiary |
|---|---|---|---|
| discount_requests | ix_discount_requests_bill_id_name | service_bill_id, service_bill_name | every discount lookup (BE-18) |
| parking_bill_receivables | ix_pbr_discount_status_parking | discount_status, parking_id | discount landing screen (BE-18) |
| storage_bill_receivables | ix_sbr_discount_status_storage | discount_status, storage_id | same |
| bond_item_bill_receivables | ix_bibr_discount_status_bond_item | discount_status, bond_item_id | same |
| parkings | ix_parkings_status_checked_out | status, checked_out_date_time | all status lists, checkout windows |
| parkings | ix_parkings_checked_in_status | checked_in_date_time, status | dashboard totals, monthly stats |
| parkings | ix_parkings_chasis_no_status | chasis_no, status | duplicate check on every check-in |
| bond_items | ix_bond_items_status_checked_out, ix_bond_items_zone_status_checked_out, ix_bond_items_chasis_no | … | bond lists, check-in |
| storages | ix_storages_status_checked_out, ix_storages_warehouse_status_checked_out | … | storage lists |
| maintenances | ix_maintenances_status_checked_out | status, checked_out_date_time | maintenance lists |
| collections | ix_collections_collection_date_time | collection_date_time | **every** finance report |
| weighs | ix_weighs_created_date_time | created_date_time | `/weighs/recent` |
| vehicle_equipments | ix_vehicle_equipments_chasis_no_active | chasis_no, active | chassis lookups |
| sales, restaurant_sales | ix_*_created_date_time | created_date_time | sales reports |
| restaurant_sales_orders | ix_rso_restaurant_status_created | restaurant_id, status, created_date_time | POS pending list |
| shop_product_logs, restaurant_product_logs | ix_*_shop/restaurant_created | (shop/restaurant)_id, created_date_time | stock-log reports |
| machines | ix_machines_branch_status_created, ix_machines_workshop_created | … | service-bay lists |

**How to deliver (R2: through `ddl-auto=update`, no migration tool):**

```java
@Table(name = "discount_requests", indexes = {
    @Index(name = "ix_discount_requests_bill_id_name", columnList = "service_bill_id, service_bill_name")
})
```

- **Use physical snake_case column names** in `columnList` (`created_date_time`, not `createdDateTime`), the same way the existing `uniqueConstraints` do. With Spring's naming strategy, Hibernate 5.4 resolves `columnList` against database column names, and a camelCase name fails at startup with "database column not found".
- **Always set `name`.** `update` checks for an existing index by name and creates only the missing ones, so explicit names make it idempotent across restarts and environments.
- **Keep existing `uniqueConstraints`** in the same `@Table` (e.g. `restaurant_sales_orders`, `shop_sales_orders`; see Appendix C).
- **Startup cost.** On first boot after deployment, startup blocks while each `CREATE INDEX` runs. InnoDB builds indexes online (concurrent reads and writes continue), but the app will not serve until it finishes. Deploy P1 indexes in a low-traffic window, and allow extra time for the health check on `parkings`, `bond_items`, `storages` and `collections`.
- **Implicit FK indexes.** Where a composite index leads with an FK column, MySQL drops the now-redundant implicit FK index by itself. This is expected.
- **Verify:** `SHOW INDEX FROM <table>` after the first boot, then `EXPLAIN` on the queries listed in Appendix C.
- **Leading-wildcard searches:** `…ContainingIgnoreCase` compiles to `LIKE '%x%'` and cannot use a B-tree index. Bound these with pagination or limits (BE-30); see Appendix C §C.2.

**Behaviour.** BP. Indexes change query plans, not results.

**Effort.** 2 days (P1 plus P2, including `EXPLAIN` checks).

---

### BE-05: Unbounded `findAll()` on growing tables
**S1 · QUERY · BP (filter) / PG (pagination)**

**Evidence.**
- 37 `findAll()` call sites. The growing ones:

  | Call site | Endpoint |
  |---|---|
  | `ParkingServiceController:75` | `/parkings` |
  | `BondItemServiceController:74` | `/bond_items` |
  | `StorageServiceController:74` | `/storages` |
  | `MaintenanceServiceController:70` | `/maintenances` |
  | `VehicleEquipmentServiceController:70` | `/vehicle_equipments` |
  | `InvoiceReceivableServiceController:38` | `/invoice_receivables` |
  | `InvoiceReceivableServiceController:59` | all parkings, as an input to a query |
  | `UserServiceController:391` | `/users` |

- No list is filtered by branch or company (see Appendix E-1).
- The frontend paginates client-side at 10 rows, so the whole table is loaded, hydrated, serialised and transferred to show ten rows.

**Fix.**
- **Server-side pagination** (BE-30 / FE-20) for the PAGINATE class in Appendix D.
- Meanwhile, replace `findAll()` with the filtered query the caller actually needs where the result is provably identical (e.g. BE-21).

**Effort.** Covered under BE-30 and BE-21.

---

### BE-06: Per-row queries (N+1) in mappers and loops
**S1 · QUERY · BP**

**Evidence.** Complete inventory (excluding the discount, cleared-list, payment and sales paths, which have their own findings below):

| Location | Pattern | Statements |
|---|---|---|
| `parkingResponseDTOMapper` (ParkingServiceController:687–688) | 2 bill queries per CHECKED-OUT row | +2N |
| `bondItemResponseDTOMapper` (BondItemServiceController:676), `storageResponseDTOMapper` (StorageServiceController:546), `maintenanceResponseDTOMapper` (MaintenanceServiceController:535) | 1 bill query per CHECKED-OUT row | +N |
| `getAllRecentCheckedOutBondItemsByBondZone` (BondItem:160), `getAllRecentCheckedOutStoragesByWarehouse` (Storage:159), `getAllCheckedInMaintenancesWithClosedJobsAndMine` (Maintenance:134) | mapper runs per row because every row is checked out | 1 + N |
| `ParkingReportResource:184 → 213` | bills per parking in the report loop | 1 + N (+N·B) |
| `MaintenanceJobCardServiceController:146 → 147` | bills per issue, including issues dropped afterwards (:155) | 1 + I |
| `WeighBillReceivableResource:69 → 188` | `getNicknameByUserId` re-loads the full user graph per bill, although `createdByUser` is already loaded | 2 + N |
| `ProductServiceController:302 → 304`, `:332 → 334`; `DineableServiceController:298 → 300` | "imported?" lookup per catalogue row | 1 + N |
| `UserResource:567/571 → 573` (`/load_privilege_model`) | `findByName` per object × operation, plus reflection per call | O·P |
| `UserResource:442` (`/privileges/addtorole`) | 2 `findByName` per privilege, for removals and adds | 4P |
| `UserServiceController:267–270` (`saveUserWithDto`) | `findById` per role | R |

**Fix.** Batch-then-map: one `…In(parents)` query per child type, then `groupingBy` on the parent id, then map. Specific one-liners:
- Weigh: `bill.getCreatedByUser().getNickname()`. Zero queries.
- Product/dineable flags: `select sp.product.id from ShopProduct sp where sp.shop = :s`, collected into a `Set<Long>`.
- Privilege model: `select p.name from Privilege p` into a `HashSet`, with the reflection lists cached in static fields.
- Roles: `findAllById(ids)`.
- Chunk any `IN` list at 1,000 elements.

**Behaviour.** BP. Add `order by <alias>.id` to batched queries wherever the current code relies on implicit id order (`list.get(size-1)` for the "last bill").

**Effort.** 4 days.

---

### BE-07: User context re-resolved from the database on every reference
**S2 · PERSISTENCE · BP**

**Evidence.**
- `getUser` / `getUserId` / `getUserCompany` / `getUserBranch` each run `findByUsername` (UserServiceController:655–743). There are **255** call sites.
- The first call per request loads the full eager graph (BE-01/BE-03). Each later call is a fresh SQL plus an auto-flush dirty-check.
- Worst methods:

  | Method | User lookups |
  |---|---|
  | `VehicleEquipmentServiceController.createVehicleEquipment` (178, 182, 227, then `createParking` 315, 319, 396) | **6** |
  | `ParkingServiceController.removeVehicleEquipment` (1048, 1051, 1069, 1077) | 4 |
  | `checkOut` in Parking (850/853/882), Bond (890/894/934), Storage (694/697/727), Maintenance (436/439/456) | 3 each |
  | `create*` in Parking, Bond, Storage, Maintenance, Warehouse, GoodType, MaintenanceIssueType, ServiceSpecialist, JobCardIssue | 3 each |
  | `UserServiceController.saveRole` (360–362) | 3 |
  | `DiscountRequestServiceController.approve` (297 + 302 per branch) and `createDiscountRequest` (171 + 173) | 2 each |
  | `GrnServiceController.createGrnByLpoNo:210` and `approveGrn:366` | 1 per detail line, inside the loop |

- 63 sites call `companyRepository.findById(userService.getUserCompany(request).getId())`. The `findById` half is a cache hit; the `getUser*` half is not.

**Fix.** Resolve the user once per request and memoise it: a `request.setAttribute("orbix.user", …)` inside `getUser`, or a `@RequestScope` holder. Inside loops, hoist `getUser` above the loop.

**Behaviour.** BP. The user cannot change mid-request.

**Effort.** 1.5 days.

---

### BE-08: SQL logging at `DEBUG`/`TRACE` in the shipped configuration
**S1 · CONFIG · BP** (raised from S2: it multiplies every other finding)

**Evidence.** `application.properties`:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type=TRACE
```

- `type=TRACE` logs every bound parameter **and every extracted column of every row**, synchronously.
- With the per-row query volumes above (e.g. about 18,000 statements for one discount screen load), logging can take more time than the queries themselves.
- It is a direct driver of CPU and disk use on the server. If stdout goes to a file without rotation (`nohup`, a systemd journal with no cap), it also fills the disk.
- It writes customer data and parameter values into the logs.

**Fix.** Production values (Appendix B):
- `show-sql=false`
- `format_sql=false`
- `org.hibernate.SQL=WARN`
- `org.hibernate.type=WARN` (or remove the line)

Keep the verbose settings in a dev profile (HY-02). Check the server's log destination and rotation policy.

**Effort.** 1 hour. **Do this first.**

---

### BE-09: No HTTP response compression
**S2 · TRANSPORT · BP**

Unchanged from revision 1. No `server.compression.*` properties are set, and responses are large, repetitive JSON. Enable compression (Appendix B). This typically cuts transferred bytes by 70–85% on these payloads. 15 minutes.

---

### BE-10: `ddl-auto=update` is kept, with operating notes (rewritten for R2)
**S4 · CONFIG · BP**

**Decision.** Schema management stays on `spring.jpa.hibernate.ddl-auto=update` (R2). No migration tool is introduced.

**Operating notes for this plan:**
- `update` compares the live schema with 81 mappings at every boot. That is a fixed startup cost and is acceptable.
- `update` **adds** tables, columns, FK constraints and **named indexes**. It never drops or alters them. This is what makes the BE-04 delivery route safe: adding `@Index` annotations creates exactly those indexes and touches nothing else.
- Because `update` never drops anything, a mistaken index must be removed by hand (`DROP INDEX`). Review index names in code review.
- Keep the entity mappings as the single source of truth, so dev, staging and production stay identical.

**Effort.** None.

---

### BE-11: Lombok `@Data` on all 81 entities across a cyclic graph
**S2 · PERSISTENCE · BP\***

Unchanged from revision 1.
- `@Data` generates graph-walking `equals`/`hashCode`/`toString`.
- 94 `@EqualsAndHashCode.Exclude` markers exist against 232 relations.
- `MainApplication.java:277,288` calls `List.contains` inside nested loops.

**Fix:** use `@Getter`/`@Setter` with id-based equality and `@ToString.Exclude` on every relation. Audit value-equality sites first; `MainApplication:277` is one. 2–3 days.

---

### BE-12: Per-request allocation in the authorization filters
**S3 · RUNTIME · BP**

`CustomAuthorizationFilter.java` rebuilds work on every request:
- `Algorithm.HMAC256` and `JWT.require().build()` at lines 67–68;
- `new ObjectMapper()` at line 97;
- `printStackTrace` at line 90.

`CustomAuthenticationFilter.java:72,97` does the same on login. Elsewhere:
- `UserResource:371,379` creates `new ObjectMapper()` on token refresh.
- `MachineServiceController:151,186` calls `DateTimeFormatter.ofPattern` per row.
- `UserServiceController:67` uses `System.out.println`, and `:508,530` call `printStackTrace`.

**Fix:** hoist these into `static final` fields or an injected singleton, and use `@Slf4j`. 2 hours.

> Non-performance note: line 67 hard-codes the signing key `"secret"` (with `jwt.secret=javainuse` also committed). Raise a separate security ticket.

---

### BE-13: Reports load full entities to read a few scalars
**S3 · QUERY · BP**

**Evidence.** These report methods load complete entities, with the full eager graph, to read 3–6 scalar fields each:

| File | Lines |
|---|---|
| `ParkingReportResource` | 112/120, 167/175, 263 |
| `BondItemReportResource` | 73/81, 122 |
| `StorageReportResource` | 76/84, 125, 179 |

Other cases:
- `VehicleEquipmentServiceController:144` (`/vehicle_equipments/get_chasis_nos`) loads **2,000 full entities** to return strings.
- `:112` and `:133` load entity lists to read the last id.
- `RestaurantBadgeServiceController:50` loads a full entity graph to read `max(id)`.
- `UserResource:630` loads agents for their names.

**Fix.** Use interface or constructor projections. Examples:
- `select v.chasisNo from VehicleEquipment v where v.active = true` with `PageRequest.of(0, 2000)`.
- `select max(b.id) from RestaurantBadge b`.
- For the registration report: `select p.chasisNo, t.name, p.createdDateTime, u.nickname, p.hasKeys from Parking p join p.vehicleEquipmentType t join p.createdByUser u where …`.

`CollectionRepository` already shows the pattern. Response DTOs stay the same.

**Effort.** 2 days.

---

### BE-14: Connection pool and JDBC settings (corrected)
**S3 · CONFIG · BP**

**Evidence (corrected).**
- **The pool is tomcat-jdbc.** The jar contains `tomcat-jdbc-9.0.31.jar` and no HikariCP, because the POM uses `spring-data-jpa` plus `tomcat-jdbc` rather than `spring-boot-starter-data-jpa`.
  - Defaults: `maxActive=100`, `maxIdle=100`, `minIdle=10`, `initialSize=10`, `maxWait=30000`, no validation.
  - The risk is not starvation but the opposite: up to 100 concurrent heavy requests hitting the database at once, which multiplies database CPU during peaks.
- **`open-in-view` is on** by default, so each connection is held through JSON serialisation.
- **No prepared-statement cache** on the Connector/J URL.
- **All 81 entities use `GenerationType.IDENTITY`,** so Hibernate disables JDBC insert batching. `jdbc.batch_size` helps only updates and deletes.

**Fix (Appendix B).**
- Set `spring.datasource.tomcat.*`:
  - `max-active` sized to the database (start at about 2 × DB vCPU + spare, and keep it below MySQL `max_connections`);
  - `min-idle`, `max-wait`;
  - `test-on-borrow=true` with `validation-query=SELECT 1` and `validation-interval=30000`.
- Add JDBC URL flags: `cachePrepStmts`, `prepStmtCacheSize`, `prepStmtCacheSqlLimit`, `useServerPrepStmts`, `useLocalSessionState`, `rewriteBatchedStatements`.
- Set `hibernate.jdbc.batch_size=50` and `order_updates=true`, for updates only.
- Keep `open-in-view` on until BE-01 step 3 is done.

**Effort.** 0.5 day plus load verification.

---

### BE-15: Startup seeding is O(n²) with per-item queries
**S4 · RUNTIME · BP**

Unchanged. `MainApplication.java:184–298` runs `existsByName` per privilege permutation and nested `List.contains` checks. Load the names once into a `Set` and `saveAll` the difference. This costs deploy time, not user time. 0.5 day.

---

### BE-16: Unused dependencies (corrected)
**S4 · CONFIG · BP**

- `hibernate-envers` is in the jar, while `@Audited` appears 0 times. It registers listeners for no benefit, so remove it.
- `javafx-weaver-spring-boot-starter` is a JavaFX integration in a REST API. Remove it.
- `commons-fileupload` with `CommonsMultipartResolver` is superseded by Spring's native multipart support. Remove both.
- `spring-boot-devtools` is **not** in the repackaged jar (corrected). Mark it `<optional>true</optional>` anyway, so `mvn spring-boot:run` on a server never activates it.
- Spring Boot 2.2.5 is end-of-life. The upgrade is a separate project (Appendix E-3).

**Effort.** 1 day.

---

### BE-17: Unbounded multipart limits
**S4 · CONFIG · BP**

Unchanged:
- `max-file-size=-1`, `max-request-size=-1`, `max-swallow-size=-1`;
- the temp location is `/`.

Set the limits to 50 MB, matching `MainApplication.java:311`, and point the temp location at a dedicated directory. 15 minutes.

---
### BE-18: Discount workflow: per-row scans that grow with history
**S1 · QUERY + INDEX · BP**

This is the path clients reported. Every step of it is covered below.

**18.1 Discount landing screens: `get_all_with_discounts`**

| Endpoint | Method |
|---|---|
| `/parkings/get_all_with_discounts` | `ParkingServiceController:249–267` |
| `/storages/get_all_with_discounts` | `StorageServiceController:137–156` |
| `/bond_items/get_all_with_discounts` | `BondItemServiceController:138–156` |

```java
List<Parking> parkings = parkingRepository.findAllByStatusIn(["CHECKED-IN"]);   // every branch
for (Parking parking : parkings) {
    List<ParkingBillReceivable> pbrs = parkingBillReceivableRepository.findByParking(parking); // 1 query per item
    for (ParkingBillReceivable pbr : pbrs)                                                      // + 1 eager BillReceivable per bill
        if ("Requested".equals(pbr.getDiscountStatus())) { add(parking); break; }              // filter in Java
}
```

- **Cost:** `1 + N + N·B` statements, where N is checked-in items across all branches and B is bills per item. Daily billing makes B grow every day an item stays.
- **Scan:** `status` is unindexed, so the first query is a full scan.
- **Logging:** each statement is logged at TRACE (BE-08).
- **Weighbridge:** `WeighServiceController:74` is an unmapped stub that returns `null`, so it costs nothing.

**Fix.** Drive the query from the few bills that are actually "Requested", with one statement per screen:

```java
@Query("select distinct p from ParkingBillReceivable b join b.parking p " +
       "where b.discountStatus = 'Requested' and p.status in :statuses order by p.id")
List<Parking> findAllWithRequestedDiscount(@Param("statuses") List<String> statuses);
```

- Storage and bond get the same shape on `StorageBillReceivable.storage` and `BondItemBillReceivable.bondItem`.
- Index the bill tables on `(discount_status, <parent>_id)` (BE-04, Appendix C). This scans only the requested bills.
- `order by p.id` reproduces the order rows have today (implicit primary-key order).

**18.2 Discount list for one item: `GET /discount_requests` (`DiscountRequestServiceController.getRequests:54–93`)**

Today the method:
1. loads the parent with `findById`;
2. loads **all** its bills (eager graph each) just to collect their ids;
3. runs `IN (ids)` on `discount_requests`, which has no index on `service_bill_id` / `service_bill_name`, so it is a full scan;
4. filters "pending, or decided within 48 h" in Java.

**Fix.** One statement:

```java
@Query("select d from DiscountRequest d where d.serviceBillName = :name and d.serviceBillId in " +
       "(select b.id from ParkingBillReceivable b where b.parking.id = :serviceId) " +
       "and (d.status = :pending or d.approvedDateTime > :cutoff or d.rejectedDateTime > :cutoff) order by d.id")
```

- Write the same query for Storage and Bond.
- Compute `cutoff = LocalDateTime.now().minusHours(48)` once, exactly as the current code does.
- **Precondition (BP\*):** today an unknown `service_id` throws from `Optional.get()`. Keep that behaviour by checking `existsById` first and throwing the same exception.

**18.3 Single lookups (`get_discount`, `create`).**
- `findByServiceBillIdAndServiceBillName` is a full scan of `discount_requests` on every Request/View modal open and on every create (lines 111, 137, 185, 233). The `ix_discount_requests_bill_id_name` index fixes this.
- Note: `get_discount` ignores its `bill_amount` and `discount_amount` parameters. The frontend's differing amount formulas are therefore harmless.

**18.4 Approve / reject.**
- `approve` (284) resolves the user twice per branch (297+302, 319+324, 341+346), and `reject` (364) once.
- `createDiscountRequest` resolves it twice (e.g. 171+173).
- Use one memoised user per request (BE-07).
- Everything else in these methods is single-row and fine once the indexes exist.

**18.5 Frontend side.** See FE-14.

**Expected effect.** The landing screens go from `1 + N + N·B` statements to **1**, and the discount list from `2 + B` to **1**. Cost no longer depends on how long items have been stored.

**Effort.** 1.5 days (backend), plus the three indexes.

---

### BE-19: Loading every bill to answer a yes/no, "last one" or total question
**S1 · QUERY · BP\***

**Evidence.** Many write paths load **every bill an item has ever had** (with the eager graph) only to check whether one is unpaid, find the last one, or sum a quantity:

| Location | Question asked | Rewrite |
|---|---|---|
| `ParkingServiceController.checkOut:860,873`; `BondItemServiceController.checkOut:898`; `StorageServiceController.checkOut:704`; `MaintenanceServiceController.checkOut:446` | any UNPAID? last `endedAt`? | `existsBy<Parent>AndBillReceivable_PayStatus(p, UNPAID)`, plus `findFirstBy<Parent>OrderByIdDesc` or `select max(b.endedAt)`, whichever matches today's code exactly |
| `ParkingServiceController.removeVehicleEquipment:1054` | any PAID? | `existsByParkingAndBillReceivable_PayStatus(p, PAID)` |
| `ParkingServiceController.modifyParking:536`; `BondItemServiceController.modifyBondItem:733` | any bills? | `existsByParking` / `existsByBondItem` |
| `ParkingResource:159`, `BondItemResource:160`, `StorageResource:161` (`get_last_*_bill_date`) | last bill | `findFirstBy<Parent>OrderByIdDesc` |
| `ParkingBillReceivableServiceController:102`, `BondItemBillReceivableServiceController:185`, `StorageBillReceivableServiceController:79` (create bill) | empty? last? | `findFirstBy<Parent>OrderByIdDesc` |
| `ParkingServiceController.createParkingBillReceivable:936,972`; Bond `:1059,1090`; Storage `:781,817` | overlapping period? OPEN invoice? | `existsByParkingAndStartedAtBeforeAndEndedAtAfter(…)`; `findFirstByParkingAndInvoiceReceivable_StatusOrderByIdAsc(p, "OPEN")` |
| `BondItemServiceController.showBondItemCustomBillDetail:1163`; `StorageServiceController.showStorageCustomBillDetail:889`; `StorageBillReceivableServiceController:213,221` | total qty | `select coalesce(sum(b.qty), 0) from … where b.<parent> = :p` |
| `StorageGoodReleaseServiceController:50,58` (create) and `:112,120` (detail) | approved released qty; paid billed qty | two `SUM` queries filtered by status / pay status |

**Behaviour.** BP\*. The preconditions:
- "Last" must mean the same thing as today. The current code takes the last element in implicit id order, so use `order by id desc`. Where it uses max `endedAt`, use `max`.
- SQL `SUM` over `double` may differ from a Java loop in the last binary digit. If any of these totals is compared for exact equality, keep the Java sum over a **projection** (`select b.qty …`) instead.

**Effort.** 3 days.

---

### BE-20: "Cleared", "today checked-out" and "recent" lists run 2–4 queries per row
**S1 · QUERY · BP\***

**Evidence.**

| Method (endpoint) | Pattern | Statements |
|---|---|---|
| `ParkingServiceController.getAllCleared:101` (`/parkings/get_all_cleared`) | per checked-in parking: parking bills (113) and service bills (123), then filter `payStatus` in Java | 1 + 2N (+N·(B+S)) |
| `ParkingServiceController.getTodayCheckedOut:141` | same, then the mapper re-runs both (687/688) | 1 + 4N |
| `ParkingServiceController.getRecentCheckedOut:187` | same | 1 + 4N |
| `BondItemServiceController.getAllCleared:182` / `getTodayCheckedOut:213` | per row (194 / 234), plus the mapper (676) | 1 + N / 1 + 2N |
| `StorageServiceController.getAllCleared:179` / `getTodayCheckedOut:208` | per row (191 / 226), plus the mapper (546) | 1 + N / 1 + 2N |
| `MaintenanceServiceController.getAllCleared:155` / `getRecentCheckedOut:189` | per row (166 / 206), plus the mapper (535) | 1 + N / 1 + 2N |

**Fix.**
- One statement per list using `not exists`. For example, for parking "cleared":

  ```java
  select p from Parking p where p.status = 'CHECKED-IN'
    and not exists (select b from ParkingBillReceivable b where b.parking = p and b.billReceivable.payStatus <> :paid)
    and not exists (select s from ParkingServiceBillReceivable s where s.parking = p and s.billReceivable.payStatus <> :paid)
  order by p.id
  ```

- When the mapper needs the bills, load them **once** for the whole result with `findAllByParkingIn(page)` and `groupingBy`.

**Behaviour.** BP\*. **Before replacing each loop, encode its exact Java predicate.** Check how items with **no** bills are treated, which pay statuses count as "cleared", and whether service bills are included. `not exists` includes items with no bills, so add `and exists (…)` if the loop excludes them.

**Effort.** 3 days.

---

### BE-21: Invoice receivables load every parking ever
**S1 · QUERY · BP / PG**

**Evidence.** `InvoiceReceivableServiceController.getPendingParkingInvoiceReceivables:58–89` (`/invoice_receivables/get_pending_parking_invoice_receivables`):
1. `parkingRepository.findAll()` at line 59 loads every parking ever, with its eager graph.
2. `findAllByParkingIn(allParkings)` at line 60 builds a huge `IN` list. Past `max_allowed_packet` this **fails outright**.
3. `findByInvoiceReceivable` per invoice at line 74 re-fetches a link row that is already in memory.

`getAllInvoiceReceivables:37` (`/invoice_receivables`) does `findAll()` plus eager `invoiceReceivableDetails` (each with its `BillReceivable`).

**Fix.**
- Replace steps 1–3 with `select pir from ParkingInvoiceReceivable pir join fetch pir.parking join fetch pir.invoiceReceivable order by pir.id`, and use `pir` directly in the loop. This is equivalent because `parking_id` is non-null.
- Paginate both endpoints (BE-30).
- Restricting the list to "pending" statuses, as the method name and the code comment intend, **changes results** (BC, Appendix E-2).

**Effort.** 1 day.

---

### BE-22: Payment confirmation does 8 lookups per bill
**S2 · QUERY · BP**

**Evidence.** `BillReceivableServiceController.confirmBillPayment:87–180` (`/bill_receivables/confirm_bills_payment`, used by every billing screen's "Confirm Payment"). For each bill paid, it runs:
- `findById` (107);
- a save (118);
- **eight** `findByBillReceivable` probes, one per bill type (129, 134, 140, 146, 152, 158, 164, 170);
- an insert (176) and two saves (177, 179).

That is about `2 + 11·B` statements per payment.

**Fix.**
- `findAllById(ids)` once.
- For each of the 8 types, one `findAllByBillReceivableIn(bills)`, mapped by bill id (8 statements in total).
- Keep today's precedence where the last match wins (parking → … → machine).

**Effort.** 1 day.

---

### BE-23: Sales, GRN and service confirmation loops
**S2 · QUERY · BP**

| Location | Per-line work | Fix |
|---|---|---|
| `ShopSalesOrderServiceController.confirmShopSalesOrder:260` (loops 275, 312) | `findByProductAndShop`, save, `getUser`, log insert per line; then 2 BR inserts, insert, `getUser`, 2 inserts per line | User once; preload `ShopProduct` for all products in one `in` query; `saveAll` the logs |
| `SaleServiceController.createSale:60`, `RestaurantSaleServiceController.createRestaurantSale:57` | `saveAndFlush` **per detail line** | `save` plus one flush at the end |
| `RestaurantSalesOrderServiceController.confirmRestaurantSalesOrder:298` (loops 313, 330, 361) | per dineable: 2 lookups; per product: lookup, save, `getUser`, log | Preload `RestaurantDineableProduct` and `RestaurantProduct` maps; user once |
| `GrnServiceController.approveGrn:332` (loop 356) | `findByProductAndShop`, save, `getUser`, log per line | Same pattern |
| `GrnServiceController.createGrnByLpoNo:155` (loop 196) | `getUser` per line (210) | User once |
| `MachineServiceServiceController.confirm:87` (loop 93) | save, BR insert and update, `getUser` per service | User once |

**Behaviour.** BP. The same rows are written. Note that IDENTITY ids mean inserts still go one round trip each (BE-14). The gain comes from removing reads and flushes.

**Effort.** 2.5 days.

---

### BE-24: Document lists that grow with all history, or filter in Java
**S2 · QUERY · BP / PG**

| Location | Problem | Fix |
|---|---|---|
| `LpoServiceController.getAllVisibleLposByBranch:80` (`/lpos/get_all_visible_by_branch`) | loads **every APPROVED LPO ever** (89), keeps < 48 h in Java (92) | `… where l.branch = :b and (l.status in (:open) or (l.status = 'APPROVED' and l.approvedDateTime > :cutoff))`, index `lpos(branch_id, status, …)` |
| `LpoServiceController.getAllVisibleLposByShop:99` | same, for APPROVED and COMPLETED (114/117) | same shape |
| `GrnServiceController:81 / :96` (`get_all_visible_by_branch / _by_shop`) | APPROVED/COMPLETED forever, eager details per row | paginate (BE-30) and batch fetch |
| `getAllPendingGrns:69`, `getAllPendingLpos:68`, `ShopSalesOrderServiceController:77`, `RestaurantSalesOrderServiceController:85` | eager detail collections loaded per row, never read by the list mapper | batch fetch now; LAZY under BE-01 |
| `RestaurantAgentServiceController.getAvailableBadgeByRestaurantId:81` | two lists, set difference in Java | `select b from RestaurantBadge b where b.restaurant = :r and not exists (select a from RestaurantAgent a where a.restaurant = :r and a.restaurantBadge = b) order by b.id` |

**Effort.** 1.5 days.

---

### BE-25: Duplicate and dead queries (safe to delete)
**S2 · QUERY · BP**

| Location | What |
|---|---|
| `BondItemServiceController.archive:960–984` | loads and **sorts** all bills; the loop body is commented out |
| `BondItemBillReceivableServiceController:323–327` | loads bills to sum `billedQty`, which is never used |
| `StorageServiceController.checkOut:713`, `removeGoods:932` | `lastBillDate` / `billedQty` computed and never used |
| `getBillView` + `getUngeneratedBill`: Parking (409 → 434), Bond (393 → 467), Storage (300 → 325) | the same bill list loaded twice in one request; replace both with `select b.billReceivable.payStatus, sum(b.billReceivable.amount) … group by …` plus `findFirst…OrderByIdDesc` |
| `ParkingBillReceivableServiceController:447/459` | exception thrown inside `try` as control flow |
| `ParkingReportResource:79–80` | `countRegistered()` called twice |
| `VehicleEquipmentServiceController.createVehicleEquipment` | `existsByChasisNoAndStatusIn` run 3 times (164, 243, `createParking:305`); type looked up twice |
| `UserResource.getBranchAgentnames:628` | `getUser` result unused |

**Effort.** 1 day.

---

### BE-26: Report SQL that cannot use indexes, or joins one table too many
**S2 · QUERY · BP**

- **`YEAR()`/`MONTH()` on columns** in `getMonthlyStats` (`ParkingRepository:49–72`, `BondItemRepository:56–79`, `StorageRepository:53–76`) prevent index use. Rewrite as `col >= :yearStart and col < :nextYearStart`. Once BE-04 is in, the `*_checked_in_status` indexes serve it.
- **Unneeded hop through `bill_receivables`** in `CollectionRepository:158, 264, 302, 326, 350, 411, 445`. Join `pbr.bill_receivable_id = brc.bill_receivable_id` directly. Keep the join where `br` columns are read (`:509`, `BillReceivableCollectionRepository:15`).
- **`COALESCE(l.supplier_id) = s.id`** (`GrnRepository:39`) is a no-op wrapper. Remove it.
- **Optional-parameter `(:p is null or col = :p)` predicates** (`CollectionRepository:38, 402, 469, 500, 541`, `BillReceivableCollectionRepository:55`, `RestaurantProductLogRepository:26`, `RestaurantSalesOrderDetailRepository:33–35`) cannot use an index on `col`. The date range still drives them once `collections(collection_date_time)` exists, and that is enough. Splitting each into two queries is optional.
- **Every finance report** filters `collections.collection_date_time BETWEEN`. The single P1 index in BE-04 covers all 11 of them.

**Effort.** 1.5 days.

---

### BE-27: Batch fetching is not configured
**S1 · CONFIG · BP**

**Evidence.**
- `hibernate.default_batch_fetch_size` is unset, so every eager association and collection that is not join-fetched loads **one statement per id** (BE-01).
- This is the single biggest multiplier behind the `1 + B` costs in BE-06 and BE-18 to BE-24.

**Fix.** `spring.jpa.properties.hibernate.default_batch_fetch_size=100`. Hibernate then loads pending associations in `IN` batches of up to 100 ids.

**Behaviour.** BP. Same entities, fewer round trips. No code change.

**Effort.** 15 minutes, plus a before-and-after query count (§8).

---

### BE-28: Billing screen lists load one `BillReceivable` per bill
**S2 · QUERY · BP**

**Evidence.** These endpoints are opened on every billing screen. Each returns rows whose mapper reads `getBillReceivable()`, which costs `1 + B` statements:
- `ParkingBillReceivableServiceController:45, :60` (`/parking_bill_receivables/get_all_by_parking`, `/service_bill_receivables/get_all_by_parking`);
- the six `BillReceivableServiceController.getAllBy*` methods (191, 208, 221, 234, 247, 260).

**Fix.**
- Use `select b from ParkingBillReceivable b join fetch b.billReceivable where b.parking.id = :id order by b.id`, and equivalents for the other parent types.
- For the `getAllBy*` methods, use `select b.billReceivable from … where … order by b.id`.
- `getAllByMachine` (264 + 266) collapses into one query through `machineService.machine.id`.

**Effort.** 1 day.

---

### BE-29: Type-ahead searches are unbounded leading-wildcard scans
**S2 · QUERY · PG**

**Evidence.** These `…Containing[IgnoreCase]` methods compile to `UPPER(col) LIKE UPPER('%x%')`. They cannot use an index and return **every** match:

| Repository method | Endpoint |
|---|---|
| `VehicleEquipmentRepository:20` | `/vehicle_equipments/get_vehicle_equipments_chasis_no_containing` |
| `UserRepository:37` | `/users/load_users_like`; ORs three columns |
| `ProductRepository:12`, `DineableRepository:11`, `ServiceRepository:12`, `SupplierRepository:11` | catalogue type-aheads |
| `RestaurantDineableRepository:15`, `RestaurantProductRepository:22`, `ShopProductRepository:24`, `SupplierProductRepository:22` | type-aheads across a join |

The frontend calls them **on every keystroke** with no debounce (FE-16).

**Fix.**
- Add a `Pageable` parameter and request a fixed first page (e.g. 20). Spring Data then appends `LIMIT`.
- Together with FE-16 (debounce, minimum 2 characters, cancel stale requests), this bounds both how often the searches run and how much each one returns.
- Switching to prefix search (`StartingWith`), which can use an index, changes which items match. It is listed as BC (Appendix E-4).

**Effort.** 1 day (backend).

---

### BE-30: Server-side pagination (backend design)
**S1 · QUERY · PG** (approved under R4)

**Scope.** Only endpoints whose results grow without bound. The classification is in Appendix D:

| Class | Meaning | Action |
|---|---|---|
| **PAGINATE** | Grows with all history | Server paging |
| **FILTER-ENOUGH** | Naturally bounded: yard lists by status, per-parent bills, date windows | No paging needed once the query fixes above land; optional later for very large yards |
| **REFERENCE** | Small master data | Keep unpaged |
| **DO-NOT-PAGE** | Payment bill lists and reports whose totals and exports are computed from the full array on the client | Keep full results; see FE-20 |

**PAGINATE endpoints:**
- `/parkings`, `/bond_items`, `/storages`, `/maintenances`;
- `/vehicle_equipments`, `/vehicle_equipments/get_all_active`;
- `/invoice_receivables`, `/invoice_receivables/get_pending_parking_invoice_receivables`;
- `/grns/get_all_visible_by_branch`, `/grns/get_all_visible_by_shop`, `/grns/get_all_visible_by_restaurant`;
- `/users/load_users_like` and all `*_containing` type-aheads (limit only, BE-29);
- `/products`, `/dineables`, `/services`, `/suppliers`, once the catalogue exceeds about 1,000 rows.

**Contract.**
- Add **new** endpoints next to the existing ones (e.g. `GET /parkings/page?page=0&size=20&sort=id,desc&q=…`). Migrate each screen, then retire the old endpoint. No screen ever sees a half-changed response.
- Return a small, stable envelope DTO, **not** Spring's `PageImpl`, whose JSON shape is not a public contract:

  ```json
  { "content": [ …same row DTOs as today… ], "page": 0, "size": 20, "totalElements": 1234 }
  ```

- **Sort on the server.** Most screens reverse the array on the client to show newest first, so the default sort is `id,desc`. Row numbers become `page*size + i + 1` on the client.
- **Search must move to the server** (`q` parameter) for paged screens, because the client `searchFilter` can only see the loaded page. The server matches a declared list of columns per screen. Today's filter matches any field of the JSON row, so the declared list must be agreed per screen (note in FE-20).
- **Use `Page<T>`** where the UI shows a page count (ngx-pagination's server mode needs `totalItems`). Use `Slice<T>` where it does not, to skip the `COUNT(*)`. Index the filter columns so the count is cheap (BE-04).
- **Don't combine `join fetch` of a collection with paging.** Hibernate would page in memory (warning HHH000104). Rely on `default_batch_fetch_size` (BE-27) for associations on paged queries.

**Effort.** 4–5 days (backend), plus FE-20.

---
## 5. Frontend findings

FE-01 to FE-13 keep their revision-1 IDs. The counts were re-checked on 2026-10-05; bundle sizes were measured on the committed `dist/` build. FE-14 onward are new.

### FE-01: 4.67 MB global script bundle, with jQuery included three times
**S1 · BUNDLE · BP**

`angular.json` `scripts[]` produces `scripts-*.js` at **4,668.7 KB**. It contains:
- jQuery listed twice (lines 60 and 71), plus a third copy (v1.9.1) inside jquery-ui;
- datamaps **hires**;
- d3 v3;
- fullcalendar and moment;
- pdfmake with `vfs_fonts`;
- popper, bootstrap, jquery-knob, topojson, dropzone and select2.

Nothing in `scripts[]` is tree-shaken, and all of it loads on every page. The budgets (7 MB warning, 10 MB error) let this pass.

**Fix, in order:**
1. Delete the duplicate jQuery entry.
2. Use the non-hires datamaps build, or a dynamic import.
3. Drop pdfmake and vfs_fonts from `scripts[]` (they are already imported as modules).
4. Load fullcalendar, dropzone, select2 and knob with dynamic imports.
5. Tighten the budgets.

Verify every `window`-global consumer after each move. 3 days.

### FE-02: pdfmake and its fonts pulled into 62 routes
**S1 · BUNDLE · BP**

- 68 files reference pdfmake; 50 import `pdfmake/build/pdfmake`.
- `require('pdfmake/build/vfs_fonts.js')` appears **97 times in 62 files**, at module scope.
- The shared chunk is about 2.19 MB.

**Fix:** a dynamic `import()` inside the print handlers, so the cost is paid only when printing. 2 days.

### FE-03: `PreloadAllModules` defeats route-level code splitting
**S1 · BUNDLE · BP**

`app.config.ts:20` preloads all 133 lazy chunks (11.4 MB of JavaScript) after bootstrap. `env.js` points at plain-HTTP `http://51.20.119.45:8080`, so HTTP/1.1 limits the browser to 6 connections. Preload only the 3–4 most-used routes. 0.5 day.

### FE-04: The search filter JSON-stringifies every row, and ships a `debugger`
**S1 · CLIENT-RUNTIME · BP**

`custom-pipes/search-filter.ts` is used 56 times across 48 templates.
- Line 16 is a `debugger` statement. It freezes the app whenever DevTools is open.
- Line 18 runs `JSON.stringify(item).toLowerCase()` per row, per keystroke, with no debounce.

**Fix:**
- FE-04.1: delete line 16.
- FE-04.2: debounce the input, and cache each row's serialised lowercase string.

On paged screens the filter is replaced by server search (FE-20). 1 day.

### FE-05: Permission checks decode the JWT on every change-detection pass
**S1 · CLIENT-RUNTIME · BP**

- `grant()` is used 30 times across 11 templates (21 live uses in 9).
- Each call in `menu_items.ts:1122–1140` reads `localStorage`, parses it, and decodes the JWT.
- `auth.service.ts:215–245` decodes the JWT once **per privilege**.

**Fix:** decode once at login and keep a `Set<string>` of privileges. Keep the menu's current once-at-import timing (see Appendix E-5). 1 day.

### FE-06: No `OnPush`, and no `trackBy` on `*ngFor`
**S2 · CLIENT-RUNTIME · BP**

160 components, 0 `OnPush`. **271** `*ngFor` (196 outside comments), **0** with `trackBy`. The 39 newer `@for` blocks already use `track`, which is the pattern to follow. Lists are reassigned on every refresh, so every row's DOM is destroyed and rebuilt.

**Fix:**
- FE-06.1: `trackBy: trackById` everywhere (1.5 days).
- FE-06.2: `OnPush` per component, with a visual check of each screen (5+ days, last).

### FE-07: Sequential request waterfalls
**S2 · CLIENT-NETWORK · BP**

1,227 `await this.` and 0 `Promise.all`. Independent `ngOnInit` loads run one after another. Use `Promise.all` only where the calls are truly independent, and review each site. Screen-specific chains are in FE-19. 3 days.

### FE-08: `console.log` everywhere
**S2 · CLIENT-RUNTIME · BP**

1,595 calls in source (1,491 survive into the build). Most log whole API payloads, which DevTools then keeps in memory. Strip them in production builds. 0.5 day.

### FE-09: No compression or cache headers on static assets
**S2 · TRANSPORT · BP**

`src/.htaccess` only has rewrite rules. Add `mod_deflate` and long-lived `Cache-Control` for hashed assets, with `no-cache` for `index.html` (Appendix B). 1 hour.

### FE-10: Render-blocking third-party scripts in `index.html`
**S2 · TRANSPORT · BP**

Google Maps and the CKEditor 4.7 `full-all` build load synchronously on every page, including login. Lazy-load them in the components that use them, or at least add `defer`. 1 day.

### FE-11: Unoptimised image and font assets
**S3 · BUNDLE · BP**

3.9 MB of images and 1.5 MB of fonts are shipped, including a 1.18 MB Illustrator `.ai` source file and legacy font formats. Delete unreferenced assets, keep `woff2` only, and compress the JPEGs. 1 day.

### FE-12: `router.events` subscriptions never released
**S3 · CLIENT-RUNTIME · BP**

63 `.subscribe(` and 0 `ngOnDestroy`. `mail-list.component.ts:35` and `mail.component.ts:32` add a permanent handler on every visit. Use `takeUntilDestroyed()`. 2 hours.

### FE-13: Production optimisation partially disabled
**S3 · BUNDLE · BP**

`inlineCritical: false`, and the CLI cache is disabled. Re-enable both and check the screens visually. 0.5 day.

---

### FE-14: Discount flows (frontend)
**S2 · CLIENT-NETWORK · BP**

**Request sequences today:**

| User action | Requests | Problems |
|---|---|---|
| Open the parking / storage / bond discount screen | GET `…/get_all_with_discounts` (`parking-discounts.ts:192`, `storage-discounts.ts:175`, `bond-discounts.ts:197`) | the backend cost is in BE-18 |
| Click "Discounts" on a row | navigation, then GET `/discount_requests` (`discounts.ts:193`) | the list is set to `[]` first (191), so the table flickers |
| Open Approve/Reject | GET `/discount_requests/get?id` (`discounts.ts:350`) | **redundant:** the row already has `comments`. Its `.catch` reloads `/parking_bill_receivables/get_all_by_parking?parking_id=null` (363), which is dead code. |
| Approve / Reject | POST `approve` (380) / `reject` (408), then a **full list reload** (387 / 414) | the row could be updated in place. Errors are swallowed (approve logs only; reject's catch is empty). |
| Billing screen: Request/View modal | GET bill (`vehicle-equipment-billing.ts:184`), **then** GET `get_discount` (200 → 322), in sequence | the first GET is redundant (the row holds the same fields). The modal shows the previous row's values until both return. `discountReason`/`discountComments` are never reset. |
| Billing screen: submit request | POST `/discount_requests/create` (292), then reload the bill list on success (297) **and** on error (304) | patch the row's `discountStatus` instead |
| Billing screen: open or refresh | GET bills, plus GET service bills (175, 555), which **are never rendered** (the template block is commented out) | delete the service-bill call |

The same pattern repeats in `good-billing` (143→161→315, 284→289/296), `weight-billing` (same lines) and `bond-item-billing` (148→166→319, 288→293/300, and init 138→139 awaited in sequence).

**Fix.**
- Open modals from the row data. Call `get_discount` only when `discountStatus` is set.
- Patch rows from mutation responses.
- Reload only on success.
- Delete the unrendered service-bill calls.
- Show errors.

**Effort.** 1.5 days.

---

### FE-15: A full list reload after every action
**S2 · CLIENT-NETWORK · BP**

**Evidence.** Nearly every mutation re-fetches the whole list, often in both `.then` **and** `.catch`, and usually after clearing the array (so the table flickers). Mutation responses are already typed as the entity.

| Area | Call sites (component.ts lines) |
|---|---|
| Billing detail screens | vehicle-equipment-billing 238, 245, 255, 262, 297, 304, 617–619; good-billing and weight-billing 185, 231, 238, 248, 255, 289, 296, 607–608; bond-item-billing 190, 236, 243, 253, 260, 293, 300, 611–612; maintenance-vehicle-equipment-billing 190, 197, 207, 214, 494; machine-service-billing 317 |
| Checkout from list screens (runs on success **and** failure) | storage-billing 524, bond-billing 676, bond-discounts 570, storage-discounts 539, weigh-billing 524 |
| Registers | release-vehicle-equipment 333, 355, 389, 424, 494, 524 (these reload `get_all_cleared` while the screen opened `get_recent_checked_out`, so the dataset also changes); vehicle-register 308, 330, 432, 467, 502, 541, 571, 720; maintenance 313, 335, 369, 405, 444, 474; maintenance-verify 306, 328, 362, 398, 437, 467; select-bond-zone 429, 469, 790, 799, 829, 839, 1160; select-warehouse 320, 360, 563, 572, 602, 612; select-workshop 876, 1078, 1087, 1117, 1127; weighbridge 298, 330 |
| Job cards (reload by POSTing `create_maintenance_job_card_and_mine`) | my-jobs 682, 715, 754, 950; my-closed-jobs 681, 714, 753, 949; maintenance 696, 731, 806; maintenance-verify 684, 717, 756, 951 |
| Document editors (`this.get(id)` after each line change) | lpo 295, 318, 361, 530, 545, 566, 589, 613, 636; grn 108, 269, 292, 321, 490, 505, 526, 549, 573, 596; shop-lpo, shop-grn, shop-sales-order, restaurant-sales-order (about 8 each); weighbridge 427, 478, 511, 650, 665, 686, 709, 733, 756 (each triggers 2 more sequential GETs) |
| Master-data CRUD (about 25 screens) | branch, company, currency-conversion, restaurant, shop, workshop, bond-item-type, bond-zone, role, user, dineable, product, service, maintenance-issue-type, service-specialist, parking-zone, vehicle-and-equipment-type, supplier, supplier-price-list, good-type, warehouse, restaurant-agent, restaurant-badge, restaurant-dineable-product, supplier-product screens |
| Stock and import screens | shop/restaurant product and dineable stock-status screens; import-product, import-dineable and import-restaurant-product reload a second list that is **never rendered** |

**Fix.**
- Update or remove the affected row from the response.
- Never reload in `.catch`.
- Don't clear the array before reloading.
- Once paging exists (FE-20), re-fetch only the current page.

**Behaviour.** BP. The screen shows the same data. Where the server derives fields that the response doesn't carry, keep a reload of **that one row**.

**Effort.** 4 days.

---

### FE-16: The server is queried on every keystroke
**S2 · CLIENT-NETWORK · BP**

**Evidence.** 16 inputs call the server on each change, with no debounce, no minimum length and no cancellation. Out-of-order responses can overwrite newer results, and the search term is not URL-encoded.

| Endpoint | Inputs (template → ts) |
|---|---|
| `/shop_products/get_products_by_shop_containing` | shop-sales-order h315→425; shop-lpo h343→422; shop-grn h352→447; grn h354→448; lpo h395→490 |
| `/products/get_products_by_company` | import-product h93→234; import-restaurant-product h93→233; shop-product-stock-status h126→290; -out h116 and -under h116 → 266; restaurant-product-stock-status h126→326 |
| `/dineables/get_dineables_by_company` | import-dineable h93→234; restaurant-dineable-stock-status h126→289 |
| `/restaurant_dineables/get_dineables_by_restaurant_containing` | restaurant-sales-order h388→467 |
| `/suppliers/get_supplier_by_name_containing` | supplier-product-list h17→480 |
| `/services/get_services_by_company_containing` | select-workshop h186→439 |

**Fix.** Pipe each input through a `Subject` with `debounceTime(300)`, `distinctUntilChanged()`, `filter(q => q.length >= 2)` and `switchMap(...)`, and `encodeURIComponent` the term. Pair this with BE-29 (server limit).

**Effort.** 1.5 days.

---

### FE-17: Requests whose results are never used, and duplicate requests
**S2 · CLIENT-NETWORK · BP**

| Pattern | Locations |
|---|---|
| List fetched but never rendered | service bills: vehicle-equipment-billing 175, 555, 618; good-billing and weight-billing 134, 546; bond-item-billing 139 (awaited, so it delays the screen), 550. Cash collections on open: bond-cash-collection 63, storage-cash-collection 61 (POST report, not in the template). `discounts.ts:206–1300` is copied parking-billing logic its template never uses. |
| Several server calls bound to one button | "Go to Payment" fires GET unpaid list **and** GET entity: vehicle-equipment-billing h211, good-billing h182, weight-billing h182, bond-item-billing h176, machine-service-billing h160, maintenance-vehicle-equipment-billing h170 |
| Same document fetched twice | release-vehicle-equipment `printGatePass` GETs `/parkings/get` at 182 and again at 660. Checkout chains re-GET the entity the `check_out` response already returned (vehicle-equipment-billing 1061; good-billing and weight-billing 1118; bond-item-billing 1187; storage-billing 556; bond-billing 708; weigh-billing 556). The good-release print re-GETs the release it just created (good-billing and weight-billing 1097→1339). |
| Fetch only to navigate, then the target fetches again | select-shop 196, 218; select-restaurant 205, 227 → shop-lpo 107, shop-grn 111 |
| Request with a null id | bond-billing 205 and bond-checkout-list 162 call `get_all_checked_in?bond_zone_id=null` |

**Fix.** Delete the unused calls. Use the response that is already in hand. Use one handler per button. Navigate directly. Skip the call when the id is null.

**Effort.** 1.5 days.

---

### FE-18: Reference data and print headers are fetched again on every screen
**S3 · CLIENT-NETWORK · BP**

**Evidence (about 60 call sites).**
- `/users/get_branch_user_names` is fetched on 18 report screens.
- The selected shop, restaurant or bond zone is fetched on 22 screens.
- `/vehicle_equipment_types/get_all_company_active` on 7 screens.
- Issue types and specialists on 4 screens each.
- Parking zones on 3, available bond zones on 4, suppliers by company on 5, branch shops on 4.
- **Print headers:** `DataService.getBranchReceiptHeaderWithNoTinAndVrn` (data.service.ts:379) and `getDocumentHeader` (651 `/company/get`, then 663 `/company_profile/get_logo`, in sequence) run on **every print**. The second one downloads the logo every time.

**Fix.** Cache these in services (`shareReplay(1)` keyed by branch or company), and clear the cache on logout or when the master data is edited.

**Effort.** 1.5 days.

---

### FE-19: Long sequential chains before a modal or PDF appears
**S2 · CLIENT-NETWORK · BP**

| # | Flow | Chain today | Fix |
|---|---|---|---|
| 1 | Gate pass (release-vehicle-equipment h60) | GET parking (182) → **same** GET (660) → GET last bill date (661) → GET header (673) | entity from row, then date and cached header in parallel |
| 2 | Checkout + gate pass (vehicle-equipment-billing 672→1061/1062/1074; good-billing and weight-billing 662→1118/1119; bond-item-billing 666→1187/1188; storage-billing 506→556/557; bond-billing 658→708/709; release-vehicle-equipment 487→660/661/673) | POST → GET entity → GET date → GET header (plus a full list reload) | use the `check_out` response; fetch the date in parallel with the cached header |
| 3 | Maintenance and vehicle-register checkout (maintenance-verify 430/794, maintenance 437/853, vehicle-register 534/757) | POST → list reload + header (2 sequential) → PDF | cached header |
| 4 | Good release (good-billing and weight-billing 1090→1339→1517; 1379→1381) | POST → GET release → GET company → GET logo | use the response; cached header |
| 5 | Weighbridge save/remove | POST → GET weigh (260) → GET bills (351) | patch from the response |
| 6 | bond-billing and bond-checkout-list `ngOnInit` (195–205, 153–162) | GET zone → GET items, awaited although independent | `Promise.all` |
| 7 | maintenance `openAllIssues` (746–781) | one awaited POST per pending issue | `Promise.all`, or a bulk endpoint (a new endpoint, not a schema change) |
| 8 | role-access h44 | `privilegeChecked()` loops all privileges per cell, per change detection, inside a nested `*ngFor` | precompute a `Set` of `object:operation` keys |

**Effort.** 2 days.

---

### FE-20: Pagination (frontend design)
**S1 · CLIENT-NETWORK · PG**

The full consumer map is in Appendix D. Rules per group:

1. **Payment bill lists: DO NOT PAGE.**
   - Endpoints: `bill_receivables/get_all_by_{parking, storage, weigh, bond_item, machine, maintenance}`.
   - Consumers: vehicle-equipment-billing 524, good-billing 516, weight-billing 516, bond-item-billing 520, machine-service-billing 229, maintenance-vehicle-equipment-billing 403.
   - The client sums `totalBillReceivable` and **posts the whole array** to `confirm_bills_payment`. Paging this list would charge for the wrong bills.
   - These lists are bounded per item and become cheap after BE-28. Keep them unpaged.

2. **Reports: do not page until the server returns the totals.**
   - About 25 report screens (cash-collection with 9 tabs, sales listing, stock logs, GRN/LPO, parking, storage, goods removed, weighbridge bills).
   - They compute totals and PDF exports over the full array.
   - Option A (recommended now): keep them unpaged. They are bounded by date range, so BE-26 and the `collections` index make them fast.
   - Option B (later): server-side totals plus an "export all" endpoint, then page the on-screen table.

3. **Operational lists using client `paginate` and `searchFilter`** (yard lists, discount lists, LPO/GRN lists, POS pending orders, stock status, catalogues).
   - Paging them server-side requires **server search** (`q`), **server sort** (`id,desc` replaces the client `reverse()`), and row numbers computed as `page*size + i + 1`.
   - Switch the templates to ngx-pagination's server mode: `paginate: { itemsPerPage, currentPage, totalItems }`, with `(pageChange)` triggering the fetch.
   - **Required order:** the PAGINATE endpoints of BE-30 first; the FILTER-ENOUGH yard lists only if they still return more than about 500 rows after BE-18 and BE-20.
   - **Search semantics:** today's filter matches any field of the row's JSON. Server search matches declared columns. Agree the column list per screen with the product owner. Where users rely on matching hidden fields, include those fields.

4. **Stock-status screens** filter the full array on the client (shop-product-stock-status 115/124, restaurant-product-stock-status 121/130, restaurant-dineable-stock-status 114/123). Switch to the existing `/get_under_stock_by_shop` and `/get_out_of_stock_by_shop` endpoints before paging.

5. **Per-entity child lists** (bill lines, releases) are bounded per item. Keep them unpaged.

**Effort.** 5–6 days, after BE-30 for each endpoint.

---

## 6. Repository and build hygiene

### HY-01: 2.3 GB `.git`, with build output under version control
**S3 · HYGIENE · BP**

- **773** tracked files sit under `node_modules/`, `target/` or `.metadata/`. That includes 635 under `target/`, plus the Eclipse `.classpath`, `.project`, `.factorypath` and `.settings/`.
- There is no root `.gitignore`.
- **Observed during this audit:** opening a `.class` file in VS Code made the Java extension re-import the project. It rewrote 4 Eclipse metadata files and recompiled about 390 tracked files under `target/classes`. This produced a large, meaningless diff with no source change.

**Fix.**
1. Add a root `.gitignore` (Appendix B).
2. `git rm -r --cached` the build output and IDE metadata.
3. A history rewrite to reclaim the 2.3 GB is optional and needs the whole team to coordinate.

**Effort.** 2 hours.

### HY-02: No production profile
**S3 · CONFIG · BP**

There is a single `application.properties`. It points at `orbix_business_db_test` on `localhost`, with committed credentials and the dev logging (BE-08).

**Fix:**
- Split it into `application.properties` (shared), `application-dev.properties` and `application-prod.properties`, and start production with `--spring.profiles.active=prod`.
- Source the datasource credentials and `jwt.secret` from environment variables.
- This is a prerequisite for BE-08, BE-14 and BE-27 (Appendix B).

**Effort.** 0.5 day.

---

## 6A. Infrastructure and runtime

This audit was static: source code and the built artefact only. The hosting environment has **not** been measured. The items below are therefore checks and tuning to carry out once the baseline week in §8 has been recorded. Where the repository gives evidence, it is cited. Everything else needs access to the servers and the hosting account. None of these items changes the schema or the code's behaviour.

### IN-01: Server sizing not checked against real load
**S2 · CONFIG · BP**

Application and database instance sizes have never been compared with measured use. The code findings above inflate both CPU and memory, so today's sizing may hide waste, or it may be the only thing keeping the system usable.

**Action:**
1. Record CPU, memory, disk I/O and network for the app server and the database for one week (§8).
2. Re-size after Phase 1 and again after Phase 2, when per-request cost drops. Expect to size **down** once the TRACE logging (BE-08) and the per-row queries are gone.

### IN-02: Database server settings
**S2 · CONFIG · BP**

These settings could not be read from the repository. Check and tune:
- `innodb_buffer_pool_size`: about 50–70% of RAM on a dedicated database host.
- `max_connections`: aligned with the tomcat-jdbc `max-active` from BE-14, multiplied by the number of app instances, plus headroom.
- **The slow-query log:** `slow_query_log=ON`, `long_query_time=0.5`, `log_queries_not_using_indexes=ON` for the baseline week. This catches anything that only shows under real traffic, and confirms the BE-04 indexes are used.
- **`tmp_table_size` / `max_heap_table_size`**, for the report `GROUP BY` queries.
- **The MySQL version:** the API uses `MySQL5InnoDBDialect`. If the server is MySQL 8, online index builds (BE-04) and several optimiser improvements are available.

### IN-03: Java runtime settings
**S3 · CONFIG · BP**

No JVM options are recorded in the repository. Set explicitly:
- heap `-Xms`/`-Xmx`, sized to the instance after IN-01;
- `-XX:+UseG1GC`;
- GC logging (`-Xlog:gc*:file=…:time,uptime:filecount=5,filesize=10m`);
- `-XX:+HeapDumpOnOutOfMemoryError`.

Without a cap, the heap grows to the JVM default fraction of RAM and competes with everything else on the host.

### IN-04: Log destination and rotation
**S2 · CONFIG · BP**

**Evidence.**
- The API is built as a **fully executable jar** (`pom.xml:249`, `<executable>true</executable>`; the jar starts with Spring Boot's launch script).
- When installed as an init.d service, that script writes stdout to `/var/log/<app>.log` **with no rotation**.
- Combined with TRACE SQL logging (BE-08), this file grows without limit and is a likely contributor to disk cost.
- Under systemd, the journal applies, with its default size cap unless one is configured.

**Action:**
- Confirm how the service runs.
- Add `logrotate` for the log file, or cap the journal with `SystemMaxUse`.
- Use `logging.file.max-size` and `logging.file.total-size-cap` if file logging is configured.

### IN-05: No monitoring or alerting
**S3 · CONFIG · BP**

Spring Boot Actuator is not a dependency, so the API exposes no health or metrics endpoint.

**Action:**
- Add Actuator with the `health` and `metrics` endpoints, restricted to internal access.
- Add host-level monitoring for CPU, memory, disk and database connections, with alerts at agreed thresholds.
- This also supplies the before/after numbers for §8.

Adding Actuator is a dependency and configuration change only. No business code changes.

### IN-06: Plain HTTP to a raw IP address
**S2 · TRANSPORT · BP (new address)**

**Evidence.** The frontend's `env.js` points the API at `http://51.20.119.45:8080` (FE-03):
- **Unencrypted.** Credentials and JWTs cross the network in clear text.
- **HTTP/1.1 only.** The browser is limited to six parallel connections, which amplifies FE-03.
- **No proxy layer.** There is no reverse proxy to handle compression, caching or TLS.

**Action:**
- Put a reverse proxy (Apache, which already serves the frontend, or nginx) in front of the API, with a domain name and TLS certificate.
- Enable HTTP/2 there.
- Point `env.js` at the new address.

Users see a new, secure address. Nothing else changes.

### IN-07: Runtime-only behaviour
**S3 · CONFIG · BP**

Lock waits, contention under concurrent load, and slow queries that depend on data distribution cannot be seen statically.

**Action:** after the baseline week, review the slow-query log (IN-02), `SHOW ENGINE INNODB STATUS` and the load-test results (§8). Add any new hot spots to Phase 2 or 3 in the same format as the findings above.

---
## 7. Remediation plan

Phases are ordered by **return per unit of risk**. Phases 1–3 change no response and no schema beyond indexes. Phase 4 is the approved contract change (pagination). The frontend bundle track (F) can run in parallel at any time.

### 7.1 Phase 1: Configuration and zero-risk fixes
**About 2 days · all BP · config only, apart from two one-line deletions**

| ID | Action | Effort |
|---|---|---|
| HY-02 | Add a `prod` profile and externalise secrets | 0.5 d |
| BE-08 | SQL logging off in production; check log rotation on the server | 1 h |
| BE-27 | `hibernate.default_batch_fetch_size=100` | 15 m |
| BE-09 | `server.compression.*` | 15 m |
| BE-14 | tomcat-jdbc pool sizing and validation; JDBC URL flags; update batching | 4 h |
| BE-17 | Bound multipart limits | 15 m |
| FE-09 | gzip and cache headers in `.htaccess` | 1 h |
| FE-04.1 | Delete the `debugger` (`search-filter.ts:16`) | 5 m |
| FE-01.1 | Delete the duplicate jQuery (`angular.json:71`) | 5 m |
| HY-01 | Root `.gitignore`; untrack build output and IDE metadata | 2 h |

**Expected (estimate):** a large drop in app server CPU and disk from logging alone. Far fewer round trips everywhere from batch fetching. 70–85% less data transferred.

### 7.1a Track K: Infrastructure (alongside Phase 1)
**Needs server and hosting-account access · BP · no code changes**

| ID | Action | When |
|---|---|---|
| IN-05 | Monitoring and alerting; Actuator `health`/`metrics` | First, so the baseline can be recorded |
| IN-02 | Slow-query log on; check database settings | Baseline week |
| IN-04 | Confirm log destination; add rotation and caps | Immediately (disk) |
| IN-03 | Explicit JVM heap, GC and GC logging | With Phase 1 deploy |
| IN-06 | Reverse proxy with TLS and HTTP/2; update `env.js` | With Phase 1 or 2 |
| IN-01 | Re-size app and database instances | After Phase 1, again after Phase 2 |
| IN-07 | Review runtime-only hot spots and feed them into Phases 2–3 | After the baseline week |

### 7.2 Phase 2: Indexes and the discount path
**About 1.5 weeks · BP / BP\***

| ID | Action | Effort |
|---|---|---|
| BE-04 (P1) | 23 P1 indexes via `@Table(indexes)`; deploy in a quiet window; `SHOW INDEX` + `EXPLAIN` | 1 d |
| BE-18 | Discount landing queries (×3), `getRequests` rewrite, approve/reject user lookups | 1.5 d |
| FE-14 | Discount modals from row data; patch rows; drop redundant and unrendered calls | 1.5 d |
| BE-25 | Delete duplicate and dead queries | 1 d |
| BE-07 | Request-scoped user; hoist `getUser` out of loops | 1.5 d |
| BE-02 | Spring `@Transactional(readOnly = true)` on read methods | 2 d |
| FE-16 | Debounce and cancel type-ahead requests | 1.5 d |
| FE-17 | Remove unused and duplicate requests | 1.5 d |
| BE-04 (P2) | 18 P2 indexes | 0.5 d |

**Expected:** discount screens drop from thousands of statements to single digits, and stop growing with history.

### 7.3 Phase 3: Query rewrites on the remaining hot paths
**About 3 weeks · BP / BP\* · each rewrite needs an equivalence test first (§8)**

| ID | Action | Effort |
|---|---|---|
| BE-20 | Cleared / today / recent lists → `not exists` + batched bills | 3 d |
| BE-19 | Load-to-test → `exists` / `findFirst` / `sum` | 3 d |
| BE-21 | Invoice receivables join-fetch rewrite | 1 d |
| BE-22 | Payment confirmation batching | 1 d |
| BE-23 | Sales / GRN / service confirmation loops | 2.5 d |
| BE-24 | LPO 48 h predicate in SQL; badge set difference; eager detail lists | 1.5 d |
| BE-26 | Sargable monthly stats; remove report join hops | 1.5 d |
| BE-28 | Join-fetch on billing screen lists | 1 d |
| BE-06 | Remaining per-row lookups in mappers | 4 d |
| BE-13 | Report and lookup projections | 2 d |
| BE-03 | `Company.logo` lazy (bytecode enhancement) | 1 d |
| BE-12 | Static JWT algorithm, verifier and `ObjectMapper`; logger | 2 h |
| FE-15 | Patch rows instead of reloading lists | 4 d |
| FE-18 | Cache reference data and print headers | 1.5 d |
| FE-19 | Shorten sequential chains | 2 d |
| FE-05 | Decode the JWT once; `Set`-based `grant()` | 1 d |
| FE-06.1 | `trackBy` on all `*ngFor` | 1.5 d |
| FE-04.2 | Debounce the search filter and cache row strings | 1 d |

### 7.4 Phase 4: Pagination (approved contract change)
**About 2 weeks · PG · backend and frontend ship together, per endpoint**

| ID | Action | Effort |
|---|---|---|
| BE-30 | `/…/page` endpoints with the envelope DTO, server sort and `q` search, for the PAGINATE class | 4–5 d |
| BE-29 | Limit all type-ahead endpoints | 1 d |
| FE-20 | Server-mode paging on the matching screens; agree search columns per screen | 5–6 d |
| BE-05 | Retire the unpaged `findAll()` endpoints once no screen uses them | 0.5 d |

### 7.5 Phase 5: Fetch strategy and change detection
**About 3 weeks · BP but invasive · golden-file tests required**

| ID | Action | Effort |
|---|---|---|
| BE-01 step 3 | `EAGER` → `LAZY` with `@EntityGraph`, module by module, starting with `Branch.childBranches`, `Company.branches` and the detail collections | 5–8 d |
| BE-11 | `@Data` → `@Getter`/`@Setter` with id equality (audit first) | 2–3 d |
| FE-06.2 | `OnPush`, one component at a time with a visual check | 5 d+ |
| FE-07 | `Promise.all` where calls are independent | 3 d |
| BE-15, BE-16 | Startup seeding; dependency removals | 1.5 d |

### 7.6 Track F: Frontend bundle (parallel, any time)

| ID | Action | Effort |
|---|---|---|
| FE-03 | Selective preloading | 0.5 d |
| FE-01 | Shrink `scripts[]` | 3 d |
| FE-02 | Dynamic-import pdfmake | 2 d |
| FE-08 | Strip `console.*` in production | 0.5 d |
| FE-10 | Defer or lazy-load Maps and CKEditor | 1 d |
| FE-11 | Assets | 1 d |
| FE-12 | `takeUntilDestroyed()` | 2 h |
| FE-13 | `inlineCritical` and CLI cache | 0.5 d |

---

## 8. Verification plan

The figures in this document come from static analysis. Measure before and after each phase.

**Server resources** (the cost question):
1. Record a one-week baseline before Phase 1:
   - app server CPU, resident memory and GC time;
   - database CPU, IOPS and connections;
   - log volume per day and free disk.
2. Compare the same week-over-week figures after each phase. The target is for cost to stop tracking data growth.

**Backend:**
1. **Statements per endpoint.** In a staging profile, enable `hibernate.generate_statistics=true` (or p6spy). Record the statement count for the top 25 endpoints, starting with the discount and billing endpoints. The goal is a count that does not grow with row counts.
2. **Indexes.** After the first boot with BE-04, run `SHOW INDEX FROM <table>`. Then run `EXPLAIN` for each query in Appendix C and confirm the index is chosen and `rows` drops.
3. **Equivalence tests for every rewrite** (BE-18 to BE-26). Run old and new against a copy of production data and assert identical row sets **and order**. This is mandatory for BP\* items.
4. **Golden-file responses** for the top 25 endpoints. Mandatory before Phase 5.
5. Load-test the discount landing screens, a billing screen open, payment confirmation and one finance report at production-like volume. Record p50, p95 and p99.

**Frontend:**
1. Network panel request counts for each discount flow in FE-14, before and after.
2. Lighthouse on login and the two heaviest screens.
3. `source-map-explorer` after Track F.
4. Type quickly in each FE-16 input and confirm one request after the pause, with stale requests cancelled.

---

## 9. Risk register

| Risk | Finding | Mitigation |
|---|---|---|
| Index creation lengthens the first startup on large tables | BE-04 | Deploy in a quiet window; allow a longer health-check grace period; P1 first, P2 later |
| A camelCase `columnList` stops startup with "column not found" | BE-04 | Use physical snake_case names (Appendix C); boot once in staging |
| A wrong index cannot be removed by `ddl-auto=update` | BE-04, BE-10 | Code-review index names; remove by hand with `DROP INDEX` if needed |
| A rewritten query returns different rows or order | BE-18 to BE-26 | Equivalence tests on production-like data; explicit `order by id`; encode each Java predicate exactly (BE-20 note) |
| A `SUM` in SQL differs from a Java sum in the last digit | BE-19, BE-25 | Keep the Java sum over a projection wherever exact equality matters |
| Mixing the `javax` and Spring `@Transactional` annotations confuses reviewers | BE-02 | Fully qualify the Spring annotation on read methods; review checklist |
| A `readOnly` method that writes silently stops writing | BE-02 | Annotate per method, never per class; check each method for `save()` |
| A pool sized too high overloads the DB; too low queues requests | BE-14 | Size from DB vCPU and `max_connections`; load-test |
| Paging the payment list charges for the wrong bills | FE-20 | Payment and report lists are explicitly DO-NOT-PAGE |
| Server search matches different rows than the client filter | FE-20 | Agree search columns per screen; release notes |
| A half-migrated paging contract | BE-30 / FE-20 | New `/page` endpoints next to the old ones; retire the old ones last |
| `LazyInitializationException` after `EAGER` → `LAZY` | BE-01 | Phase 5 only; entity graphs; keep `open-in-view`; golden files |
| `@Basic(LAZY)` ignored without bytecode enhancement | BE-03 | Check the generated SQL in the dev profile |
| `OnPush` stops rendering on mutated arrays | FE-06.2 | One component at a time, with a visual check |
| Cache headers pin users to a stale build | FE-09 | `index.html` is `no-cache` |

---

## Appendix A: Findings index

| ID | Sev | Category | Tag | Phase | Title |
|---|---|---|---|---|---|
| BE-01 | S1 | PERSISTENCE | BP | 3 / 5 | Universal `EAGER` fetching, cyclic graph |
| BE-02 | S1 | PERSISTENCE | BP | 2 | Read paths in read-write transactions; `javax` annotation |
| BE-03 | S2 | PERSISTENCE | BP\* | 3 | `Company.logo` loaded with every user (corrected) |
| BE-04 | S1 | INDEX | BP | 2 | No indexes: 47 proposed through `ddl-auto=update` |
| BE-05 | S1 | QUERY | BP / PG | 4 | Unbounded `findAll()` |
| BE-06 | S1 | QUERY | BP | 3 | Per-row queries in mappers and loops |
| BE-07 | S2 | PERSISTENCE | BP | 2 | User re-resolved up to 6× per request |
| BE-08 | S1 | CONFIG | BP | 1 | SQL logging at TRACE in production |
| BE-09 | S2 | TRANSPORT | BP | 1 | No response compression |
| BE-10 | S4 | CONFIG | BP | — | `ddl-auto=update` kept: operating notes |
| BE-11 | S2 | PERSISTENCE | BP\* | 5 | Lombok `@Data` on entities |
| BE-12 | S3 | RUNTIME | BP | 3 | Per-request allocations in filters |
| BE-13 | S3 | QUERY | BP | 3 | Full entities loaded for scalars |
| BE-14 | S3 | CONFIG | BP | 1 | tomcat-jdbc pool and JDBC flags (corrected) |
| BE-15 | S4 | RUNTIME | BP | 5 | O(n²) startup seeding |
| BE-16 | S4 | CONFIG | BP | 5 | Unused dependencies (corrected) |
| BE-17 | S4 | CONFIG | BP | 1 | Unbounded multipart limits |
| BE-18 | S1 | QUERY / INDEX | BP / BP\* | 2 | **Discount workflow** |
| BE-19 | S1 | QUERY | BP\* | 3 | All bills loaded to answer exists / last / sum |
| BE-20 | S1 | QUERY | BP\* | 3 | Cleared / today / recent lists, 2–4 queries per row |
| BE-21 | S1 | QUERY | BP / PG | 3 / 4 | Invoice receivables load every parking |
| BE-22 | S2 | QUERY | BP | 3 | Payment confirmation, 8 lookups per bill |
| BE-23 | S2 | QUERY | BP | 3 | Sales / GRN / service confirmation loops |
| BE-24 | S2 | QUERY | BP / PG | 3 | Document lists over all history, filtered in Java |
| BE-25 | S2 | QUERY | BP | 2 | Duplicate and dead queries |
| BE-26 | S2 | QUERY | BP | 3 | Non-sargable report SQL, extra joins |
| BE-27 | S1 | CONFIG | BP | 1 | Batch fetching not configured |
| BE-28 | S2 | QUERY | BP | 3 | Billing lists load one `BillReceivable` per bill |
| BE-29 | S2 | QUERY | PG | 4 | Unbounded `%x%` type-ahead |
| BE-30 | S1 | QUERY | PG | 4 | Server-side pagination (backend) |
| FE-01 | S1 | BUNDLE | BP | F | 4.67 MB global bundle, jQuery ×3 |
| FE-02 | S1 | BUNDLE | BP | F | pdfmake and fonts in 62 routes |
| FE-03 | S1 | BUNDLE | BP | F | `PreloadAllModules` |
| FE-04 | S1 | CLIENT-RUNTIME | BP | 1 / 3 | Search filter stringify; `debugger` |
| FE-05 | S1 | CLIENT-RUNTIME | BP | 3 | JWT decoded on every change detection |
| FE-06 | S2 | CLIENT-RUNTIME | BP | 3 / 5 | No `OnPush` / `trackBy` |
| FE-07 | S2 | CLIENT-NETWORK | BP | 5 | Sequential waterfalls |
| FE-08 | S2 | CLIENT-RUNTIME | BP | F | `console.log` volume |
| FE-09 | S2 | TRANSPORT | BP | 1 | No static compression or caching |
| FE-10 | S2 | TRANSPORT | BP | F | Render-blocking third-party scripts |
| FE-11 | S3 | BUNDLE | BP | F | Unoptimised assets |
| FE-12 | S3 | CLIENT-RUNTIME | BP | F | Unreleased router subscriptions |
| FE-13 | S3 | BUNDLE | BP | F | Optimisation partly disabled |
| FE-14 | S2 | CLIENT-NETWORK | BP | 2 | **Discount flows (frontend)** |
| FE-15 | S2 | CLIENT-NETWORK | BP | 3 | Full list reload after every action |
| FE-16 | S2 | CLIENT-NETWORK | BP | 2 | Server queried on every keystroke |
| FE-17 | S2 | CLIENT-NETWORK | BP | 2 | Unused and duplicate requests |
| FE-18 | S3 | CLIENT-NETWORK | BP | 3 | Reference data and print headers not cached |
| FE-19 | S2 | CLIENT-NETWORK | BP | 3 | Long sequential chains |
| FE-20 | S1 | CLIENT-NETWORK | PG | 4 | Server-side pagination (frontend) |
| HY-01 | S3 | HYGIENE | BP | 1 | Build output and IDE metadata in git |
| HY-02 | S3 | CONFIG | BP | 1 | No production profile |
| IN-01 | S2 | CONFIG | BP | K | Server sizing not checked against real load |
| IN-02 | S2 | CONFIG | BP | K | Database server settings; slow-query log |
| IN-03 | S3 | CONFIG | BP | K | Java runtime settings |
| IN-04 | S2 | CONFIG | BP | K | Log destination and rotation (executable jar) |
| IN-05 | S3 | CONFIG | BP | K | No monitoring or alerting |
| IN-06 | S2 | TRANSPORT | BP (new address) | K | Plain HTTP to a raw IP address |
| IN-07 | S3 | CONFIG | BP | K | Runtime-only behaviour |

---

## Appendix B: Configuration reference

**`application-prod.properties`** (new). All values are behaviour-preserving.

```properties
# --- BE-08: logging --------------------------------------------------------
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type=WARN

# --- BE-27: batch fetching (biggest no-code win) ---------------------------
spring.jpa.properties.hibernate.default_batch_fetch_size=100

# --- BE-14: JDBC batching (updates/deletes only; IDENTITY blocks inserts) --
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_updates=true

# --- BE-14: tomcat-jdbc pool (the jar ships tomcat-jdbc, NOT HikariCP) -----
# Size against DB vCPU and max_connections; these are starting points.
spring.datasource.tomcat.max-active=30
spring.datasource.tomcat.max-idle=30
spring.datasource.tomcat.min-idle=5
spring.datasource.tomcat.initial-size=5
spring.datasource.tomcat.max-wait=20000
spring.datasource.tomcat.test-on-borrow=true
spring.datasource.tomcat.validation-query=SELECT 1
spring.datasource.tomcat.validation-interval=30000
# open-in-view stays at its default (true) until BE-01 step 3.

# --- BE-14 / BE-02: Connector/J flags (append to the existing URL params) ---
spring.datasource.url=${DB_URL}
#   DB_URL example:
#   jdbc:mysql://<host>:3306/<db>?useSSL=false&serverTimezone=UTC&useLegacyDatetimeCode=false
#     &cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048
#     &useServerPrepStmts=true&useLocalSessionState=true&rewriteBatchedStatements=true
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}

# --- R2: schema management unchanged ---------------------------------------
spring.jpa.hibernate.ddl-auto=update

# --- BE-09: compression ----------------------------------------------------
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/plain,text/css,application/javascript
server.compression.min-response-size=1024

# --- BE-17: bounded uploads (matches MainApplication.java:311) ------------
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
spring.servlet.multipart.location=${java.io.tmpdir}
```

**`application-dev.properties`** keeps today's SQL logging for local debugging. Add `spring.jpa.properties.hibernate.generate_statistics=true` there for §8.

**`src/.htaccess`** (FE-09). Append to the existing rewrite rules:

```apache
<IfModule mod_deflate.c>
  AddOutputFilterByType DEFLATE text/html text/css text/plain \
      application/javascript application/json image/svg+xml
</IfModule>
<IfModule mod_headers.c>
  <FilesMatch "\.(js|css|woff2?|ttf|eot|svg)$">
    Header set Cache-Control "public, max-age=31536000, immutable"
  </FilesMatch>
  <FilesMatch "^index\.html$">
    Header set Cache-Control "no-cache, must-revalidate"
  </FilesMatch>
</IfModule>
```

**Root `.gitignore`** (HY-01):

```gitignore
node_modules/
dist/
target/
.metadata/
.angular/
*.zip
*.log
# Eclipse / VS Code Java LS metadata
.classpath
.project
.factorypath
.settings/
```

---

## Appendix C: Index catalogue

**Rules (BE-04):**
- Declare each index on its entity's `@Table` and let `ddl-auto=update` create it.
- Use **physical snake_case** column names.
- Always give the index an explicit `name`.
- Keep any existing `uniqueConstraints`.
- Add `import javax.persistence.Index;`.

**Already covered, no index needed:**
- **Single-FK lookups.** InnoDB indexes FK columns. This covers every `findAllByParking`/`Storage`/`BondItem`/`Weigh`/`Grn`/`Lpo`/`Machine`, `findByBillReceivable`, `findByInvoiceReceivable`, `findAllByBranch`/`Company`, and the 3-hop maintenance bill path.
- **Unique keys.** `users.username`/`code`/`nickname`; `(name, branch_id)` and `(name, company_id)` on zones, warehouses and types; `findByName` on branches, companies, roles and privileges; the `no` column on parkings, bond items, storages, bill receivables, machines, weighs and maintenances; `days.bussiness_date`; and the restaurant and currency composite uniques.
- **FK plus status with few rows per parent.** `findAllByVehicleEquipmentAndStatusIn`, `findFirstByMaintenanceAndStatusIn`, `findAllByMachineAndStatus`.

### C.1 Proposed indexes

**P1: hot paths and tables that grow without bound (23)**

```java
// discount workflow (BE-18)
@Table(name = "discount_requests", indexes = {
    @Index(name = "ix_discount_requests_bill_id_name", columnList = "service_bill_id, service_bill_name") })
@Table(name = "parking_bill_receivables", indexes = {
    @Index(name = "ix_pbr_discount_status_parking", columnList = "discount_status, parking_id") })
@Table(name = "storage_bill_receivables", indexes = {
    @Index(name = "ix_sbr_discount_status_storage", columnList = "discount_status, storage_id") })
@Table(name = "bond_item_bill_receivables", indexes = {
    @Index(name = "ix_bibr_discount_status_bond_item", columnList = "discount_status, bond_item_id") })

// yard lists, check-in duplicate checks, dashboards
@Table(name = "parkings", indexes = {
    @Index(name = "ix_parkings_status_checked_out", columnList = "status, checked_out_date_time"),
    @Index(name = "ix_parkings_checked_in_status",  columnList = "checked_in_date_time, status"),
    @Index(name = "ix_parkings_chasis_no_status",   columnList = "chasis_no, status") })           // + P2 below
@Table(name = "bond_items", indexes = {
    @Index(name = "ix_bond_items_status_checked_out",      columnList = "status, checked_out_date_time"),
    @Index(name = "ix_bond_items_zone_status_checked_out", columnList = "bond_zone_id, status, checked_out_date_time"),
    @Index(name = "ix_bond_items_chasis_no",               columnList = "chasis_no") })        // + P2 below
@Table(name = "storages", indexes = {
    @Index(name = "ix_storages_status_checked_out",           columnList = "status, checked_out_date_time"),
    @Index(name = "ix_storages_warehouse_status_checked_out", columnList = "warehouse_id, status, checked_out_date_time") }) // + P2
@Table(name = "maintenances", indexes = {
    @Index(name = "ix_maintenances_status_checked_out", columnList = "status, checked_out_date_time") })
@Table(name = "weighs", indexes = {
    @Index(name = "ix_weighs_created_date_time", columnList = "created_date_time") })
@Table(name = "vehicle_equipments", indexes = {
    @Index(name = "ix_vehicle_equipments_chasis_no_active", columnList = "chasis_no, active") })

// finance and sales reports
@Table(name = "collections", indexes = {
    @Index(name = "ix_collections_collection_date_time", columnList = "collection_date_time") })
@Table(name = "sales", indexes = {
    @Index(name = "ix_sales_created_date_time", columnList = "created_date_time") })
@Table(name = "restaurant_sales", indexes = {
    @Index(name = "ix_restaurant_sales_created_date_time", columnList = "created_date_time") })
@Table(name = "shop_product_logs", indexes = {
    @Index(name = "ix_shop_product_logs_shop_created", columnList = "shop_id, created_date_time") })
@Table(name = "restaurant_product_logs", indexes = {
    @Index(name = "ix_restaurant_product_logs_restaurant_created", columnList = "restaurant_id, created_date_time") })

// POS and service bay
@Table(name = "restaurant_sales_orders",
    uniqueConstraints = { @UniqueConstraint(columnNames = {"no", "restaurant_id"}) },
    indexes = {
        @Index(name = "ix_rso_restaurant_status_created", columnList = "restaurant_id, status, created_date_time") }) // + P2
@Table(name = "machines", indexes = {
    @Index(name = "ix_machines_branch_status_created", columnList = "branch_id, status, created_date_time"),
    @Index(name = "ix_machines_workshop_created",      columnList = "workshop_id, created_date_time") })
```

What each P1 index serves:

| Index | Serves (repository:line) | Hot call sites |
|---|---|---|
| ix_discount_requests_bill_id_name | `DiscountRequestRepository:10, :12` | `DiscountRequestServiceController:78, 111, 137, 185, 233` |
| ix_pbr / ix_sbr / ix_bibr_discount_status_* | the BE-18 rewrite | parking-, storage- and bond-discounts landing screens |
| ix_parkings_status_checked_out | `ParkingRepository:14, :41, :22, :26` | `ParkingServiceController:91, 106, 239, 254, 150, 197`; `ParkingReportResource:77, 79, 80` |
| ix_parkings_checked_in_status | `ParkingRepository:38, :19`; `:49` after the BE-26 rewrite | `ParkingReportResource:71, 175` |
| ix_parkings_chasis_no_status | `ParkingRepository:46` | `ParkingServiceController:305`; `VehicleEquipmentServiceController:164, 243` |
| ix_bond_items_* | `BondItemRepository:15, 17, 20, 24, 54, 81` | `BondItemServiceController:90, 110, 128, 143, 187, 222, 261, 303` |
| ix_storages_* | `StorageRepository:14, 16, 19, 51, 78` | `StorageServiceController:90, 109, 127, 142, 168, 184, 217, 248` |
| ix_maintenances_status_checked_out | `MaintenanceRepository:15, 17, 22, 25, 28` | `MaintenanceServiceController:86, 102, 121, 143, 159, 197, 227` |
| ix_weighs_created_date_time | `WeighRepository:10` | `WeighServiceController:51` |
| ix_vehicle_equipments_chasis_no_active | `VehicleEquipmentRepository:18` | `VehicleEquipmentServiceController:101, 122` |
| ix_collections_collection_date_time | `CollectionRepository:29, 49, 158, 264, 302, 326, 350, 378, 411, 445, 478, 509`; `BillReceivableCollectionRepository:15` | all 11 `FinanceReportResource` endpoints |
| ix_sales_created_date_time / ix_restaurant_sales_created_date_time | `SaleRepository:12, 34`; `RestaurantSaleRepository:12` | `SalesReportResource:44, 55`; `RestaurantReportResource:52` |
| ix_shop_product_logs_shop_created / ix_restaurant_product_logs_restaurant_created | `ShopProductLogRepository:12`; `RestaurantProductLogRepository:12` | `ReportResource:68, 96` |
| ix_rso_restaurant_status_created | `RestaurantSalesOrderRepository:15, :13` | `RestaurantSalesOrderServiceController:96` |
| ix_machines_* | `MachineRepository:37, :18` | `MachineServiceController:219, 116` |

**P2: secondary reports and procurement (18).** Add these to the same `@Table` declarations:

| Table | Index | Columns | Serves |
|---|---|---|---|
| parkings | ix_parkings_created_status | created_date_time, status | `ParkingRepository:32` → `ParkingReportResource:120` |
| parkings | ix_parkings_created_by_created | created_by_user_id, created_date_time | `:29` → `ParkingReportResource:112` |
| parkings | ix_parkings_checked_in_by_checked_in | checked_in_by_user_id, checked_in_date_time | `:35` → `ParkingReportResource:167` |
| bond_items | ix_bond_items_checked_in_status | checked_in_date_time, status | `BondItemRepository:49, 56` → `BondItemReportResource:122` |
| bond_items | ix_bond_items_created_status | created_date_time, status | `:43` → `BondItemReportResource:81` |
| bond_items | ix_bond_items_created_by_created | created_by_user_id, created_date_time | `:40` → `BondItemReportResource:73` |
| storages | ix_storages_checked_in_status | checked_in_date_time, status | `StorageRepository:46, 53` → `StorageReportResource:125` |
| storages | ix_storages_created_status | created_date_time, status | `:40` → `StorageReportResource:84` |
| storages | ix_storages_created_by_created | created_by_user_id, created_date_time | `:37` → `StorageReportResource:76` |
| maintenance_job_card_issues | ix_mjci_specialist_status_closed | service_specialist_user_id, status, closed_date_time | `MaintenanceJobCardIssueRepository:11`; `MaintenanceRepository:25, 28` |
| bill_receivables | ix_bill_receivables_paid_status | paid_date_time, pay_status | `ParkingBillReceivableRepository:21` → `ParkingReportResource:73` |
| restaurant_sales_orders | ix_rso_status_confirmed | status, confirmed_date_time | `RestaurantSalesOrderDetailRepository:18` → `RestaurantReportResource:42` |
| shop_sales_orders | ix_shop_sales_orders_shop_status | shop_id, status | `ShopSalesOrderRepository:12` → `ShopSalesOrderServiceController:82` (keep the existing `uniqueConstraints {"no","shop_id"}`) |
| grns | ix_grns_status_approved | status, approved_date_time | `GrnRepository:19, 25` |
| grns | ix_grns_branch_status_shop | branch_id, status, shop_id | `GrnRepository:17, 21` → `GrnServiceController:88, 109` |
| lpos | ix_lpos_status_approved | status, approved_date_time | `LpoRepository:17, 28` |
| lpos | ix_lpos_branch_status_shop | branch_id, status, shop_id | `LpoRepository:21, 25` → `LpoServiceController:89, 114`; also serves the BE-24 rewrite |
| lpos | ix_lpos_no | no | `LpoRepository:23` → `GrnServiceController:158` (`lpos.no` is not unique) |

**P3: optional, small or slow-growing tables (6):**
- `days(status)`
- `removed_vehicle_equipments(created_date_time)`
- `removed_goods(created_date_time)`
- `shop_products(shop_id, product_id)`
- `restaurant_products(restaurant_id, product_id)`
- `supplier_products(supplier_id, product_id)`

The last three gain little, because the `product_id` FK already narrows the lookup to about one row per shop.

**Write cost.** `parkings` ends up with 6 secondary indexes. If insert rate becomes a concern, drop the P2 indexes on it first.

### C.2 Predicates no index can help as written
- **Leading-wildcard `LIKE` plus `UPPER()`:**
  - `UserRepository:37` (an OR across 3 columns);
  - `VehicleEquipmentRepository:20`;
  - `ProductRepository:12`, `DineableRepository:11`, `ServiceRepository:12`, `SupplierRepository:11`;
  - `RestaurantDineableRepository:15`, `RestaurantProductRepository:22`, `ShopProductRepository:24`, `SupplierProductRepository:22`.

  Bound them with limits (BE-29). Switching to prefix search is a behaviour change (Appendix E-4).
- **Functions on columns:** `YEAR()`/`MONTH()` in `getMonthlyStats`. Rewrite as ranges (BE-26).
- **Column-to-column comparisons:** `current_stock < min_stock` and `current_stock = 0 OR current_stock < 0` (`RestaurantProductRepository:26–38`, `ShopProductRepository:28–40`). Only the shop or restaurant FK can be used, which is fine at catalogue sizes.
- **Optional-parameter ORs:** the date range drives these queries (BE-26).
- **Boolean flags** (`active`, `isDefault`, `defaultCurrency`) are not selective. Don't index them.

---

## Appendix D: Pagination map

**PAGINATE** (backend BE-30, then the frontend consumers per FE-20):

| Endpoint | Resource:line | Frontend consumers |
|---|---|---|
| `/parkings` | ParkingResource:40 | admin and list screens |
| `/bond_items` | BondItemResource:42 | admin and list screens |
| `/storages` | StorageResource:41 | admin and list screens |
| `/maintenances` | MaintenanceResource:34 | admin and list screens |
| `/vehicle_equipments`, `/vehicle_equipments/get_all_active` | VehicleEquipmentResource:33, 38 | vehicle registers |
| `/invoice_receivables` | InvoiceReceivableResource:26 | receivable-invoice-list.ts:43 |
| `/invoice_receivables/get_pending_parking_invoice_receivables` | :31 | parking-receivable-invoice-list.ts:45 |
| `/grns/get_all_visible_by_branch`, `_by_shop`, `_by_restaurant` | GrnResource:37, 43 | grn.ts:192, shop-grn.ts:195, select-shop.ts:171, select-restaurant.ts:172 |
| `/products`, `/dineables`, `/services`, `/suppliers` (above about 1k rows) | ProductResource:33, DineableResource:31, ServiceResource:32, SupplierResource:32 | product.ts:68, dineable.ts:68, service.ts:68, supplier.ts:69, supplier-price-list.ts:89 |
| `/users/load_users_like` and all `*_containing` / `*_by_company` type-aheads (limit only) | UserResource:152; Dineable 85/111; Product 87/120; Service 86/119; Supplier 85/92; VehicleEquipmentResource:97 | the FE-16 inputs |

**DO-NOT-PAGE** (financial correctness):
- `bill_receivables/get_all_by_{parking, storage, weigh, bond_item, machine, maintenance}`, used by the payment screens. The client totals the array and posts all of it to `confirm_bills_payment`.

**Keep unpaged for now; needs server totals before paging:**
- All `finance_reports/*`, `sales_reports/*`, stock logs, GRN/LPO reports, parking, storage and goods-removed reports, and `weigh_bills/get_by_weigh_id`.
- They are date-bounded and become fast with BE-26 and the `collections` index.

**FILTER-ENOUGH** (bounded by status, window or parent; page only if still more than about 500 rows after BE-18 and BE-20):
- **Yard lists:** parkings/bond/storage/maintenance `get_all_checked_in`, `pending_or_checked_in`, `cleared`, `with_discounts`, `today` and `recent`, plus the per-zone, per-warehouse and per-workshop variants.
- **POS and orders:** `/weighs/recent`, `/machines/by_*`, `/discount_requests`, `lpos/get_all_visible_*` (after the BE-24 rewrite), `shop_sales_orders/get_all_pending_by_shop`, `restaurant_sales_orders/get_all_pending_by_restaurant`.
- **Per-parent lists:** every per-parent bill and release list.

**REFERENCE** (keep unpaged):
- Branches, companies (use a projection, to skip loading logos), currencies, restaurants, shops, workshops, zones, types, warehouses, UoMs and roles.
- Branch-scoped dropdown lists and user or agent name lists.

---

## Appendix E: Behaviour changes left out of the plan

These would improve performance but change what users see. Each needs a product decision before it can enter the plan.

- **E-1: Branch and company scoping.** No list, yard, report or count filters by the user's branch or company. Every screen loads all branches' data. Examples: all `findAll()` lists; the `findAllByStatusIn` yard lists; `get_all_with_discounts`; `/weighs/recent`; pending GRNs and LPOs; `findTop2000ByActiveTrue`; every report. Adding the filter would cut volume roughly in proportion to the number of branches, and would also fix a data-visibility issue. It changes results, so it needs sign-off. **Strongly recommended.**
- **E-2: "Pending" invoice list that isn't filtered.** `getPendingParkingInvoiceReceivables` returns every invoice. Filtering it to open statuses, as the code comment intends, changes results.
- **E-3: Spring Boot upgrade.** 2.2.5 is end-of-life, as are `MySQL5InnoDBDialect`, `jjwt` 0.9.1 and `jackson-databind` 2.9.8. This is a project in its own right.
- **E-4: Prefix type-ahead.** `StartingWith` with an index on the column makes searches index-backed, but stops matching mid-string.
- **E-5: Reactive permission menu.** `menuItems` evaluates `grant()` once at import, so the menu does not refresh after a re-login. Fixing that is a behaviour change, so raise it as a defect.
- **E-6: Data retention and archiving.** Every business table keeps all history forever, so table size, index size, backup size and report ranges all grow without limit. A retention policy (moving closed records older than an agreed period to archive tables that stay readable for reports) would bound the live data set. It needs new archive tables, which breaks R1, and a business decision on the retention period. Decide this at executive level; it is listed in the executive proposal under "Related matters".

---

## Appendix F: Non-performance defects found during the audit

These are listed for triage only. They are outside the scope of this document.

| Defect | Location |
|---|---|
| Weighbridge discount requests always fail: the frontend sends `serviceBillName: 'Weigh'`, and the backend accepts only Parking, Storage and Bond (`Invalid Service selected`) | weight-billing.ts:279; `DiscountRequestServiceController:277` |
| Approve and reject errors are swallowed in the UI | discounts.ts:391–395, 417–421 |
| The Request/View modal shows the previous bill's reason and comments | vehicle-equipment-billing and siblings (FE-14) |
| Signing key hard-coded as `"secret"`, while `jwt.secret` is committed separately | `CustomAuthorizationFilter:67`; `application.properties` |
| Every list ignores branch boundaries | Appendix E-1 |
| `/maintenances/get_today_checked_out` returns `null` | MaintenanceResource:69 → `MaintenanceServiceController:183–186` |

---

*End of audit, revision 2.*
