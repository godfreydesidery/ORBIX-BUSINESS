-- =============================================================================
-- ORBIX Business - performance indexes
-- =============================================================================
-- Adds 41 secondary indexes on the columns the application searches by most
-- (status, dates, discount fields, reference numbers). No table, column or data is
-- changed; only indexes are added.
--
-- The same indexes are declared on the JPA entities (@Table(indexes = ...)) with the
-- same names, so ddl-auto=update will find them already present and do nothing.
--
-- HOW TO RUN
--   1. Run against the application database, preferably in a low-traffic window:
--        mysql -u <user> -p <database> < 2026-10-05_performance_indexes.sql
--      or open it in MySQL Workbench and execute the whole script.
--   2. Run it BEFORE deploying the application version that declares these indexes.
--      Otherwise the application creates them itself at startup, and startup waits
--      until large tables are indexed.
--   3. Safe to run more than once: an index that already exists is skipped.
--
-- ONLINE BUILD
--   Each index is built with ALGORITHM=INPLACE, LOCK=NONE: reads and writes continue
--   while it is built (MySQL 5.6+ / InnoDB). If the server cannot build an index
--   online, that statement fails instead of locking the table; the script can then
--   be re-run in a maintenance window without the ALGORITHM/LOCK options.
-- =============================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS orbix_create_index $$

CREATE PROCEDURE orbix_create_index(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_columns VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = DATABASE() AND table_name = p_table) THEN
        SELECT CONCAT('Skipped ', p_index, ': table ', p_table, ' does not exist') AS result;
    ELSEIF EXISTS (SELECT 1 FROM information_schema.statistics
                   WHERE table_schema = DATABASE() AND table_name = p_table AND index_name = p_index) THEN
        SELECT CONCAT('Exists  ', p_index) AS result;
    ELSE
        SET @ddl = CONCAT('CREATE INDEX `', p_index, '` ON `', p_table, '` (', p_columns, ') ALGORITHM=INPLACE LOCK=NONE');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('Created ', p_index) AS result;
    END IF;
END $$

DELIMITER ;

-- -----------------------------------------------------------------------------
-- P1: hot screens (discounts, yard lists, billing) and finance / sales reports
-- -----------------------------------------------------------------------------
CALL orbix_create_index('discount_requests', 'ix_discount_requests_bill_id_name', 'service_bill_id, service_bill_name');
CALL orbix_create_index('parking_bill_receivables', 'ix_pbr_discount_status_parking', 'discount_status, parking_id');
CALL orbix_create_index('storage_bill_receivables', 'ix_sbr_discount_status_storage', 'discount_status, storage_id');
CALL orbix_create_index('bond_item_bill_receivables', 'ix_bibr_discount_status_bond_item', 'discount_status, bond_item_id');
CALL orbix_create_index('parkings', 'ix_parkings_status_checked_out', 'status, checked_out_date_time');
CALL orbix_create_index('parkings', 'ix_parkings_checked_in_status', 'checked_in_date_time, status');
CALL orbix_create_index('parkings', 'ix_parkings_chasis_no_status', 'chasis_no, status');
CALL orbix_create_index('bond_items', 'ix_bond_items_status_checked_out', 'status, checked_out_date_time');
CALL orbix_create_index('bond_items', 'ix_bond_items_zone_status_checked_out', 'bond_zone_id, status, checked_out_date_time');
CALL orbix_create_index('bond_items', 'ix_bond_items_chasis_no', 'chasis_no');
CALL orbix_create_index('storages', 'ix_storages_status_checked_out', 'status, checked_out_date_time');
CALL orbix_create_index('storages', 'ix_storages_warehouse_status_checked_out', 'warehouse_id, status, checked_out_date_time');
CALL orbix_create_index('maintenances', 'ix_maintenances_status_checked_out', 'status, checked_out_date_time');
CALL orbix_create_index('weighs', 'ix_weighs_created_date_time', 'created_date_time');
CALL orbix_create_index('vehicle_equipments', 'ix_vehicle_equipments_chasis_no_active', 'chasis_no, active');
CALL orbix_create_index('collections', 'ix_collections_collection_date_time', 'collection_date_time');
CALL orbix_create_index('sales', 'ix_sales_created_date_time', 'created_date_time');
CALL orbix_create_index('restaurant_sales', 'ix_restaurant_sales_created_date_time', 'created_date_time');
CALL orbix_create_index('shop_product_logs', 'ix_shop_product_logs_shop_created', 'shop_id, created_date_time');
CALL orbix_create_index('restaurant_product_logs', 'ix_restaurant_product_logs_restaurant_created', 'restaurant_id, created_date_time');
CALL orbix_create_index('restaurant_sales_orders', 'ix_rso_restaurant_status_created', 'restaurant_id, status, created_date_time');
CALL orbix_create_index('machines', 'ix_machines_branch_status_created', 'branch_id, status, created_date_time');
CALL orbix_create_index('machines', 'ix_machines_workshop_created', 'workshop_id, created_date_time');

-- -----------------------------------------------------------------------------
-- P2: secondary reports and procurement
-- -----------------------------------------------------------------------------
CALL orbix_create_index('parkings', 'ix_parkings_created_status', 'created_date_time, status');
CALL orbix_create_index('parkings', 'ix_parkings_created_by_created', 'created_by_user_id, created_date_time');
CALL orbix_create_index('parkings', 'ix_parkings_checked_in_by_checked_in', 'checked_in_by_user_id, checked_in_date_time');
CALL orbix_create_index('bond_items', 'ix_bond_items_checked_in_status', 'checked_in_date_time, status');
CALL orbix_create_index('bond_items', 'ix_bond_items_created_status', 'created_date_time, status');
CALL orbix_create_index('bond_items', 'ix_bond_items_created_by_created', 'created_by_user_id, created_date_time');
CALL orbix_create_index('storages', 'ix_storages_checked_in_status', 'checked_in_date_time, status');
CALL orbix_create_index('storages', 'ix_storages_created_status', 'created_date_time, status');
CALL orbix_create_index('storages', 'ix_storages_created_by_created', 'created_by_user_id, created_date_time');
CALL orbix_create_index('restaurant_sales_orders', 'ix_rso_status_confirmed', 'status, confirmed_date_time');
CALL orbix_create_index('maintenance_job_card_issues', 'ix_mjci_specialist_status_closed', 'service_specialist_user_id, status, closed_date_time');
CALL orbix_create_index('bill_receivables', 'ix_bill_receivables_paid_status', 'paid_date_time, pay_status');
CALL orbix_create_index('shop_sales_orders', 'ix_shop_sales_orders_shop_status', 'shop_id, status');
CALL orbix_create_index('grns', 'ix_grns_status_approved', 'status, approved_date_time');
CALL orbix_create_index('grns', 'ix_grns_branch_status_shop', 'branch_id, status, shop_id');
CALL orbix_create_index('lpos', 'ix_lpos_status_approved', 'status, approved_date_time');
CALL orbix_create_index('lpos', 'ix_lpos_branch_status_shop', 'branch_id, status, shop_id');
CALL orbix_create_index('lpos', 'ix_lpos_no', 'no');

DROP PROCEDURE orbix_create_index;

-- -----------------------------------------------------------------------------
-- Verify: should list all 41 indexes
-- -----------------------------------------------------------------------------
SELECT table_name, index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND index_name IN ('ix_discount_requests_bill_id_name', 'ix_pbr_discount_status_parking', 'ix_sbr_discount_status_storage', 'ix_bibr_discount_status_bond_item', 'ix_parkings_status_checked_out', 'ix_parkings_checked_in_status', 'ix_parkings_chasis_no_status', 'ix_parkings_created_status', 'ix_parkings_created_by_created', 'ix_parkings_checked_in_by_checked_in', 'ix_bond_items_status_checked_out', 'ix_bond_items_zone_status_checked_out', 'ix_bond_items_chasis_no', 'ix_bond_items_checked_in_status', 'ix_bond_items_created_status', 'ix_bond_items_created_by_created', 'ix_storages_status_checked_out', 'ix_storages_warehouse_status_checked_out', 'ix_storages_checked_in_status', 'ix_storages_created_status', 'ix_storages_created_by_created', 'ix_maintenances_status_checked_out', 'ix_weighs_created_date_time', 'ix_vehicle_equipments_chasis_no_active', 'ix_collections_collection_date_time', 'ix_sales_created_date_time', 'ix_restaurant_sales_created_date_time', 'ix_shop_product_logs_shop_created', 'ix_restaurant_product_logs_restaurant_created', 'ix_rso_restaurant_status_created', 'ix_rso_status_confirmed', 'ix_machines_branch_status_created', 'ix_machines_workshop_created', 'ix_mjci_specialist_status_closed', 'ix_bill_receivables_paid_status', 'ix_shop_sales_orders_shop_status', 'ix_grns_status_approved', 'ix_grns_branch_status_shop', 'ix_lpos_status_approved', 'ix_lpos_branch_status_shop', 'ix_lpos_no')
GROUP BY table_name, index_name
ORDER BY table_name, index_name;

-- -----------------------------------------------------------------------------
-- Rollback (only if ever needed): remove the indexes again.
-- Also remove the matching @Index entries from the entities, or the application
-- recreates them at the next startup.
-- -----------------------------------------------------------------------------
-- DROP INDEX `ix_discount_requests_bill_id_name` ON `discount_requests`;
-- DROP INDEX `ix_pbr_discount_status_parking` ON `parking_bill_receivables`;
-- DROP INDEX `ix_sbr_discount_status_storage` ON `storage_bill_receivables`;
-- DROP INDEX `ix_bibr_discount_status_bond_item` ON `bond_item_bill_receivables`;
-- DROP INDEX `ix_parkings_status_checked_out` ON `parkings`;
-- DROP INDEX `ix_parkings_checked_in_status` ON `parkings`;
-- DROP INDEX `ix_parkings_chasis_no_status` ON `parkings`;
-- DROP INDEX `ix_parkings_created_status` ON `parkings`;
-- DROP INDEX `ix_parkings_created_by_created` ON `parkings`;
-- DROP INDEX `ix_parkings_checked_in_by_checked_in` ON `parkings`;
-- DROP INDEX `ix_bond_items_status_checked_out` ON `bond_items`;
-- DROP INDEX `ix_bond_items_zone_status_checked_out` ON `bond_items`;
-- DROP INDEX `ix_bond_items_chasis_no` ON `bond_items`;
-- DROP INDEX `ix_bond_items_checked_in_status` ON `bond_items`;
-- DROP INDEX `ix_bond_items_created_status` ON `bond_items`;
-- DROP INDEX `ix_bond_items_created_by_created` ON `bond_items`;
-- DROP INDEX `ix_storages_status_checked_out` ON `storages`;
-- DROP INDEX `ix_storages_warehouse_status_checked_out` ON `storages`;
-- DROP INDEX `ix_storages_checked_in_status` ON `storages`;
-- DROP INDEX `ix_storages_created_status` ON `storages`;
-- DROP INDEX `ix_storages_created_by_created` ON `storages`;
-- DROP INDEX `ix_maintenances_status_checked_out` ON `maintenances`;
-- DROP INDEX `ix_weighs_created_date_time` ON `weighs`;
-- DROP INDEX `ix_vehicle_equipments_chasis_no_active` ON `vehicle_equipments`;
-- DROP INDEX `ix_collections_collection_date_time` ON `collections`;
-- DROP INDEX `ix_sales_created_date_time` ON `sales`;
-- DROP INDEX `ix_restaurant_sales_created_date_time` ON `restaurant_sales`;
-- DROP INDEX `ix_shop_product_logs_shop_created` ON `shop_product_logs`;
-- DROP INDEX `ix_restaurant_product_logs_restaurant_created` ON `restaurant_product_logs`;
-- DROP INDEX `ix_rso_restaurant_status_created` ON `restaurant_sales_orders`;
-- DROP INDEX `ix_rso_status_confirmed` ON `restaurant_sales_orders`;
-- DROP INDEX `ix_machines_branch_status_created` ON `machines`;
-- DROP INDEX `ix_machines_workshop_created` ON `machines`;
-- DROP INDEX `ix_mjci_specialist_status_closed` ON `maintenance_job_card_issues`;
-- DROP INDEX `ix_bill_receivables_paid_status` ON `bill_receivables`;
-- DROP INDEX `ix_shop_sales_orders_shop_status` ON `shop_sales_orders`;
-- DROP INDEX `ix_grns_status_approved` ON `grns`;
-- DROP INDEX `ix_grns_branch_status_shop` ON `grns`;
-- DROP INDEX `ix_lpos_status_approved` ON `lpos`;
-- DROP INDEX `ix_lpos_branch_status_shop` ON `lpos`;
-- DROP INDEX `ix_lpos_no` ON `lpos`;
