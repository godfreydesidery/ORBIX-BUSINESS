# ORBIX-BUSINESS — Performance Audit & Remediation Plan

**Audit date:** 2026-09-12
**Scope:** `Orbix-Business-API` (Spring Boot 2.2.5 / Java 11 / MySQL) and `Orbix-Bussiness-Web` (Angular 18 standalone)
**Governing constraint:** *No change to system structure or observable behaviour.* Every finding below is classified against this constraint. Fixes that would alter an API contract, a payload shape, or a user-visible flow are quarantined in Appendix C and are **not** part of the recommended plan without explicit sign-off.

---

## 1. Executive summary

The codebase is functionally broad and internally consistent, but it carries a small number of *systemic* performance defects that are each replicated hundreds of times. The cost is multiplicative rather than additive: an unpaginated query feeds an over-eager object graph, which is serialised uncompressed over HTTP/1.1 into a browser that re-filters it by JSON-stringifying every row on every keystroke, inside a change-detection loop that has no memoisation anywhere.

**Headline measurements taken during this audit:**

| Metric | Measured value | Healthy target |
|---|---|---|
| JPA relations mapped `EAGER` | **232** (vs. 2 `LAZY`) | Near-zero `EAGER` |
| Entities declaring a database index | **0 of 81** | All hot filter columns |
| Endpoints supporting pagination (`Pageable`/`Page<>`) | **0** | All collection endpoints |
| `@Transactional(readOnly = true)` on read paths | **0** (133 `@Transactional`, all read-write) | All GET paths |
| HTTP response compression | **Disabled** (server and Apache) | gzip/brotli on |
| Production global script bundle (`scripts-*.js`) | **4.67 MB** | < 300 KB |
| Total built JS shipped | **11.4 MB** across 137 files | < 2 MB initial |
| jQuery copies in one bundle | **3** (v3.4.1 ×2, v1.9.1 ×1) | 1 |
| Angular components using `OnPush` | **0 of 160** | Majority |
| `*ngFor` loops with `trackBy` | **0 of 311** | All list loops |
| `console.log` calls in the production bundle | **1,491** | 0 |
| `debugger` statements in the production bundle | **1** | 0 |
| `.git` directory size | **2.3 GB** | < 200 MB |

**Expected outcome of the Phase 1 + Phase 2 plan (Sections 7.1–7.2), all behaviour-preserving:**

- List endpoint latency: **60–90 % reduction** on the worst paths (driven by SQL count collapse, blob elimination and read-only transactions).
- Transferred bytes per API response: **70–85 % reduction** (compression alone).
- First contentful paint: **3–6 s faster** on a typical connection (bundle + compression + preload strategy).
- Interaction latency on data grids: **materially improved** (search-filter and change-detection fixes).

None of Phase 1 or Phase 2 alters an API contract, a JSON payload shape, or a screen.

---

## 2. Method and evidence base

Findings were derived by static analysis of the working tree, cross-checked against the committed production build in `Orbix-Bussiness-Web/dist/`. Every claim below cites a file and line, or a reproducible count. The built artefacts were measured directly, so bundle figures are actual and not projected.

**Not performed** (and therefore not claimed): runtime profiling, load testing, SQL `EXPLAIN` against a populated database, or browser tracing. Section 8 defines the measurement plan needed to confirm these findings empirically and to quantify the gains. Where this document estimates an improvement, it is labelled as an estimate.

**A note on the two `image` columns.** Several findings below depend on the fact that `Byte[] image` is dead weight. This was verified: *every* `setImage(...)` call site in the backend is commented out — `ParkingServiceController.java:380,669`, `VehicleEquipmentServiceController.java:217,409,448`, `BondItemServiceController.java:392,420,623`, `MaintenanceServiceController.java:309,525`, `StorageServiceController.java:366,524`. The column is never written and never returned. Making it lazy is provably behaviour-neutral.

---

## 3. Classification scheme

### Severity

| Level | Meaning |
|---|---|
| **S1 — Critical** | Cost scales with data volume. Already degrading, or will fail outright as tables grow. |
| **S2 — High** | Large constant-factor cost on every request or page load. |
| **S3 — Moderate** | Measurable waste; compounds with S1/S2 but survivable alone. |
| **S4 — Low / Hygiene** | Correctness-adjacent, developer velocity, or latent risk. |

### Behaviour risk

| Tag | Meaning |
|---|---|
| **BP** | Behaviour-preserving. Identical observable output; only execution cost changes. |
| **BP\*** | Behaviour-preserving *given a stated precondition* that has been verified in this codebase. |
| **BC** | Behaviour- or contract-changing. **Excluded from the plan**; see Appendix C. |

### Category

`PERSISTENCE` · `RUNTIME` · `TRANSPORT` · `BUNDLE` · `CLIENT-RUNTIME` · `CONFIG` · `HYGIENE`

---

## 4. Backend findings

### BE-01 — Universal `EAGER` fetching creates a self-referential object graph
**S1 · PERSISTENCE · BP**

**Evidence.** 232 relations are mapped `FetchType.EAGER`; only 2 are `LAZY`. The graph is cyclic:

```
BondItem  (BondItem.java:124–170)
  ├─ BondZone            EAGER
  ├─ BondItemType        EAGER
  ├─ Branch              EAGER
  └─ User × 4            EAGER   (createdBy, checkedIn, checkedOut, canceledBy)
        └─ User.java:65  roles          ManyToMany EAGER (SUBSELECT)
        │     └─ Role.privileges        ManyToMany EAGER
        │     └─ Role.company           EAGER ─┐
        ├─ User.java:75  company        EAGER ─┤
        │     ├─ Company.java:77  logo  @Lob   │  ← BLOB, loaded every time
        │     ├─ Company.currency      EAGER   │
        │     ├─ Company.timeZone      EAGER   │
        │     ├─ Company.createdByUser EAGER   │  ← back into User
        │     └─ Company.java:112 branches  ManyToMany EAGER
        │           └─ Branch.company  EAGER ──┘  ← cycle
        │           └─ Branch.parentBranch EAGER  ← recursive
        └─ User.java:81  branch         EAGER
```

Retrieving a **single** `BondItem` row materialises the company, its entire branch list, each branch's parent chain, the user's roles and every privilege on them — plus the company logo BLOB. Hibernate's first-level cache prevents infinite recursion, but the first-touch cost per request is the full transitive closure.

Critically, `EAGER` associations reached through a **JPQL or derived query** (as opposed to `find(id)`) are *not* join-fetched. Hibernate issues a secondary `SELECT` per association per row. A `findAll()` over *N* bond items therefore issues on the order of *N × 7* queries before the graph fan-out is counted.

**Fix.** Convert `@ManyToOne` / `@OneToOne` to `fetch = FetchType.LAZY`, and add `@EntityGraph` / `JOIN FETCH` on the specific repository methods whose mappers actually dereference the association. The DTO mappers are the authority on what is genuinely needed — e.g. `BondItemServiceController.java:589–693` touches only `bondZone.name`, `bondItemType.name` and `branch.id`, so a graph of exactly those three is sufficient. The four `User` associations are never dereferenced by that mapper at all.

**Behaviour note.** BP, provided every dereference site is covered by an entity graph. This is why BE-01 is sequenced *after* BE-02/BE-03 in the plan: those are zero-risk and buy time to do BE-01 module by module with tests. Migrate one module at a time and assert the response JSON is byte-identical before and after.

**Effort.** High (~5–8 days across all modules, but parallelisable and independently shippable per module).

---

### BE-02 — Read paths run inside read-write transactions
**S1 · PERSISTENCE · BP**

**Evidence.** `@Transactional` appears 133 times, applied at **class level** on every `*ServiceController` (e.g. `ParkingServiceController.java:43`, `BondItemServiceController`, `BranchServiceController.java:25`) and on 64 `*Resource` classes. **Zero** use `readOnly = true`.

Every GET therefore:
1. Opens a read-write transaction and holds a pooled connection in RW mode for the whole request.
2. Forces Hibernate to retain a **loaded-state snapshot** of every entity in the persistence context.
3. Runs **automatic dirty-checking** at flush, walking every field of every managed entity and comparing against its snapshot.

Combined with BE-01, a single `findAll()` of 5,000 rows leaves tens of thousands of entities managed, and the flush walks all of them — on a request that writes nothing. The snapshot also doubles the memory held per request.

**Fix.** Annotate read methods `@Transactional(readOnly = true)`. Hibernate then sets `FlushMode.MANUAL`, skips snapshot retention and skips dirty-checking entirely.

**Behaviour note.** BP for genuine read paths. Apply per-method (not by flipping the class-level annotation) so that any method that writes keeps its read-write semantics. Audit each candidate for incidental writes before annotating — several `get*` methods in this codebase call `save()` (e.g. lazy number backfills), and those must be left alone.

**Effort.** Low (~1–2 days). **Highest value-to-risk ratio in this audit.**

---

### BE-03 — Dead BLOB columns fetched on every row
**S1 · PERSISTENCE · BP\***

**Evidence.** `Byte[] image` is mapped on 5 entities — `Parking.java:83`, `VehicleEquipment.java:57`, `BondItem.java:96`, `Maintenance.java:86`, and `Storage` — and `@Lob byte[] logo` on `Company.java:76–77` and `SystemProfile.java:67`.

Two compounding problems:

1. **The column is fetched but never used.** Basic attributes default to eager. Every `SELECT` on these tables reads the BLOB off disk, ships it over the JDBC wire and materialises it in the JVM — then the mapper discards it. As established in §2, all 12 `setImage` call sites are commented out.
2. **`Byte[]` is the boxed form.** Each byte becomes a distinct `java.lang.Byte` object (~16 bytes of header plus a reference). A 1 MB image becomes roughly **1 million objects and ~20 MB of heap**. Across a 1,000-row result set this is catastrophic GC pressure on its own.

`Company.logo` *is* live (written at `SystemProfileResource.java:44`), but it is dragged in on every `User` load through the eager `Company` relation (BE-01) — i.e. on every authenticated request, because `getUser*` resolves a `User`.

**Fix.**
- `image` fields → `@Basic(fetch = FetchType.LAZY)` plus bytecode enhancement, or drop the mapping with `@Transient`. Given they are provably dead, `@Transient` is the cleaner option and leaves the column in place for later use.
- `Company.logo` and `SystemProfile.logo` → `@Basic(fetch = FetchType.LAZY)`, keeping the single legitimate read path working.
- Change `Byte[]` → `byte[]` on the entity fields.

**Behaviour note.** BP\* — precondition (no live read or write of `image`) verified across all 12 call sites. `Company.logo`'s one consumer is preserved. **Note:** lazy basic attributes require Hibernate bytecode enhancement to take effect; without it the annotation is silently ignored. Verify with SQL logging that the column disappears from the generated `SELECT`, and treat `@Transient` as the fallback for the dead fields.

**Effort.** Low (~0.5 day).

---

### BE-04 — No indexes on any queried column
**S1 · PERSISTENCE · BP**

**Evidence.** `@Index` / `indexes =` appears **0 times** across 81 entities. With `spring.jpa.hibernate.ddl-auto = update`, MySQL/InnoDB auto-creates indexes only for primary keys, unique constraints and foreign keys. Every other filter predicate is a full table scan.

The derived-query inventory shows exactly which columns are hot and unindexed:

| Predicate column | Query methods | Index status |
|---|---|---|
| `status` | `findAllByStatusIn` ×4, `findAllByStatusInAndBranch` ×2, `findAllByVehicleEquipmentAndStatusIn` ×4 | **none** |
| `created_date_time` | `findAllByCreatedDateTimeBetweenAndStatusIn` ×3 | **none** |
| `checked_in_date_time` | `findAllByCheckedInDateTimeBetweenAndStatusIn` ×3 | **none** |
| `checked_out_date_time` | `findAllByStatusInAndCheckedOutDateTimeBetween` ×4 | **none** |
| `collection_date_time` | native reports, `CollectionRepository.java:29–62` | **none** |
| `name` (LIKE) | `findAllByCompanyAndNameContainingIgnoreCase` ×4 | **none** — and unindexable as written |
| `active` | `findAllByCompanyAndActive` ×4, `findAllByBranchAndActive` ×4 | **none** |

The financial reports are the sharpest case: `CollectionRepository.java:29–44` filters `collection_date_time BETWEEN ...` then `GROUP BY` — a full scan plus a filesort on every report run, growing linearly and forever.

`...NameContainingIgnoreCase` compiles to `LIKE '%term%'`. A leading wildcard cannot use a B-tree index at all; this needs a `FULLTEXT` index or a prefix-match contract change to become sub-linear.

**Fix.** Add composite indexes via `@Table(indexes = {...})`, ordered to match the query predicates:

```java
@Table(name = "bond_items", indexes = {
    @Index(name = "ix_bond_items_status",        columnList = "status"),
    @Index(name = "ix_bond_items_created",       columnList = "created_date_time"),
    @Index(name = "ix_bond_items_branch_status", columnList = "branch_id, status")
})
```

Ship the equivalent `CREATE INDEX` statements as explicit DDL rather than relying on `ddl-auto` (see BE-10). Build the composite key order from the actual predicate, not alphabetically: `(branch_id, status)` serves `findAllByStatusInAndBranch`, whereas `(status, branch_id)` serves it far less well.

**Behaviour note.** BP. Indexes change plan selection only; result sets are identical. Adding an index to a large InnoDB table locks briefly — schedule for a maintenance window.

**Effort.** Medium (~2 days, including `EXPLAIN` verification per index).

---

### BE-05 — Unbounded `findAll()` on transactional tables
**S1 · PERSISTENCE · BP for the query shape, BC for true pagination**

**Evidence.** 37 `findAll()` call sites, several on tables that grow without bound:

- `ParkingServiceController.java:75` — every parking record ever created
- `BondItemServiceController.java:74` — every bond item
- `InvoiceReceivableServiceController.java:59` — every parking record, *as a subquery input* (see BE-06)
- `UserServiceController.java:391`, `:438`, `:447`

There is **no** `Pageable` or `Page<>` anywhere in the backend (0 occurrences), while the frontend paginates client-side in 59 templates at 10 rows per page. The full table is therefore loaded, hydrated through the eager graph, serialised to JSON and transferred — so the browser can render ten rows.

The codebase already acknowledges this: `UserService.java:24` carries the comment `// edit this to limit the number, for perfomance`, and `InvoiceReceivableServiceController.java:59` says `// Later change to find all by status, open, pending etc to avoid loading completed or canceled invoices`.

**Fix — two tiers.**

*Tier 1 (BP, recommended).* Replace `findAll()` with an equivalent filtered query where the caller immediately discards rows anyway. `InvoiceReceivableServiceController.java:59` is the clearest instance — it fetches all parkings only to join them to pending invoices. Narrowing the query to the statuses actually used produces an identical response with a fraction of the I/O.

*Tier 2 (BC, deferred).* Genuine server-side pagination. See **Appendix C-1** — this changes the response from an array to a page envelope and is out of scope under the stated constraint.

**Behaviour note.** Tier 1 is BP only where the narrowing is provably equivalent to the downstream filter. Prove it per call site; do not narrow speculatively.

**Effort.** Medium (~2 days for Tier 1).

---

### BE-06 — N+1 and `IN`-clause amplification in mappers
**S1 · PERSISTENCE · BP**

**Evidence — the worst single path**, `InvoiceReceivableServiceController.java:58–89`:

```java
List<Parking> parkings = parkingRepository.findAll();                            // (1) whole table
List<ParkingInvoiceReceivable> pirs =
        parkingInvoiceReceivableRepository.findAllByParkingIn(parkings);         // (2) IN (...) of N entities
...
for (InvoiceReceivable ir : invoiceReceivables) {
    ParkingInvoiceReceivable pir =
            parkingInvoiceReceivableRepository.findByInvoiceReceivable(ir);      // (3) one query per row
    ...
}
```

Three defects stacked:
1. Loads every parking row with its full eager graph and BLOB column.
2. Binds all of them into a single `IN (...)` predicate. Beyond the query-planner cost, a sufficiently large list will breach MySQL's `max_allowed_packet` and fail the request outright — a correctness cliff, not just a slowdown.
3. Re-queries per invoice for an association **already loaded into memory at step (2)**.

**Other confirmed N+1 sites in DTO mappers:**

| Location | Pattern |
|---|---|
| `BondItemServiceController.java:676` | `findAllByBondItem(bondItem)` inside the per-row mapper |
| `ParkingServiceController.java` (multiple: 94, 115, 161, 208, 242, 260, 290, 701, 786, 874, 973, 1056) | repository call inside a `for` over the result set |
| `ParkingReportResource.java:129, 220` | repository call inside the report row loop |
| `BondItemBillReceivableServiceController.java:55, 327, 394` | same pattern |
| `StorageServiceController`, `ShopSalesOrderServiceController`, `RestaurantSalesOrderServiceController`, `ProductServiceController`, `GrnServiceController` | same pattern |

**Fix.** Standard batch-then-map. Fetch the child collection once for the whole parent set, group it in memory, then map:

```java
Map<Long, List<BondItemBillReceivable>> byItem =
        bondItemBillReceivableRepository.findAllByBondItemIn(bondItems)
            .stream().collect(groupingBy(r -> r.getBondItem().getId()));
```

For step (2) above, chunk any `IN` list at ~1,000 elements regardless, as a standing safeguard.

**Behaviour note.** BP. Same rows, same order (preserve ordering explicitly where the current code relies on it), fewer round trips.

**Effort.** Medium-High (~4 days across all sites).

---

### BE-07 — User context re-resolved from the database on every reference
**S2 · PERSISTENCE · BP**

**Evidence.** `UserServiceController.java:655–662` and `:736–738`:

```java
public Long    getUserId(HttpServletRequest r)      { return userRepository.findByUsername(...).get().getId(); }
public User    getUser(HttpServletRequest r)        { return userRepository.findByUsername(...).get(); }
public Company getUserCompany(HttpServletRequest r) { return userRepository.findByUsername(...).get().getCompany(); }
public Branch  getUserBranch(HttpServletRequest r)  { return userRepository.findByUsername(...).get().getBranch(); }
```

Each call is a fresh query. Via BE-01 each one also drags in roles, privileges, company (with its logo BLOB), the branch list and the parent-branch chain.

These are called **254 times** across the codebase, concentrated in single request methods:

| Controller | Calls |
|---|---|
| `DiscountRequestServiceController` | 18 |
| `BondItemServiceController` | 16 |
| `ParkingServiceController` | 16 |
| `MaintenanceServiceController` | 14 |
| `StorageServiceController` | 13 |

Compounding it, **63 sites** use the double-lookup anti-pattern — re-fetching by ID an entity that was just returned:

```java
// BondItemServiceController.java:760
Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
```

That is two queries plus a full graph hydration to obtain an object the first query already held.

**Fix.**
1. Cache the resolved `User` per request (`@RequestScope` bean, or a `ThreadLocal` populated in the existing authorization filter). Within a Hibernate session the first-level cache already de-duplicates *entity* loads, but not the repeated `findByUsername` **query** execution — each is a fresh round trip.
2. Replace the 63 `findById(getUserX(request).getId())` sites with direct use of the already-resolved object.

**Behaviour note.** BP. Request-scoped caching matches the existing semantics exactly — the user cannot change mid-request.

**Effort.** Medium (~2 days).

---

### BE-08 — SQL logging at `DEBUG`/`TRACE` in the shipped configuration
**S2 · CONFIG · BP**

**Evidence.** `src/main/resources/application.properties`:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type=TRACE
```

There is **one** properties file — no profile separation — so this is what runs in production.

`hibernate.type=TRACE` logs every bound parameter of every statement, which means reflective formatting of each value. `format_sql=true` pretty-prints every statement. Against the N+1 volumes established in BE-01/BE-06, this generates enormous log volume and can dominate request time; synchronous appenders add lock contention across request threads on top.

**Fix.** Introduce `application-prod.properties` with these disabled, keeping the current values in a `dev` profile. Set `logging.level.org.hibernate.SQL=WARN` and remove the `type` logger entirely.

**Behaviour note.** BP. Logging verbosity only. As a bonus this stops SQL parameter values — which may include credentials or personal data — from reaching production logs.

**Effort.** Trivial (~1 hour). **Do this first.**

---

### BE-09 — No HTTP response compression
**S2 · TRANSPORT · BP**

**Evidence.** No `server.compression.*` properties are set, so Spring Boot leaves compression **off**. Given BE-05 (whole tables serialised to JSON), responses are large, highly repetitive JSON — the most compressible payload there is — sent raw.

**Fix.**

```properties
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/plain,text/css,application/javascript
server.compression.min-response-size=1024
```

**Behaviour note.** BP. Transparent at the HTTP layer; the client decompresses automatically and sees identical bytes. Angular's `HttpClient` needs no change.

**Effort.** Trivial (~15 minutes). Typically a **70–85 %** reduction in transferred bytes on these payloads.

---

### BE-10 — `ddl-auto=update` in production
**S2 · CONFIG · BP**

**Evidence.** `spring.jpa.hibernate.ddl-auto = update`.

Costs: Hibernate reads the full `INFORMATION_SCHEMA` and diffs it against 81 entity mappings at every boot, lengthening startup and holding DDL locks. It also silently prevents index management — `update` adds columns and tables but never drops or reshapes them, so BE-04's indexes cannot be reliably managed through it, and schema drift accumulates silently between environments.

**Fix.** Move to `validate` in production with an explicit migration tool (Flyway or Liquibase). Baseline the current schema as migration `V1`, then add BE-04's indexes as `V2`. Keep `update` in the dev profile.

**Behaviour note.** BP once the baseline matches the live schema. **Verify the baseline against production before switching** — `validate` fails fast on any mismatch, which is the point, but it will refuse to start if the baseline is wrong.

**Effort.** Medium (~2 days, mostly baseline verification).

---

### BE-11 — Lombok `@Data` on all 81 entities across a cyclic graph
**S2 · PERSISTENCE · BP**

**Evidence.** All 81 entities are annotated `@Data`, which generates `equals`, `hashCode` and `toString` over **all** fields. Only 94 `@EqualsAndHashCode.Exclude` markers exist against 232 relations, so most relations are included.

Consequences:

1. **`hashCode()` traverses the object graph.** `BondItem.hashCode()` recurses into `User` → `Company` → `Arrays.hashCode(logo)` over the whole BLOB → `branches` → each `Branch` → back to `Company`. On a cyclic graph this is at best O(graph) per call and at worst a `StackOverflowError`.
2. **Any `HashSet`/`HashMap` of entities becomes pathological.** `MainApplication.java:277,288` uses `List.contains(privilege)` inside nested loops — O(n²) invocations of a graph-traversing `equals`.
3. **`toString()` serialises the entire graph** into any log statement that interpolates an entity.
4. Hibernate's own identity semantics are subverted: a lazy proxy and its initialised entity can compare unequal, and mutable-field-based hashing breaks set membership after a field changes.

**Fix.** Replace `@Data` on entities with `@Getter` / `@Setter`, and define `equals`/`hashCode` explicitly on the business key or the surrogate ID only. Add `@ToString.Exclude` to every relation. The codebase has already started this — `BondItem.java:137–138` shows the correct pattern — it simply needs to be applied consistently.

**Behaviour note.** BP for the common case, but **audit before applying**: if any code path relies on `@Data`'s field-wise equality (e.g. deduplicating detached entities by value), changing to ID-based equality alters it. `MainApplication.java:277` is one such site and must be checked. Treat this as the one finding in Phase 2 needing per-site review.

**Effort.** Medium (~2–3 days including the audit).

---

### BE-12 — Per-request allocation in the authorization filter
**S3 · RUNTIME · BP**

**Evidence.** `CustomAuthorizationFilter.java`:

- Line 67: `Algorithm.HMAC256("secret".getBytes())` — the algorithm and its MAC key are rebuilt on every request. So is the `JWTVerifier` on line 68.
- Line 97: `new ObjectMapper().writeValue(...)` — `ObjectMapper` construction is genuinely expensive (it builds serialiser caches from scratch); it is designed to be a shared singleton.
- Line 90: `exception.printStackTrace()` — synchronous, unbuffered write to `System.err`, which is a global lock. Under concurrent failures this serialises request threads.

**Fix.** Hoist `Algorithm`, `JWTVerifier` and `ObjectMapper` to `static final` fields (all three are thread-safe once built). Replace `printStackTrace()` with the SLF4J logger already injected via `@Slf4j`.

**Behaviour note.** BP. Same verification, same error payload.

> **Non-performance observation, flagged for visibility only:** line 67 hard-codes the signing key as the literal `"secret"`, while `application.properties` separately defines `jwt.secret=javainuse`. Both are committed to the repository. This is outside the scope of this audit but warrants a separate ticket.

**Effort.** Trivial (~1 hour).

---

### BE-13 — Reports materialise full entities to read a handful of scalars
**S3 · PERSISTENCE · BP**

**Evidence.** `ParkingReportResource.java:112–138` loads complete `Parking` entities — full eager graph, `Byte[] image` column and all — then reads exactly five scalar values:

```java
registrationResponse.setChassisNo(parking.getChasisNo());
registrationResponse.setVehicleType(parking.getVehicleEquipmentType().getName());
registrationResponse.setRegisteredDate(parking.getCreatedDateTime().toString());
registrationResponse.setRegisteredBy(parking.getCreatedByUser().getNickname());
registrationResponse.setKeyStatus(parking.isHasKeys() ? "YES" : "NO");
```

The same shape appears in `BondItemReportResource.java:130`.

**Fix.** Use a Spring Data **interface projection** or a constructor-expression JPQL query selecting only those five columns. Hibernate then emits one narrow `SELECT` with two joins and skips the persistence context entirely. `CollectionRepository` already demonstrates the pattern with `IBillReceivableCollection`.

**Behaviour note.** BP. The response DTO is unchanged; only the retrieval path differs.

**Effort.** Low-Medium (~1.5 days for the report endpoints).

---

### BE-14 — No connection-pool or JDBC-batch configuration
**S3 · CONFIG · BP**

**Evidence.** No `spring.datasource.hikari.*` and no `hibernate.jdbc.batch_size` properties. Defaults apply: pool maximum 10 connections, no statement batching.

Two interacting problems:
- **Pool starvation.** `spring.jpa.open-in-view` is unset, so it defaults to **`true`**: the Hibernate session and its JDBC connection are held for the entire request, *including JSON serialisation*. With 10 connections and multi-megabyte responses (BE-05), throughput is capped well below what the database can serve, and the eleventh concurrent request blocks.
- **No batching.** Multi-row writes issue one `INSERT`/`UPDATE` round trip each.

**Fix.**

```properties
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

**Behaviour note.** Pool and batch settings are BP. **Disabling `open-in-view` is deliberately excluded** — with BE-01 unfixed it would convert lazy-load-after-close into `LazyInitializationException` at serialisation time, i.e. a behaviour change. Revisit it only *after* BE-01 lands, and size the pool before then. Tune `maximum-pool-size` against the database's own `max_connections` and the instance's core count rather than adopting 25 verbatim.

**Effort.** Low (~0.5 day plus load verification).

---

### BE-15 — Startup seeding is O(n²) with per-item queries
**S4 · RUNTIME · BP**

**Evidence.** `MainApplication.java:208–298`. `Object_` declares 52 objects and `Operation` 27, producing roughly 200 privilege permutations. For each, a separate `existsByName()` query (line 243). Then `roleRepository.findAll()` (line 273, eager privileges and company per role) and `privilegeRepository.findAll()` (line 285), each with a nested `destroyedPrivileges.contains(privilege)` — O(n²) calls to the graph-traversing `equals` from BE-11.

Startup-only, so it costs deploy time and health-check windows rather than user-facing latency.

**Fix.** Load all existing privilege names once into a `Set<String>`, then diff in memory and `saveAll()` the difference. Replace the `List.contains` scans with a `HashSet<Long>` of IDs.

**Behaviour note.** BP. Identical final database state.

**Effort.** Low (~0.5 day).

---

### BE-16 — Unused and development-only dependencies on the runtime classpath
**S4 · CONFIG · BP**

**Evidence.** In `pom.xml`:

- `spring-boot-devtools` at **default (compile) scope** — ships in the artefact. It installs a restart classloader and a file watcher, and disables template/static-resource caching. It should be `<optional>true</optional>`.
- `hibernate-envers` — `@Audited` appears **0 times**. The dependency registers Envers event listeners on the session factory for no benefit.
- `javafx-weaver-spring-boot-starter` — a JavaFX integration in a REST API.
- `commons-fileupload` 1.4 with `CommonsMultipartResolver` (`MainApplication.java:308`) — superseded by the servlet 3.0 multipart support Spring Boot configures natively.
- Spring Boot **2.2.5** (March 2020) is long past end of life, as is `MySQL5InnoDBDialect` for any modern MySQL.

**Fix.** Mark devtools optional; remove Envers, javafx-weaver and commons-fileupload. Plan a Spring Boot upgrade separately — that is a project, not a fix.

**Behaviour note.** BP for the dependency removals (verify no transitive dependency is relied upon). The Boot upgrade is **BC** and out of scope.

**Effort.** Low (~1 day) for removals.

---

### BE-17 — Unbounded multipart limits
**S4 · CONFIG · BP**

**Evidence.**

```properties
spring.servlet.multipart.max-file-size=-1
spring.servlet.multipart.max-request-size=-1
server.tomcat.max-swallow-size=-1
spring.servlet.multipart.location= /
```

Unlimited upload size with the temp location set to the filesystem **root**. A single large upload can exhaust the root partition or heap. `resolve-lazily=true` mitigates but does not bound this.

**Fix.** Set concrete limits aligned with the 50 MB already configured at `MainApplication.java:311`, and point `location` at a dedicated temp directory.

**Behaviour note.** BP provided the limit is at or above the largest legitimate upload. 50 MB matches the existing resolver setting.

**Effort.** Trivial (~15 minutes).

---

## 5. Frontend findings

### FE-01 — 4.67 MB global script bundle, with jQuery included three times
**S1 · BUNDLE · BP**

**Evidence.** `angular.json` `scripts[]` produces `dist/orbix-business/browser/scripts-K3ZW7QOW.js` at a **measured 4,668.7 KB**. Contents, verified by scanning the built file:

| Entry | Note |
|---|---|
| `jquery/dist/jquery.min.js` | **listed twice** — lines 60 and 71 of `angular.json` |
| `jquery-ui-dist/jquery-ui.min.js` | full build; embeds a second jQuery (v1.9.1) |
| `datamaps/dist/datamaps.all.hires.min.js` | the **hires** variant — world topology, the single largest entry |
| `d3/d3.min.js` | v3.5.17 (2016) |
| `fullcalendar/dist/fullcalendar.min.js` + `moment/moment.js` | |
| `pdfmake/build/pdfmake.min.js` + `pdfmake/build/vfs_fonts.js` | `vfs_fonts` is base64-encoded font binaries |
| `popper.js`, `bootstrap`, `jquery-knob`, `topojson`, `dropzone`, `select2` | |

Scanning the built bundle confirms **`jQuery v3.4.1` twice and `jQuery v1.9.1` once**.

Anything in `scripts[]` is concatenated verbatim, **never tree-shaken**, and loaded on every page regardless of route. `angular.json` sets the initial budget to `maximumWarning: 7mb` / `maximumError: 10mb` — roughly fourteen times Angular's default — which is how this passed CI.

**Fix, in order of value:**
1. Delete the duplicate `jquery` entry (line 71). *Free.*
2. Swap `datamaps.all.hires.min.js` for the standard resolution build, or move datamaps to a dynamic `import()` in the one map component. Largest single saving.
3. Remove `pdfmake` and `vfs_fonts` from `scripts[]` — they are **already imported as modules** by 68 components (FE-02), so the global copy is pure duplication.
4. Move `fullcalendar`, `dropzone`, `select2` and `jquery-knob` to dynamic imports in the components that use them.
5. Once the above land, tighten the budgets so the size cannot silently regress.

**Behaviour note.** BP provided each library remains available where it is actually used. These are `window`-global libraries, so verify each consumer after moving it — this is mechanical but must be done per library, not in one sweep.

**Effort.** Medium (~3 days).

---

### FE-02 — pdfmake and its font payload imported by 68 components
**S1 · BUNDLE · BP**

**Evidence.** `grep` across `src/app` finds **68** components importing `pdfmake` and **62** calling `require('pdfmake/build/vfs_fonts.js')` — at **module scope**, e.g. `bond-item-billing.component.ts:12,21`:

```typescript
import * as pdfMake from 'pdfmake/build/pdfmake';
...
var pdfFonts = require('pdfmake/build/vfs_fonts.js');
```

Module-scope import means the font payload is pulled into the chunk graph whenever any of those 62 routes is reached — and the measured `chunk-MDJXAUWA.js` at **2,193.5 KB** is consistent with the shared pdfmake/vfs chunk. This is *in addition to* the copy in the global bundle (FE-01).

**Fix.** Convert to a dynamic import inside the print handler, so the cost is paid only when a user actually generates a PDF:

```typescript
async printReceipt() {
  const [pdfMake, pdfFonts] = await Promise.all([
    import('pdfmake/build/pdfmake'),
    import('pdfmake/build/vfs_fonts')
  ]);
  (pdfMake as any).vfs = (pdfFonts as any).pdfMake.vfs;
  // ... unchanged
}
```

**Behaviour note.** BP. The function becomes `async`, which is internal — the PDF produced is identical. Callers that already `await` are unaffected; check the handful that do not.

**Effort.** Medium (~2 days across 68 files, largely mechanical).

---

### FE-03 — `PreloadAllModules` defeats route-level code splitting
**S1 · BUNDLE · BP**

**Evidence.** `app.config.ts:20`:

```typescript
withPreloading(PreloadAllModules),  // comment this line for enable lazy-loading
```

The codebase does the hard part correctly — **122** `loadChildren`/`loadComponent` declarations producing **133** lazy chunks. `PreloadAllModules` then downloads every one of them immediately after bootstrap, so the user pays for all **11.4 MB of JavaScript** to reach any single screen. The inline comment shows this was a known trade-off.

The cost is amplified by `env.js`, which points the app at `http://51.20.119.45:8080` — plain HTTP, therefore **HTTP/1.1**, therefore a six-connections-per-host limit while fetching 133 files.

**Fix.** Replace with `QuicklinkStrategy`, or a custom strategy that preloads only routes flagged in their route `data`, or drop preloading entirely. A reasonable default preloads the three or four most-used routes and lazy-loads the rest on demand.

**Behaviour note.** BP. Navigation targets are unchanged; only fetch timing moves. First navigation to a cold route incurs a chunk download — mitigated by preloading the common routes and by FE-01/FE-02 shrinking the chunks.

**Effort.** Low (~0.5 day).

---

### FE-04 — Search filter JSON-stringifies every row, and ships a `debugger` statement
**S1 · CLIENT-RUNTIME · BP**

**Evidence.** `src/app/custom-pipes/search-filter.ts`, used in **59** templates:

```typescript
transform(value: any, args?: any): any {
    if (!value) return null;
    if (!args)  return value;
    args = args.toLowerCase()
    debugger                                    // ← line 16
    return value.filter(function(item: any) {
        return JSON.stringify(item)             // ← line 18
            .toLowerCase()
            .includes(args);
    });
}
```

Two distinct defects:

1. **`debugger` on line 16 is in the shipped production bundle** — confirmed present in `dist/orbix-business/browser/chunk-DGTQFPG3.js`. In any browser with DevTools open this **halts execution entirely** every time a user types in a search box. For a developer or a support engineer debugging a customer issue, the application appears frozen.
2. **`JSON.stringify(item)` per row, per keystroke.** Full serialisation of every object in the dataset — and because the backend returns whole tables (BE-05), that dataset is the entire table. There is no debounce on the input, so this runs on every character. `.toLowerCase()` then allocates a second full-size string per row.

**Fix.**
1. Delete line 16. *Immediate.*
2. Replace `JSON.stringify` with a match over a known field list, or memoise a lowercased search string per row and invalidate it when the row changes.
3. Debounce the search input (150–250 ms).

**Behaviour note.** Removing `debugger` is BP. Debouncing is BP. **Changing `JSON.stringify` to a field list is BC** — it narrows which fields match a query, which users may notice. The behaviour-preserving version is to keep matching over all fields but cache the serialised form per row rather than recomputing it; adopt that unless a contract change is signed off.

**Effort.** Low (~1 day).

---

### FE-05 — Permission checks decode a JWT on every change-detection pass
**S1 · CLIENT-RUNTIME · BP**

**Evidence.** Two parallel implementations, both expensive.

`menu_items.ts:1122–1140`:

```typescript
export function grant(privileges: string[]): boolean {
    const currentUser = JSON.parse(localStorage.getItem('current-user')!);   // sync I/O + parse
    ...
    const decodedToken = new JwtHelperService().decodeToken(currentUser.access_token);  // alloc + base64 + parse
    const userPrivileges = decodedToken.privileges as string[];
    return privileges.some(privilege => userPrivileges.includes(privilege));  // linear scan
}
```

`auth.service.ts:215–245` is worse — `grant()` loops over the requested privileges calling `checkPrivilege()`, and **each** `checkPrivilege()` does its own `localStorage` read, `JSON.parse`, and `decodeToken`. So `grant(['A','B','C'])` performs three complete JWT decodes.

Per invocation: a synchronous `localStorage` read (blocks the main thread), a `JSON.parse`, an object allocation, a base64 decode and a second `JSON.parse` of the token payload, then a linear scan of the privileges array.

This is invoked from **30 template bindings**. Combined with FE-06 (no `OnPush`), Angular re-evaluates every one of them on **every change-detection cycle** — every click, keystroke, mouse event, HTTP response and timer.

Additionally, `menu_items.ts` calls `grant([...])` **27 times at module scope** inside `export const menuItems = [...]`. Beyond the boot cost, because this is a `const` evaluated once at import, the menu does not reflect a privilege change without a full page reload.

**Fix.** Decode the token **once** on login and on token refresh, and store the privileges in a `Set<string>` on `AuthService`. `grant()` becomes a set lookup — the same result, several orders of magnitude cheaper:

```typescript
private privileges = new Set<string>();
grant(required: string[]): boolean { return required.some(p => this.privileges.has(p)); }
```

**Behaviour note.** BP for the two `grant` implementations — identical boolean for identical input. **The `menuItems` module-scope evaluation needs care:** moving it into a function that is called per render would *fix* the stale-menu-after-relogin bug, which is technically a behaviour change (an improvement, but a change). Under the stated constraint, keep the current once-at-import timing and change only the cost of each call; raise the staleness bug as a separate defect ticket.

**Effort.** Low (~1 day).

---

### FE-06 — No `OnPush` and no `trackBy` anywhere
**S2 · CLIENT-RUNTIME · BP**

**Evidence.**

- **160** components; **0** use `ChangeDetectionStrategy.OnPush`.
- **311** `*ngFor` loops; **0** use `trackBy`.

With default change detection, *every* browser event triggers a full traversal of the component tree, re-evaluating every template binding — including the 30 `grant()` calls of FE-05 and the 42 `hasError()` calls.

Without `trackBy`, Angular's differ identifies list items by object identity. Because these components **reassign the array** on every refresh (see FE-07), every item is a new object reference, so Angular **destroys and recreates the entire DOM subtree** on every refresh. On a 10-row paginated view the visible cost is modest; on the larger grids, and during sort or filter, it is the dominant cost.

`app.config.ts:17` does enable `provideZoneChangeDetection({ eventCoalescing: true })`, which helps at the margins, but coalescing events does not reduce the cost of each pass.

**Fix.**
1. Add `trackBy: trackById` to the 311 `*ngFor` loops. Every row model has an `id`. This alone eliminates the destroy/recreate cycle.
2. Adopt `OnPush` incrementally, starting with leaf presentational components and the largest grids.

**Behaviour note.** `trackBy` is BP — pure rendering optimisation. **`OnPush` is BP only if the component's inputs are treated immutably.** These components mutate `this.someArray` in place in several places, which `OnPush` would stop rendering. Adopt `OnPush` **per component, with verification**, never as a bulk edit. This is the single most regression-prone item in this audit — sequence it last and gate it behind visual verification of each screen.

**Effort.** `trackBy` — Low (~1.5 days). `OnPush` — High (~5+ days, incremental).

---

### FE-07 — Sequential request waterfalls
**S2 · CLIENT-RUNTIME · BP**

**Evidence.** **1,207** `await this.*` expressions across page components; **0** uses of `Promise.all`. Independent requests execute strictly one after another, so page load time is the *sum* of every request's latency rather than the maximum.

The pattern in `bond-item-billing.component.ts:230–262` is representative: save, then refetch the list, then log — each awaited in turn.

**Fix.** Where awaited calls are genuinely independent, batch them:

```typescript
const [zones, types, customers] = await Promise.all([
  this.getBondZones(), this.getBondItemTypes(), this.getCustomers()
]);
```

**Behaviour note.** BP **only for genuinely independent calls.** Many of these awaits are sequential by necessity (the second depends on the first's result) or order-sensitive (save-then-refetch). Each site needs review; do not mass-convert. Target the `ngOnInit` reference-data loads first, where independence is usually obvious.

**Effort.** Medium (~3 days, site by site).

---

### FE-08 — 1,491 `console.log` calls in the production bundle
**S2 · CLIENT-RUNTIME · BP**

**Evidence.** **1,595** `console.log` calls in source; **1,491** survive into the production build (measured across the built `chunk-*.js` files). Angular's optimiser does not strip them.

The dominant pattern is `console.log(data)` in HTTP callbacks — logging **entire API response payloads**. Given BE-05 returns whole tables, each call hands a multi-megabyte array to the console. When DevTools is open the console **retains a live reference to every logged object**, so those arrays are never garbage-collected: a long support session on a data-heavy screen accumulates unbounded memory. Even with DevTools closed, argument evaluation and the call itself are not free at this volume.

Highest concentrations: `select-workshop.component.ts` (50), `lpo.component.ts` (40), `bond-item-billing.component.ts` (39), `shop-grn.component.ts` (38).

**Fix.** Strip `console` calls from production builds via a build-time transform, or route logging through a service that no-ops when `environment.production` is true. A build-level strip is preferable — it removes the argument evaluation too, not just the output.

**Behaviour note.** BP for users. Developers lose console output in production builds, which is the intent; keep them in the development configuration.

**Effort.** Low (~0.5 day for the build-level strip).

---

### FE-09 — No compression or cache headers on static assets
**S2 · TRANSPORT · BP**

**Evidence.** `src/.htaccess` contains **only** rewrite rules for SPA routing. There is no `mod_deflate`/`mod_brotli` block and no `Cache-Control`/`Expires` block.

So the measured **4.67 MB** script bundle and **391 KB** stylesheet are served **uncompressed**, and although `outputHashing: "all"` produces content-hashed filenames — which makes them safe to cache for a year — no cache headers are sent, so browsers revalidate on every visit.

**Fix.** Add to `.htaccess`:

```apache
<IfModule mod_deflate.c>
  AddOutputFilterByType DEFLATE text/html text/css text/plain \
      application/javascript application/json image/svg+xml
</IfModule>
<IfModule mod_headers.c>
  # Hashed bundles are immutable — safe to cache for a year
  <FilesMatch "\.(js|css|woff2?|ttf|eot|svg)$">
    Header set Cache-Control "public, max-age=31536000, immutable"
  </FilesMatch>
  # index.html must always revalidate, or clients never see new hashes
  <FilesMatch "^index\.html$">
    Header set Cache-Control "no-cache, must-revalidate"
  </FilesMatch>
</IfModule>
```

**Behaviour note.** BP, **conditional on the `index.html` exclusion above.** Without it, users would be pinned to a stale build for a year. Prefer brotli where the server supports it.

**Effort.** Trivial (~1 hour). One of the best returns in this document.

---

### FE-10 — Render-blocking third-party scripts in `index.html`
**S2 · TRANSPORT · BP**

**Evidence.** The built `index.html` loads, with neither `async` nor `defer`:

```html
<script src="https://maps.googleapis.com/maps/api/js?key=..."></script>
<script src="https://cdn.ckeditor.com/4.7.0/full-all/ckeditor.js"></script>
```

Both block HTML parsing until downloaded and executed, on **every** page — including the login screen. `full-all` is CKEditor's largest distribution, and CKEditor 4.7.0 dates from 2017. Both introduce a third-party origin on the critical path: a slow CDN response stalls the entire application.

**Fix.** Load both lazily from the components that need them (the maps view and the editor view respectively), or at minimum add `defer`. Prefer a pinned, self-hosted CKEditor build over a 2017 CDN version.

**Behaviour note.** BP if the consuming components `await` the loader before use. Verify the maps component does not assume `window.google` exists at construction time.

**Effort.** Low (~1 day).

---

### FE-11 — Unoptimised image and font assets
**S3 · BUNDLE · BP**

**Evidence.** `dist/orbix-business/browser/` ships 3.9 MB of images and 1.5 MB of fonts:

| Asset | Size | Note |
|---|---|---|
| `img/logo/Untitled-2.ai` | **1,183.3 KB** | **An Adobe Illustrator source file**, unusable by any browser |
| `media/fontawesome-webfont-*.svg` | 434.0 KB | SVG font format — obsolete, needed only by iOS ≤ 4.1 |
| `img/login-bg.jpg` | 344.4 KB | On the login screen, so on the critical path for every user |
| `img/app/snow.jpg` | 322.7 KB | |
| `img/feed/new-york-location.jpg` | 256.3 KB | Demo asset from the purchased template |
| `img/logo/az_logo_full.png` | 252.7 KB | |
| `media/fontawesome-webfont-*.eot/.ttf/.svg` | ~758 KB total | Legacy formats; `woff2` alone covers every supported browser |

**Fix.** Delete `Untitled-2.ai` and the unused template demo images. Serve `woff2` only. Compress the remaining JPEGs and add WebP/AVIF with `<picture>` fallbacks.

**Behaviour note.** BP provided each asset is confirmed unreferenced before deletion (`grep` the source for each filename). Font-format pruning is BP for all currently supported browsers.

**Effort.** Low (~1 day).

---

### FE-12 — Unreleased `router.events` subscriptions
**S3 · CLIENT-RUNTIME · BP**

**Evidence.** **63** `.subscribe(` calls and **0** `ngOnDestroy` implementations across 160 components.

Most are `HttpClient` calls, which complete after one emission and self-unsubscribe — not a leak. The real exposures are the five `router.events` subscriptions, which never complete:

| File | Line |
|---|---|
| `mail-list.component.ts` | 35 |
| `mail.component.ts` | 32 |
| `breadcrumb.component.ts` | 26 |
| `menu.component.ts` | 44 |
| `sidebar.component.ts` | 46 |

`breadcrumb`, `menu` and `sidebar` live for the application's lifetime, so their subscriptions are effectively harmless. The two **mail** components are routed page components: each navigation to them registers another permanent subscription. Navigate in and out twenty times and the handler runs twenty times per navigation, growing without bound for the session.

**Fix.** Add `takeUntilDestroyed()` (Angular 16+, and this project is on 18), or implement `ngOnDestroy` with an explicit `unsubscribe()`.

**Behaviour note.** BP. Removes duplicate handler invocations that were never intended.

**Effort.** Trivial (~2 hours).

---

### FE-13 — Production optimisation partially disabled
**S3 · BUNDLE · BP**

**Evidence.** `angular.json`, production configuration:

```json
"optimization": { "styles": { "minify": true, "inlineCritical": false } }
```

`inlineCritical: false` disables critical-CSS inlining, so the **391 KB** stylesheet is fully render-blocking before first paint. `angular.json` also sets `"cache": { "enabled": false }` at the CLI level, disabling the build cache and slowing every local and CI build.

**Fix.** Set `inlineCritical: true` and re-enable the CLI cache. Verify the styles render correctly with critical inlining — it was likely disabled to work around a specific issue, so confirm what that was before flipping it.

**Behaviour note.** BP if visual verification passes. The disabled flag suggests a prior problem; treat re-enabling as a change requiring a visual check, not a blind edit.

**Effort.** Low (~0.5 day including verification).

---

## 6. Repository and build hygiene

### HY-01 — 2.3 GB `.git` directory
**S3 · HYGIENE · BP**

**Evidence.**

- `.git` measures **2.3 GB** against 2,813 tracked files.
- **773** tracked files sit under `node_modules/`, `target/` or `.metadata/` — **635** under `target/` alone (compiled build output) and **102** under `Orbix-Business-API/.metadata` (Eclipse workspace state).
- Two binary archives are committed: `Web Template/themeforest-...zip` (2.6 MB) and `Web Template/azimuth.zip` (1.6 MB).
- There is **no root `.gitignore`**.

Committed build output is re-added as a new blob on every rebuild, which is how a codebase of ~108 K lines of source reached 2.3 GB of history. The cost is developer time: clone, fetch, checkout and branch-switch are all slow.

**Fix.**
1. Add a root `.gitignore` covering `node_modules/`, `target/`, `dist/`, `.metadata/`, `*.zip`.
2. `git rm -r --cached` the tracked build output and commit the removal.
3. For the historical bulk, a history rewrite (`git filter-repo`) would reclaim most of the 2.3 GB — but it rewrites every commit hash and requires every clone to be re-cloned. **Coordinate with the whole team or skip it**; steps 1–2 stop the growth without that disruption.

**Behaviour note.** BP for the application. Step 3 is disruptive to developers and needs team sign-off.

**Effort.** Low for steps 1–2 (~2 hours). Step 3 is a coordinated team operation.

---

### HY-02 — No production profile separation
**S3 · CONFIG · BP**

**Evidence.** A single `application.properties` with development values (BE-08's SQL tracing, `ddl-auto=update`, a `localhost` datasource URL and a committed database password) and no `application-{profile}.properties`.

**Fix.** Split into `application.properties` (shared), `application-dev.properties` and `application-prod.properties`. Source the datasource URL, credentials and `jwt.secret` from environment variables in production.

**Behaviour note.** BP provided the prod profile reproduces current production behaviour exactly, minus the logging. This finding is a **prerequisite for BE-08 and BE-10** — sequence it first.

**Effort.** Low (~1 day).

---

## 7. Remediation plan

Phases are ordered by **return per unit of risk**, not by severity. Phase 1 is almost entirely configuration and can ship in days; Phase 4 is the deep fix and needs real test coverage behind it.

### 7.1 Phase 1 — Configuration and quick wins
**~3 days · All BP · No application code touched except FE-04's one-line deletion**

| ID | Action | Effort |
|---|---|---|
| HY-02 | Split dev/prod Spring profiles | 1 d |
| BE-08 | Disable SQL `DEBUG`/`TRACE` logging in prod | 1 h |
| BE-09 | Enable `server.compression` | 15 m |
| FE-09 | Add gzip + cache headers to `.htaccess` | 1 h |
| FE-04.1 | Delete the `debugger` statement (`search-filter.ts:16`) | 5 m |
| FE-01.1 | Delete the duplicate jQuery entry (`angular.json:71`) | 5 m |
| BE-17 | Bound multipart upload limits | 15 m |
| BE-14 | Hikari pool + JDBC batch settings (**not** `open-in-view`) | 4 h |
| HY-01 | Root `.gitignore`; untrack build output | 2 h |

**Expected:** 70–85 % reduction in transferred bytes on both tiers; meaningful backend latency reduction from logging alone; the DevTools freeze eliminated.

---

### 7.2 Phase 2 — Bundle and payload
**~2 weeks · All BP, but FE-02 and FE-01 need per-consumer verification**

| ID | Action | Effort |
|---|---|---|
| FE-03 | Replace `PreloadAllModules` with a selective strategy | 0.5 d |
| FE-01 | Shrink `scripts[]`: datamaps, pdfmake, fullcalendar, dropzone, select2 | 3 d |
| FE-02 | Dynamic-import pdfmake across 68 components | 2 d |
| FE-08 | Strip `console.*` from production builds | 0.5 d |
| FE-10 | Defer/lazy-load Google Maps and CKEditor | 1 d |
| FE-11 | Remove `Untitled-2.ai` and unused assets; `woff2` only; compress images | 1 d |
| FE-12 | `takeUntilDestroyed()` on the 5 router subscriptions | 2 h |
| FE-13 | Re-enable `inlineCritical` and the CLI build cache | 0.5 d |
| BE-12 | Hoist `ObjectMapper`/`Algorithm`/`JWTVerifier`; use the logger | 1 h |
| BE-16 | Remove Envers, javafx-weaver, commons-fileupload; devtools → optional | 1 d |

**Expected:** initial JS payload down from 11.4 MB to an estimated 1.5–2.5 MB; FCP improved by an estimated 3–6 s on a typical connection.

---

### 7.3 Phase 3 — Query and client hot paths
**~2 weeks · Mostly BP; BE-11 and FE-07 need per-site review**

| ID | Action | Effort |
|---|---|---|
| BE-02 | `@Transactional(readOnly = true)` on read methods | 2 d |
| BE-03 | Lazy/transient BLOB columns; `Byte[]` → `byte[]` | 0.5 d |
| BE-04 | Composite indexes on hot predicates | 2 d |
| BE-07 | Request-scoped user context; remove 63 double lookups | 2 d |
| FE-05 | Decode the JWT once; `Set`-based `grant()` | 1 d |
| FE-04.2 | Debounce search input; memoise the row-serialisation | 1 d |
| FE-06.1 | `trackBy` on all 311 `*ngFor` loops | 1.5 d |
| BE-13 | Projections for report endpoints | 1.5 d |
| BE-15 | Batch the startup privilege seeding | 0.5 d |
| BE-11 | `@Data` → `@Getter`/`@Setter` + explicit equality (**audit first**) | 2–3 d |

**Expected:** the largest single backend improvement in the plan, driven by BE-02 and BE-04.

---

### 7.4 Phase 4 — Fetch strategy and remaining hot paths
**~3 weeks · BP but invasive — requires response-equivalence tests before starting**

| ID | Action | Effort |
|---|---|---|
| BE-01 | `EAGER` → `LAZY` + targeted `@EntityGraph`, module by module | 5–8 d |
| BE-06 | Eliminate N+1 in mappers; chunk `IN` clauses | 4 d |
| BE-05 | Tier-1 query narrowing on `findAll()` sites | 2 d |
| BE-10 | `ddl-auto=validate` + Flyway baseline | 2 d |
| FE-07 | `Promise.all` where calls are independent | 3 d |
| FE-06.2 | `OnPush` adoption, incremental and verified per component | 5 d+ |

**Precondition.** Phase 4 changes how data is *loaded* while requiring the output to stay identical. Before starting, capture golden-file responses from the highest-traffic endpoints and assert byte equality after each module's migration. Without that harness, BE-01 is the most likely item in this plan to cause a regression.

---

## 8. Verification plan

The estimates in this document are derived from static analysis. Confirm them before and after each phase.

**Backend**
1. Enable `p6spy` or Hibernate statistics in a staging profile and record **queries per endpoint** for the top 20 endpoints. This is the primary metric for BE-01, BE-06 and BE-07 — the goal is an absolute count that does not scale with row count.
2. `EXPLAIN` every query touched by BE-04 before and after, confirming the index is chosen and `rows` examined drops.
3. Load-test the three worst endpoints (`/parkings`, `/bond_items`, the invoice-receivable path) at realistic row counts. Record p50/p95/p99 and bytes transferred.
4. Watch GC logs during a `findAll()` on a large table before and after BE-03 — the `Byte[]` boxing should show as a step change in allocation rate.

**Frontend**
1. Lighthouse on the login and two heaviest data screens, before and after each phase. Track FCP, LCP, TBT and total transfer.
2. `source-map-explorer` on the production build to confirm bundle composition after FE-01/FE-02.
3. Chrome Performance profile while typing in a search box on the largest grid — before and after FE-04/FE-05/FE-06. Look for the change-detection flame pattern.
4. Memory timeline across twenty navigations in and out of the mail screens to confirm FE-12.

**Regression safety**
- Golden-file response tests on the top 20 endpoints — **mandatory before Phase 4**.
- Visual verification per screen for every `OnPush` conversion (FE-06.2).

---

## 9. Risk register

| Risk | Finding | Mitigation |
|---|---|---|
| `LazyInitializationException` at serialisation after the `EAGER` → `LAZY` switch | BE-01 | Per-module migration with `@EntityGraph` on each dereferencing query; golden-file tests; keep `open-in-view` enabled until complete |
| `@Transactional(readOnly)` applied to a method that incidentally writes | BE-02 | Annotate per method, never at class level; audit each for `save()` calls |
| `@Basic(LAZY)` silently ignored without bytecode enhancement | BE-03 | Verify via SQL log that the column leaves the `SELECT`; fall back to `@Transient` for the dead fields |
| `ddl-auto=validate` refuses to start on schema drift | BE-10 | Verify the Flyway baseline against the live schema in staging first |
| Changing entity `equals`/`hashCode` alters dedup logic | BE-11 | Audit `MainApplication.java:277` and all collection membership sites before applying |
| `OnPush` stops rendering where arrays are mutated in place | FE-06.2 | Per-component conversion with visual verification; never a bulk edit |
| Moving a global script breaks a `window`-dependent consumer | FE-01, FE-10 | Move one library at a time; verify each consumer |
| Cache headers pin users to a stale build | FE-09 | Exclude `index.html` from long-lived caching (included in the snippet above) |
| Index creation locks a large table | BE-04 | Schedule in a maintenance window; use online DDL where the MySQL version supports it |
| History rewrite invalidates every clone | HY-01 | Optional step; requires team-wide coordination, or skip |

---

## Appendix A — Findings index

| ID | Severity | Category | Behaviour | Phase | Title |
|---|---|---|---|---|---|
| BE-01 | S1 | PERSISTENCE | BP | 4 | Universal `EAGER` fetching creates a self-referential object graph |
| BE-02 | S1 | PERSISTENCE | BP | 3 | Read paths run inside read-write transactions |
| BE-03 | S1 | PERSISTENCE | BP\* | 3 | Dead BLOB columns fetched on every row |
| BE-04 | S1 | PERSISTENCE | BP | 3 | No indexes on any queried column |
| BE-05 | S1 | PERSISTENCE | BP / BC | 4 / C-1 | Unbounded `findAll()` on transactional tables |
| BE-06 | S1 | PERSISTENCE | BP | 4 | N+1 and `IN`-clause amplification in mappers |
| BE-07 | S2 | PERSISTENCE | BP | 3 | User context re-resolved from the database on every reference |
| BE-08 | S2 | CONFIG | BP | 1 | SQL logging at `DEBUG`/`TRACE` in the shipped configuration |
| BE-09 | S2 | TRANSPORT | BP | 1 | No HTTP response compression |
| BE-10 | S2 | CONFIG | BP | 4 | `ddl-auto=update` in production |
| BE-11 | S2 | PERSISTENCE | BP | 3 | Lombok `@Data` on all 81 entities across a cyclic graph |
| BE-12 | S3 | RUNTIME | BP | 2 | Per-request allocation in the authorization filter |
| BE-13 | S3 | PERSISTENCE | BP | 3 | Reports materialise full entities to read a handful of scalars |
| BE-14 | S3 | CONFIG | BP | 1 | No connection-pool or JDBC-batch configuration |
| BE-15 | S4 | RUNTIME | BP | 3 | Startup seeding is O(n²) with per-item queries |
| BE-16 | S4 | CONFIG | BP | 2 | Unused and development-only dependencies on the runtime classpath |
| BE-17 | S4 | CONFIG | BP | 1 | Unbounded multipart limits |
| FE-01 | S1 | BUNDLE | BP | 1 / 2 | 4.67 MB global script bundle, with jQuery included three times |
| FE-02 | S1 | BUNDLE | BP | 2 | pdfmake and its font payload imported by 68 components |
| FE-03 | S1 | BUNDLE | BP | 2 | `PreloadAllModules` defeats route-level code splitting |
| FE-04 | S1 | CLIENT-RUNTIME | BP / BC | 1 / 3 | Search filter JSON-stringifies every row; ships a `debugger` |
| FE-05 | S1 | CLIENT-RUNTIME | BP | 3 | Permission checks decode a JWT on every change-detection pass |
| FE-06 | S2 | CLIENT-RUNTIME | BP | 3 / 4 | No `OnPush` and no `trackBy` anywhere |
| FE-07 | S2 | CLIENT-RUNTIME | BP | 4 | Sequential request waterfalls |
| FE-08 | S2 | CLIENT-RUNTIME | BP | 2 | 1,491 `console.log` calls in the production bundle |
| FE-09 | S2 | TRANSPORT | BP | 1 | No compression or cache headers on static assets |
| FE-10 | S2 | TRANSPORT | BP | 2 | Render-blocking third-party scripts in `index.html` |
| FE-11 | S3 | BUNDLE | BP | 2 | Unoptimised image and font assets |
| FE-12 | S3 | CLIENT-RUNTIME | BP | 2 | Unreleased `router.events` subscriptions |
| FE-13 | S3 | BUNDLE | BP | 2 | Production optimisation partially disabled |
| HY-01 | S3 | HYGIENE | BP | 1 | 2.3 GB `.git` directory |
| HY-02 | S3 | CONFIG | BP | 1 | No production profile separation |

---

## Appendix B — Phase 1 configuration reference

Consolidated, copy-ready. Every block here is behaviour-preserving.

**`application-prod.properties`** (new)

```properties
# --- BE-08: silence SQL tracing -------------------------------------------
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
logging.level.org.hibernate.SQL=WARN
# (the org.hibernate.type logger is removed entirely)

# --- BE-09: response compression ------------------------------------------
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/plain,text/css,application/javascript
server.compression.min-response-size=1024

# --- BE-14: pool and batching ---------------------------------------------
# Size against the DB's max_connections and the instance core count.
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
# NOTE: open-in-view is deliberately left at its default (true).
#       Disabling it before BE-01 lands would surface
#       LazyInitializationException during JSON serialisation.

# --- BE-17: bound uploads (matches MainApplication.java:311) --------------
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
spring.servlet.multipart.location=${java.io.tmpdir}

# --- HY-02: externalised secrets ------------------------------------------
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

**`src/.htaccess`** — append to the existing rewrite rules (FE-09)

```apache
<IfModule mod_deflate.c>
  AddOutputFilterByType DEFLATE text/html text/css text/plain \
      application/javascript application/json image/svg+xml
</IfModule>

<IfModule mod_headers.c>
  # Content-hashed bundles are immutable
  <FilesMatch "\.(js|css|woff2?|ttf|eot|svg)$">
    Header set Cache-Control "public, max-age=31536000, immutable"
  </FilesMatch>
  # index.html must revalidate, or clients never pick up new hashes
  <FilesMatch "^index\.html$">
    Header set Cache-Control "no-cache, must-revalidate"
  </FilesMatch>
</IfModule>
```

**Root `.gitignore`** (new — HY-01)

```gitignore
node_modules/
dist/
target/
.metadata/
*.zip
*.log
.angular/
```

---

## Appendix C — Excluded: contract-changing optimisations

These would deliver substantial gains but **violate the no-structural-change constraint**. They are recorded so the trade-off is explicit and available if the constraint is later relaxed.

### C-1 — Server-side pagination
**Would resolve: BE-05 (fully), and much of BE-01/BE-06 by bounding the row count**

Today the backend returns whole tables (0 `Pageable` usages) and the frontend paginates client-side at 10 rows per page across 59 templates. True pagination changes every list endpoint's response from a JSON array to a page envelope (`content`, `totalElements`, `totalPages`), and requires all 59 frontend grids to move to server-driven paging.

This is the **single highest-impact change available** and the root cause behind several S1 findings. It is also unambiguously a contract change. If the constraint is relaxed, this should become Phase 0.

### C-2 — Narrowing search-filter semantics
**Would resolve: FE-04 fully**

Replacing `JSON.stringify(item)` with a declared field list is far cheaper, but narrows which fields a search matches — visible to users. The plan instead keeps full-object matching and memoises the serialisation, which is behaviour-preserving but a smaller win.

### C-3 — Spring Boot upgrade
**Would resolve: latent risk across BE-10, BE-14, BE-16**

Spring Boot 2.2.5 (March 2020) is past end of life, as are `MySQL5InnoDBDialect`, `jjwt` 0.9.1 and `jackson-databind` 2.9.8. An upgrade brings HikariCP and Hibernate improvements, current security patches, and removes the `javax` → `jakarta` migration debt that grows with every new module. It is a project in its own right, not a performance fix, and would touch every module.

### C-4 — Fixing the stale permission menu
**Related to: FE-05**

`menu_items.ts:1122` evaluates `grant()` 27 times at module scope inside `export const menuItems`, so the menu is computed once at import and does not reflect a privilege change without a page reload. Making it reactive would be both a performance and a correctness improvement — but it changes observable behaviour. Raised here as a **defect ticket**, not a performance fix.

---

*End of audit.*
