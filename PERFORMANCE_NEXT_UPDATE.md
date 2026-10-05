# ORBIX Business: performance work for the next update

**Status as of 5 October 2026.** Branch `performance-improvements`, commit `1037e95`.

This is the backlog left after the code-only performance update. Item IDs (BE-xx, FE-xx, IN-xx, HY-xx) refer to `PERFORMANCE_AUDIT.md`, which holds the full evidence. Effort uses the classes from the proposal:
- **L1 Configuration:** settings only.
- **L2 Targeted:** a few functions.
- **L3 Broad:** many functions in one part of the system.
- **L4 Cross-system:** server and screens together.

## What the current update already covers

These are in the current build:
- **Indexes:** 41 indexes, shipped with `Orbix-Business-API/api/api/sql/2026-10-05_performance_indexes.sql`.
- **Discounts, billing, check-out and payment:** batched loading instead of one query per row.
- **User look-ups:** the logged-in user is looked up once per request.
- **Read-only transactions** on list and report endpoints.
- **Reports:** the parking, bond and storage reports read only the columns they show.
- **List paging:** every list screen is paged and searched on the server, except payment bill lists, reports and dropdowns, which stay full on purpose.
- **Lazy collections:** branch trees and document lines load only when read.
- **Screens:**
  - trackBy on every list;
  - debounced searches;
  - the JWT decoded once;
  - print headers loaded once per login;
  - a single copy of pdfmake;
  - no `console.log` output in production.

`ORBIX_Performance_Improvement_Worked_Proposal.pdf` lists what each package delivered.

## Before anything below

1. **Run the index script** on each database before deploying the current build.
2. **Deploy the backend before the frontend.** The screens call the new `_page` / `get_page` endpoints.

---

## 1. Server configuration (Package A) — needs approval to change `application.properties`

This is the largest remaining gain for the least work. Every item here is configuration only.

| # | Change | Where | Why | Risk | Effort |
|---|---|---|---|---|---|
| 1.1 | Turn off SQL logging in production: `spring.jpa.show-sql=false`, remove `format_sql`, and set `logging.level.org.hibernate.SQL` and `logging.level.org.hibernate.type` to `WARN` | `application.properties` lines 16–20 | Today every statement is printed and pretty-formatted, and `org.hibernate.type=TRACE` also logs **every bound parameter value**. Every request pays this CPU and disk cost. (BE-08) | Low. Keep the verbose settings in a development profile. | L1 |
| 1.2 | Batched loading of related records: `spring.jpa.properties.hibernate.default_batch_fetch_size=50` | `application.properties` | Turns the remaining one-select-per-row loads of related records into a few `IN (...)` selects, across the whole system at once. (BE-27) | Low. Same results, fewer queries. | L1 |
| 1.3 | Response compression: `server.compression.enabled=true` plus mime types and min size | `application.properties` | JSON lists are sent uncompressed. (BE-09) | Low | L1 |
| 1.4 | Connection pool and JDBC flags: tomcat-jdbc `max-active` sized to the database, validation query, and statement cache flags on the JDBC URL | `application.properties` | Defaults allow 100 concurrent connections and cache no prepared statements. (BE-14) | Low. Verify under load. | L1 |
| 1.5 | Upload limits: replace the unlimited multipart limits (`-1`) and the `/` temp location | `application.properties` lines 43–53 | Unbounded uploads use memory and disk. (BE-17) | Low | L1 |
| 1.6 | A separate production profile (`application-prod.properties`) | resources | Development settings currently ship to production. (HY-02) | Low | L1 |
| 1.7 | Static file caching on the web server: long cache headers for the hashed `*.js` / `*.css` files, and `no-cache` for `index.html` and `env.js` | Apache `.htaccess` / vhost | Browsers re-download the app. Without `no-cache`, `env.js` changes are not picked up; this happened during testing. (FE-09) | Low | L1 |

## 2. Hosting environment (Package K) — needs access to the servers

| # | Change | Why | Effort |
|---|---|---|---|
| 2.1 | Measure real load, then right-size the application and database servers (IN-01) | Directly affects the monthly bill | L2 |
| 2.2 | MySQL settings: `innodb_buffer_pool_size`, connection limits, and the slow-query log (IN-02) | Catches anything that only shows under real traffic | L1 |
| 2.3 | Java heap and garbage-collector settings for the service (IN-03) | Stable memory use | L1 |
| 2.4 | Log rotation and caps (IN-04), plus basic monitoring and alerts for CPU, memory, disk and database (IN-05) | Early warning | L2 |
| 2.5 | HTTPS on a domain name instead of plain HTTP to an IP (IN-06) | Security, and enables HTTP/2 | L2 |

## 3. Reports (Package J)

The finance, sales, stock-log and GRN/LPO reports still download every row in the chosen period. The browser then adds up totals and builds printouts. Long periods and month-end are the slow cases.

| # | Change | Where | Notes | Effort |
|---|---|---|---|---|
| 3.1 | Index-friendly report SQL (BE-26) | `ParkingRepository`, `BondItemRepository`, `StorageRepository` `getMonthlyStats` (`YEAR()`/`MONTH()` on columns); `CollectionRepository` (an extra hop through `bill_receivables`); `GrnRepository` `COALESCE(l.supplier_id)` | Code only. Same results. Can go in the next update without other decisions. | L2 |
| 3.2 | Totals and summaries calculated on the server and returned with the report | finance, sales and stock-log report resources and screens | Each report must be checked against today's figures before release. | L4 |
| 3.3 | Then page long reports; printouts and exports produced on the server | same | Depends on 3.2 | L4 |

## 4. Remaining structural work (Package I)

| # | Change | Notes | Effort |
|---|---|---|---|
| 4.1 | Load single related records (created-by / approved-by users, their company and branch, and so on) only when read: `@ManyToOne`/`@OneToOne` to `LAZY`, module by module, with join fetches where a screen needs them (BE-01 step 3) | Largest remaining source of database work. It changes what endpoints that return entities directly (e.g. `/users`) serialise, so first record responses from the main endpoints on a copy of production data, and compare after each module. | L4 |
| 4.2 | Exclude the remaining relations from Lombok `@Data` `equals`/`hashCode`/`toString` (BE-11) | Prerequisite for 4.1 | L3 |

## 5. Code paths reviewed but not yet changed

All of these are code only.

| # | Change | Where | Why | Risk | Effort |
|---|---|---|---|---|---|
| 5.1 | Sales-order confirmation and GRN approval: look up the user once, preload the stock rows in one query, and save logs in bulk (BE-23) | `ShopSalesOrderServiceController.confirmShopSalesOrder`, `RestaurantSalesOrderServiceController.confirmRestaurantSalesOrder`, `GrnServiceController.approveGrn`, `SaleServiceController.createSale` / `RestaurantSaleServiceController.createRestaurantSale` (a flush per line), `MachineServiceServiceController.confirm` | Several database steps per line item at the till | Medium: write paths, so test stock and log rows carefully | L3 |
| 5.2 | Type-ahead searches: return the first 20 matches instead of all (BE-29) | `…Containing` repository methods: vehicle chassis numbers, users, products, dineables, services, suppliers, shop/restaurant products | Each keystroke can return thousands of rows | Low | L2 |
| 5.3 | Pending parking invoices: read only the invoice links instead of every parking ever (BE-21) | `InvoiceReceivableServiceController.getPendingParkingInvoiceReceivables` | Can fail outright as data grows. The screen is not in the menu today. | Low | L2 |
| 5.4 | Billing detail screens: update the changed bill row instead of reloading the vehicle's bill list after each save (FE-15) | vehicle-equipment-billing, good-billing, bond-item-billing, weight-billing, maintenance-vehicle-equipment-billing | Small lists, so a small gain | Low | L3 |
| 5.5 | Keep reference data (zones, types, warehouses, and so on) for the session instead of re-fetching it on every screen (FE-18) | many screens | Fewer requests | Low | L3 |
| 5.6 | Remove duplicate and dead queries and methods (BE-25), and the dead code left by the paging work (`release-vehicle-equipment` `getAllClearedParkings`, `select-bond-zone` `getAllCheckedInBondItems`, `toggleShowImportList` on the import screens, the client-side filters on `restaurant-dineable-stock-status`, the hidden storages table on `select-workshop`) | various | Maintenance and clarity | Low | L2 |
| 5.7 | Startup seeding: load privilege names once and save the missing ones in one batch (BE-15) | `MainApplication` | Faster deploys, not user-facing | Low | L2 |

## 6. Frontend build — needs approval to change `angular.json`

| # | Change | Why | Effort |
|---|---|---|---|
| 6.1 | Load pdfmake and its fonts only when printing: remove them from `angular.json` `scripts` and load them in the background after start-up (FE-02) | About 2.9 MB of the 4.8 MB global script every user downloads before the app starts | L2 |
| 6.2 | Remove the second copy of jQuery from `angular.json` `scripts` (it is listed twice), and review the other global scripts (FE-01). Plugins attach to whichever copy is loaded at that moment: jquery-ui, knob, fullcalendar and dropzone attach to the first copy, select2 to the second. Removing a copy therefore needs those widgets tested. | Smaller first load | L2 |
| 6.3 | Re-enable full production optimisation in `angular.json` (FE-13), and load third-party scripts in `index.html` without blocking rendering (FE-10) | Faster first paint | L1 |
| 6.4 | Load screens on demand: replace `PreloadAllModules` (`app.config.ts`) with no preloading or selective preloading (FE-03) | The browser stops downloading every screen at start-up | L1 |
| 6.5 | Compress images and fonts (FE-11) | Smaller downloads | L2 |

## 7. Data retention — business decision

Closed records stay in the live tables forever, so every table keeps growing. An agreed retention period would keep the live system lean. For example, closed records older than two years could move to an archive that reports can still read. The audit's Appendix E-6 has the options.

---

## Issues found along the way (not performance)

- **Unreachable check-out code.** The check-out buttons on `bond-discounts` and `weigh-billing` are commented out. Their code calls `/bonds/check_out` and `/weighs/check_out`, which don't exist. Remove the code, or fix it before re-enabling the buttons.
- **Missing endpoints.**
  - `select-restaurant` calls `/grns/get_all_visible_by_restaurant` and `/lpos/get_all_visible_by_restaurant`.
  - `select-workshop` calls `/storages/get_all_*_by_workshop`.
  - None of these exist, and the data model has no restaurant/workshop link for them. The buttons that open these lists are commented out.
- **Time stamps are three hours ahead.** `DayServiceController.getTimeStamp()` returns `now + 3h`, but most "recent" filters compare against plain `now()`. As a result, "last 24 hours" lists effectively cover about 27 hours. Fix it by using a proper time zone (`ZoneId`) in one place.
- **Secrets in configuration.** `jwt.secret` is a short word in `application.properties`. Move it to a protected environment variable and rotate it.
- **Known since the audit:**
  - weighbridge discount requests are rejected;
  - the maintenance "today's checked-out" list returns no data.

## Suggested order for the next update

1. **Section 1** (configuration), after approval. Small, low risk, and it helps every request.
2. **3.1 and 5.2** (code only, low risk), together with **5.6** clean-up.
3. **5.1** (till and GRN confirmation), with careful stock testing.
4. **Section 2** (hosting), once there is access to the servers.
5. **Section 6** (frontend build), after approval.
6. **3.2 and 3.3** (reports) and **4.1** (lazy relations), each behind response comparison against the current version.
