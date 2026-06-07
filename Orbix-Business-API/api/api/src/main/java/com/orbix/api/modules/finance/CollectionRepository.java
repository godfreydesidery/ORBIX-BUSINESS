package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
	
	
//	@Query(value = "SELECT " +
//            "SUM(c.amount) AS amount, " +
//            "c.reason AS reason, " +
//            "c.pay_code AS paymentType, " +
//            "u.nickname AS cashierName " +
//        "FROM cash_collections c " +
//        "JOIN users u ON u.id = c.collected_by_user_id " +
//        "WHERE c.collection_date_time BETWEEN :startDate AND :endDate " +
//        "AND (:nickname IS NULL OR :nickname = '' OR u.nickname = :nickname) " +
//        "GROUP BY c.reason, c.pay_code", 
//    nativeQuery = true)
//	List<IBillReceivableCollection> findTotalCollectionByDateRangeAndCashier(
//     @Param("startDate") LocalDateTime startDate, 
//     @Param("endDate") LocalDateTime endDate, 
//     @Param("nickname") String nickname);
	
	@Query(value = "SELECT " +
	        "SUM(brc.amount) AS amount, " +
	        "brc.reason AS reason, " +
	        "c.pay_code AS payCode, " +
	        "u.nickname AS cashierName " +
	    "FROM bill_receivable_collections brc " +
	    "JOIN collections c ON brc.collection_id = c.id " +
	    "JOIN users u ON u.id = c.collected_by_user_id " +
	    "WHERE c.collection_date_time BETWEEN :startDate AND :endDate " +
	    "AND (:nickname IS NULL OR :nickname = '' OR u.nickname = :nickname) " +
	    "GROUP BY brc.reason, c.pay_code, u.nickname", 
	    nativeQuery = true)
	List<IBillReceivableCollection> findTotalCollectionByDateRangeAndCashier(
	    @Param("startDate") LocalDateTime startDate, 
	    @Param("endDate") LocalDateTime endDate, 
	    @Param("nickname") String nickname);




	@Query(value = "SELECT " +
	        "SUM(brc.amount) AS amount, " +
	        "brc.reason AS reason, " +
	        "c.pay_code AS payCode, " +
	        "u.nickname AS cashierName " +
	    "FROM bill_receivable_collections brc " +
	    "JOIN collections c ON brc.collection_id = c.id " +
	    "JOIN users u ON u.id = c.collected_by_user_id " +
	    "WHERE c.collection_date_time BETWEEN :startDate AND :endDate " +
	    "GROUP BY brc.reason, c.pay_code, u.nickname", 
	    nativeQuery = true)
	List<IBillReceivableCollection> findTotalCollectionByDateRange(
	    @Param("startDate") LocalDateTime startDate, 
	    @Param("endDate") LocalDateTime endDate);


	

//	@Query(value = "SELECT " +
//	            "cc.amount AS amount, " +
//	            "cc.pay_code AS paymentType, " +
//	            "cc.collection_date_time AS dateTime, " +
//	            "cc.reason AS reason, " +
//	            "p.vehicle_equipment_category AS vehicleEquipmentCategory, " +
//	            "p.vehicle_equipment_name AS vehicleEquipmentName, " +
//	            "p.owner_first_name AS ownerFirstName, " +
//	            "p.owner_last_name AS ownerLastName, " +
//	            "p.card_no AS cardNo, " +
//	            "p.owner_phone_no AS ownerPhoneNo, " +
//	            "p.chasis_no AS chasisNo, " +
//	            "p.created_date_time AS createdDateTime, " +
//	            "pr.qty AS days, " +
//	            "pr.discount AS discount, " +
//	            "u.nickname AS cashierName " +
//	        "FROM " +
//	            "cash_collections cc " +
//	        "JOIN " +
//	            "bill_receivables br ON br.id = cc.bill_receivable_id " +
//	        "JOIN " +
//	            "parking_bill_receivables pr ON br.id = pr.bill_receivable_id " +
//	        "JOIN " +
//	            "parkings p ON p.id = pr.parking_id " +
//	        "JOIN " +
//	            "users u ON cc.collected_by_user_id = u.id " +
//	        "WHERE " +
//	            "cc.collection_date_time BETWEEN :startDate AND :endDate " 
//	        , nativeQuery = true)
//	    List<IParkingCollection> findParkingCollectionsBetweenDates(
//	            @Param("startDate") LocalDateTime startDate,
//	            @Param("endDate") LocalDateTime endDate);
	
//	@Query(value = "SELECT " +
//	        "brc.amount AS amount, " +
//	        "cc.pay_code AS payCode, " +
//	        "cc.collection_date_time AS dateTime, " +
//	        "brc.reason AS reason, " +
//	        "p.vehicle_equipment_category AS vehicleEquipmentCategory, " +
//	        "p.vehicle_equipment_name AS vehicleEquipmentName, " +
//	        "p.owner_first_name AS ownerFirstName, " +
//	        "p.owner_last_name AS ownerLastName, " +
//	        "p.card_no AS cardNo, " +
//	        "p.owner_phone_no AS ownerPhoneNo, " +
//	        "p.chasis_no AS chasisNo, " +
//	        "p.created_date_time AS createdDateTime, " +
//	        "pr.qty AS days, " +
//	        "pr.discount AS discount, " +
//	        "u.nickname AS cashierName " +
//	    "FROM bill_receivable_collections brc " +
//	    "JOIN collections cc ON brc.collection_id = cc.id " +
//	    "JOIN bill_receivables br ON br.id = brc.bill_receivable_id " +
//	    "JOIN parking_bill_receivables pr ON br.id = pr.bill_receivable_id " +
//	    "JOIN parkings p ON p.id = pr.parking_id " +
//	    "JOIN users u ON cc.collected_by_user_id = u.id " +
//	    "WHERE cc.collection_date_time BETWEEN :startDate AND :endDate", 
//	    nativeQuery = true)
//	List<IParkingCollection> findParkingCollectionsBetweenDates(
//	    @Param("startDate") LocalDateTime startDate,
//	    @Param("endDate") LocalDateTime endDate);
	
//	@Query(
//		    "SELECT " +
//		    "pbr.billReceivable.amount AS amount, " +
//		    "c.payCode AS payCode, " +
//		    "c.collectionDateTime AS dateTime, " +
//		    "brc.reason AS reason, " +
//		    "p.vehicleEquipmentCategory AS vehicleEquipmentCategory, " +
//		    "p.vehicleEquipmentName AS vehicleEquipmentName, " +
//		    "p.ownerFirstName AS ownerFirstName, " +
//		    "p.ownerLastName AS ownerLastName, " +
//		    "p.cardNo AS cardNo, " +
//		    "p.ownerPhoneNo AS ownerPhoneNo, " +
//		    "p.chasisNo AS chasisNo, " +
//		    "p.createdDateTime AS createdDateTime, " +
//		    "pbr.qty AS days, " +
//		    "pbr.discount AS discount, " +
//		    "u.nickname AS cashierName " +
//		    "FROM BillReceivableCollection brc " +
//		    "JOIN brc.collection c " +
//		    "JOIN c.collectedByUser u " +
//		    "JOIN brc.billReceivable br " +
//		    "JOIN ParkingBillReceivable pbr ON pbr.billReceivable = br " +
//		    "JOIN pbr.parking p " +
//		    "WHERE c.collectionDateTime BETWEEN :startDate AND :endDate"
//		)
//		List<IParkingCollection> findParkingCollectionsBetweenDates(
//		    @Param("startDate") LocalDateTime startDate,
//		    @Param("endDate") LocalDateTime endDate
//		);
	
	@Query(
		    value = "SELECT " +
		            "brc.amount AS amount, " +  
		            "c.pay_code AS payCode, " +
		            "c.collection_date_time AS dateTime, " +
		            "brc.reason AS reason, " +
		            "p.vehicle_equipment_category AS vehicleEquipmentCategory, " +
		            "p.vehicle_equipment_name AS vehicleEquipmentName, " +
		            "p.owner_first_name AS ownerFirstName, " +
		            "p.owner_last_name AS ownerLastName, " +
		            "p.card_no AS cardNo, " +
		            "p.owner_phone_no AS ownerPhoneNo, " +
		            "p.chasis_no AS chasisNo, " +
		            "p.created_date_time AS createdDateTime, " +
		            "pbr.qty AS days, " +
		            "pbr.discount AS discount, " +
		            "u.nickname AS cashierName " +
		            "FROM bill_receivable_collections brc " +
		            "JOIN collections c ON brc.collection_id = c.id " +
		            "JOIN users u ON c.collected_by_user_id = u.id " +
		            "JOIN bill_receivables br ON brc.bill_receivable_id = br.id " +
		            "JOIN parking_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
		            "JOIN parkings p ON p.id = pbr.parking_id " +
		            "WHERE c.collection_date_time BETWEEN :startDate AND :endDate", 
		    nativeQuery = true
		)
		List<IParkingCollection> findParkingCollectionsBetweenDates(
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate
		);





	
	
//	@Query(value = "SELECT " +
//            "cc.amount AS amount, " +
//            "cc.pay_code AS paymentType, " +
//            "cc.collection_date_time AS dateTime, " +
//            "cc.reason AS reason, " +
//            "p.vehicle_equipment_category AS vehicleEquipmentCategory, " +
//            "p.vehicle_equipment_name AS vehicleEquipmentName, " +
//            "p.owner_first_name AS ownerFirstName, " +
//            "p.owner_last_name AS ownerLastName, " +
//            "p.card_no AS cardNo, " +
//            "p.owner_phone_no AS ownerPhoneNo, " +
//            "p.chasis_no AS chasisNo, " +
//            "p.created_date_time AS createdDateTime, " +
//            "psbr.qty AS qty, " +
//            "psbr.discount AS discount, " +
//            "psbr.description AS serviceDescription, " +
//            "u.nickname AS cashierName " +
//        "FROM " +
//            "cash_collections cc " +
//        "JOIN " +
//            "bill_receivables br ON br.id = cc.bill_receivable_id " +
//        "JOIN " +
//            "parking_service_bill_receivables psbr ON br.id = psbr.bill_receivable_id " +
//        "JOIN " +
//            "parkings p ON p.id = psbr.parking_id " +
//        "JOIN " +
//            "users u ON cc.collected_by_user_id = u.id " +
//        "WHERE " +
//            "cc.collection_date_time BETWEEN :startDate AND :endDate", 
//        nativeQuery = true)
//List<IParkingServiceCollection> findParkingServiceCollectionsBetweenDates(
//        @Param("startDate") LocalDateTime startDate,
//        @Param("endDate") LocalDateTime endDate);
	
//	@Query(value = "SELECT " +
//	        "cc.amount AS amount, " +
//	        "cc.pay_code AS payCode, " +
//	        "cc.collection_date_time AS dateTime, " +
//	        "cc.reason AS reason, " +
//	        "p.vehicle_equipment_category AS vehicleEquipmentCategory, " +
//	        "p.vehicle_equipment_name AS vehicleEquipmentName, " +
//	        "p.owner_first_name AS ownerFirstName, " +
//	        "p.owner_last_name AS ownerLastName, " +
//	        "p.card_no AS cardNo, " +
//	        "p.owner_phone_no AS ownerPhoneNo, " +
//	        "p.chasis_no AS chasisNo, " +
//	        "p.created_date_time AS createdDateTime, " +
//	        "psbr.qty AS qty, " +
//	        "psbr.discount AS discount, " +
//	        "psbr.description AS serviceDescription, " +
//	        "u.nickname AS cashierName " +
//	    "FROM " +
//	        "cash_collections cc " +
//	    "JOIN " +
//	        "bill_receivables br ON br.id = cc.bill_receivable_id " +
//	    "JOIN " +
//	        "parking_service_bill_receivables psbr ON br.id = psbr.bill_receivable_id " +
//	    "JOIN " +
//	        "parkings p ON p.id = psbr.parking_id " +
//	    "JOIN " +
//	        "users u ON cc.collected_by_user_id = u.id " +
//	    "WHERE " +
//	        "cc.collection_date_time BETWEEN :startDate AND :endDate", 
//	    nativeQuery = true)
//	List<IParkingServiceCollection> findParkingServiceCollectionsBetweenDates(
//	    @Param("startDate") LocalDateTime startDate,
//	    @Param("endDate") LocalDateTime endDate);
	
	
	@Query(value = "SELECT " +
	        "brc.amount AS amount, " +
	        "c.pay_code AS payCode, " +
	        "c.collection_date_time AS dateTime, " +
	        "brc.reason AS reason, " +
	        "p.vehicle_equipment_category AS vehicleEquipmentCategory, " +
	        "p.vehicle_equipment_name AS vehicleEquipmentName, " +
	        "p.owner_first_name AS ownerFirstName, " +
	        "p.owner_last_name AS ownerLastName, " +
	        "p.card_no AS cardNo, " +
	        "p.owner_phone_no AS ownerPhoneNo, " +
	        "p.chasis_no AS chasisNo, " +
	        "p.created_date_time AS createdDateTime, " +
	        "psbr.qty AS qty, " +
	        "psbr.description AS serviceDescription, " +
	        "psbr.discount AS discount, " +
	        "u.nickname AS cashierName " +
	    "FROM " +
	        "bill_receivable_collections brc " +
	    "JOIN " +
	        "collections c ON brc.collection_id = c.id " +
	    "JOIN " +
	        "bill_receivables br ON brc.bill_receivable_id = br.id " +
	    "JOIN " +
	        "parking_service_bill_receivables psbr ON br.id = psbr.bill_receivable_id " +
	    "JOIN " +
	        "parkings p ON psbr.parking_id = p.id " +
	    "JOIN " +
	        "users u ON c.collected_by_user_id = u.id " +
	    "WHERE " +
	        "c.collection_date_time BETWEEN :startDate AND :endDate", 
	    nativeQuery = true)
	List<IParkingServiceCollection> findParkingServiceCollectionsBetweenDates(
	    @Param("startDate") LocalDateTime startDate,
	    @Param("endDate") LocalDateTime endDate);


	
	@Query(
		    value = "SELECT " +
		            "brc.amount AS amount, " +  
		            "ps.name AS productName, " + 
		            "c.pay_code AS payCode, " +
		            "c.collection_date_time AS dateTime, " +
		            "pbr.qty AS qty, " +
		            "pbr.discount AS discount, " +
		            "u.nickname AS cashierName " +
		            "FROM bill_receivable_collections brc " +
		            "JOIN collections c ON brc.collection_id = c.id " +
		            "JOIN users u ON c.collected_by_user_id = u.id " +
		            "JOIN bill_receivables br ON brc.bill_receivable_id = br.id " +
		            "JOIN sale_detail_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
		            "JOIN sale_details p ON p.id = pbr.sale_detail_id " +
		            "JOIN products ps ON p.product_id = ps.id " +
		            "WHERE c.collection_date_time BETWEEN :startDate AND :endDate", 
		    nativeQuery = true
		)
		List<ISalesCollection> findSalesCollectionsBetweenDates(
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate
		);
	
	@Query(
		    value = "SELECT " +
		            "brc.amount AS amount, " +  
		            "ps.name AS productName, " + 
		            "c.pay_code AS payCode, " +
		            "c.collection_date_time AS dateTime, " +
		            "pbr.qty AS qty, " +
		            "pbr.discount AS discount, " +
		            "u.nickname AS cashierName " +
		            "FROM bill_receivable_collections brc " +
		            "JOIN collections c ON brc.collection_id = c.id " +
		            "JOIN users u ON c.collected_by_user_id = u.id " +
		            "JOIN bill_receivables br ON brc.bill_receivable_id = br.id " +
		            "JOIN restaurant_sale_detail_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
		            "JOIN restaurant_sale_details p ON p.id = pbr.restaurant_sale_detail_id " +
		            "JOIN dineables ps ON p.dineable_id = ps.id " +
		            "WHERE c.collection_date_time BETWEEN :startDate AND :endDate", 
		    nativeQuery = true
		)
		List<IRestaurantSalesCollection> findRestaurantSalesCollectionsBetweenDates(
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate
		);

	@Query(
		    value = "SELECT " +
		            "brc.amount AS amount, " +  
		            "c.pay_code AS payCode, " +
		            "c.collection_date_time AS dateTime, " +
		            "brc.reason AS reason, " +
		            "p.good_name AS goodName, " +
		            "p.owner_first_name AS ownerFirstName, " +
		            "p.owner_last_name AS ownerLastName, " +
		            "p.owner_phone_no AS ownerPhoneNo, " +
		            "p.created_date_time AS createdDateTime, " +
		            "pbr.qty AS days, " +
		            "pbr.discount AS discount, " +
		            "u.nickname AS cashierName " +
		            "FROM bill_receivable_collections brc " +
		            "JOIN collections c ON brc.collection_id = c.id " +
		            "JOIN users u ON c.collected_by_user_id = u.id " +
		            "JOIN bill_receivables br ON brc.bill_receivable_id = br.id " +
		            "JOIN storage_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
		            "JOIN storages p ON p.id = pbr.storage_id " +
		            "WHERE c.collection_date_time BETWEEN :startDate AND :endDate", 
		    nativeQuery = true
		)
		List<IStorageCollection> findStorageCollectionsBetweenDates(
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate
		);
	
	@Query(
		    value = "SELECT " +
		            "brc.amount AS amount, " +  
		            "c.pay_code AS payCode, " +
		            "c.collection_date_time AS dateTime, " +
		            "brc.reason AS reason, " +
		            "p.bond_item_name AS bondItemName, " +
		            "p.chasis_no AS chasisNo, " +
		            "p.owner_first_name AS ownerFirstName, " +
		            "p.owner_last_name AS ownerLastName, " +
		            "p.owner_phone_no AS ownerPhoneNo, " +
		            "p.created_date_time AS createdDateTime, " +
		            "pbr.qty AS days, " +
		            "pbr.discount AS discount, " +
		            "u.nickname AS cashierName, " +
		            "bz.id AS bondZoneId, " +              
		            "bz.name AS bondZoneName " +          
		            "FROM bill_receivable_collections brc " +
		            "JOIN collections c ON brc.collection_id = c.id " +
		            "JOIN users u ON c.collected_by_user_id = u.id " +
		            "JOIN bill_receivables br ON brc.bill_receivable_id = br.id " +
		            "JOIN bond_item_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
		            "JOIN bond_items p ON p.id = pbr.bond_item_id " +
		            "JOIN bond_zones bz ON p.bond_zone_id = bz.id " +   
		            "WHERE c.collection_date_time BETWEEN :startDate AND :endDate AND (:bond_zone_id IS NULL OR bz.id = :bond_zone_id)",
		    nativeQuery = true
		)
		List<IBondItemCollection> findBondItemCollectionsBetweenDates_(
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate,
		    @Param("bond_zone_id") Long bondZoneId
		);
	
	@Query(
		    value = "SELECT " +
		            "bill_receivable_collections.amount AS amount, " +  
		            "collections.pay_code AS payCode, " +
		            "collections.collection_date_time AS dateTime, " +
		            "bill_receivable_collections.reason AS reason, " +
		            "maintenance_job_card_issues.name AS issueName, " +
		            "maintenances.vehicle_equipment_category AS vehicleEquipmentCategory, " +
		            "maintenances.vehicle_equipment_name AS vehicleEquipmentName, " +
		            "maintenances.owner_first_name AS ownerFirstName, " +
		            "maintenances.owner_last_name AS ownerLastName, " +
		            "maintenances.card_no AS cardNo, " +
		            "maintenances.owner_phone_no AS ownerPhoneNo, " +
		            "maintenances.chasis_no AS chasisNo, " +
		            "maintenances.created_date_time AS createdDateTime, " +
		            "maintenance_job_card_issue_bill_receivables.qty AS days, " +
		            "maintenance_job_card_issue_bill_receivables.discount AS discount, " +
		            "users.nickname AS cashierName " +
		            "FROM bill_receivable_collections " +
		            "JOIN collections ON bill_receivable_collections.collection_id = collections.id " +
		            "JOIN users ON collections.collected_by_user_id = users.id " +
		            "JOIN bill_receivables ON bill_receivable_collections.bill_receivable_id = bill_receivables.id " +
		            "JOIN maintenance_job_card_issue_bill_receivables ON maintenance_job_card_issue_bill_receivables.bill_receivable_id = bill_receivables.id " +
		            "JOIN maintenance_job_card_issues ON maintenance_job_card_issues.id = maintenance_job_card_issue_bill_receivables.maintenance_job_card_issue_id " +
		            "JOIN maintenance_job_cards ON maintenance_job_cards.id = maintenance_job_card_issues.maintenance_job_card_id " +
		            "JOIN maintenances ON maintenances.id = maintenance_job_cards.maintenance_id " +
		            "WHERE collections.collection_date_time BETWEEN :startDate AND :endDate", 
		    nativeQuery = true
		)
		List<IMaintenanceCollection> findMaintenanceCollectionsBetweenDates(
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate
		);
	
	@Query(
		    value = "SELECT " +
		            "brc.amount AS amount, " +  
		            "c.pay_code AS payCode, " +
		            "c.collection_date_time AS dateTime, " +
		            "brc.reason AS reason, " +
		            "p.reg_no AS regNo, " +
		            "p.owner_first_name AS ownerFirstName, " +
		            "p.owner_last_name AS ownerLastName, " +
		            "p.owner_phone_no AS ownerPhoneNo, " +
		            "p.created_date_time AS createdDateTime, " +
		            "pbr.discount AS discount, " +
		            "NULLIF(pbr.weight_one, 0) AS weightOne, " +
		            "NULLIF(pbr.weight_two, 0) AS weightTwo, " +
		            "NULLIF(pbr.weight_three, 0) AS weightThree, " +
		            "NULLIF(pbr.weight_four, 0) AS weightFour, " +
		            "u.nickname AS cashierName " +
		            "FROM bill_receivable_collections brc " +
		            "JOIN collections c ON brc.collection_id = c.id " +
		            "JOIN users u ON c.collected_by_user_id = u.id " +
		            "JOIN bill_receivables br ON brc.bill_receivable_id = br.id " +
		            "JOIN weigh_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
		            "JOIN weighs p ON p.id = pbr.weigh_id " +
		            "WHERE c.collection_date_time BETWEEN :startDate AND :endDate" +
		            " AND (:nickname IS NULL OR :nickname = '' OR u.nickname = :nickname)",
		    nativeQuery = true
		)
		List<IWeighCollection> findWeighCollectionsBetweenDates(
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate,
		    @Param("nickname") String nickname
		);
	
	@Query(
		    value = "SELECT " +
		            "brc.amount AS amount, " +  
		            "c.pay_code AS payCode, " +
		            "c.collection_date_time AS dateTime, " +
		            "brc.reason AS reason, " +
		            "p.reg_no AS regNo, " +
		            "p.owner_name AS ownerName, " +
		            "p.name AS machineName, " +
		            "p.owner_phone_no AS ownerPhoneNo, " +
		            "s.name AS serviceName, " +
		            "p.created_date_time AS createdDateTime, " +
		            "u.nickname AS cashierName " +
		            "FROM bill_receivable_collections brc " +
		            "JOIN collections c ON brc.collection_id = c.id " +
		            "JOIN users u ON c.collected_by_user_id = u.id " +
		            "JOIN bill_receivables br ON brc.bill_receivable_id = br.id " +
		            "JOIN machine_service_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
		            "JOIN machine_services ms ON pbr.machine_service_id = ms.id " +
		            "JOIN machines p ON p.id = ms.machine_id " +
		            "JOIN services s ON s.id = ms.service_id " +
		            "WHERE c.collection_date_time BETWEEN :startDate AND :endDate" +
		            " AND (:nickname IS NULL OR :nickname = '' OR u.nickname = :nickname)",
		    nativeQuery = true
		)
		List<IWorkshopCollection> findWorkshopCollectionsBetweenDates(
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate,
		    @Param("nickname") String nickname
		);
	
	@Query(value = "SELECT " +
	        "p.chasis_no AS chasisNo, " +
	        "p.owner_first_name AS ownerFirstName, " +
	        "p.owner_last_name AS ownerLastName, " +
	        "p.owner_phone_no AS ownerPhoneNo, " +
	        "DATE(p.checked_in_date_time) AS checkedInDate, " +
	        "TIME(p.checked_in_date_time) AS checkedInTime, " +
	        "DATE(p.checked_out_date_time) AS checkedOutDate, " +
	        "TIME(p.checked_out_date_time) AS checkedOutTime, " +
	        "p.checked_in_date_time AS checkedInDateTime, " +
	        "p.checked_out_date_time AS checkedOutDateTime, " +
	        "vt.name AS bondItemName, " +
	        "br.qty * 30 AS days, " +
	        "pbr.discount AS discount, " +
	        "z.name AS bondZoneName, " +
	        "brc.amount AS amount, " +
	        "c.pay_code AS payCode, " +
	        "c.collection_date_time AS dateTime, " +
	        "u.nickname AS cashierName, " +
	        "ua.nickname AS discountApprovedBy, " +
	        "pbr.discount_approved_date_time AS discountApprovedDateTime, " +
	        "DATE(pbr.discount_approved_date_time) AS discountApprovedDate, " +
	        "TIME(pbr.discount_approved_date_time) AS discountApprovedTime " +
	        "FROM bill_receivable_collections brc " +
	        "JOIN bill_receivables br ON brc.bill_receivable_id = br.id " +
	        "JOIN collections c ON brc.collection_id = c.id " +
	        "JOIN users u ON c.collected_by_user_id = u.id " +
	        "JOIN bond_item_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
	        "JOIN bond_items p ON pbr.bond_item_id = p.id " +
	        "JOIN bond_zones z ON p.bond_zone_id = z.id " +
	        "JOIN bond_item_types vt ON p.bond_item_type_id = vt.id " +
	        "LEFT JOIN users ua ON pbr.discount_approved_by_user_id = ua.id " +
	        "WHERE (:bondZoneId IS NULL OR p.bond_zone_id = :bondZoneId) " +
	        "AND c.collection_date_time BETWEEN :startDate AND :endDate",
	        nativeQuery = true)
	List<IBondItemCollection> findBondItemCollectionsBetweenDates(
	        @Param("startDate") LocalDateTime startDate,
	        @Param("endDate") LocalDateTime endDate,
	        @Param("bondZoneId") Long bondZoneId
	);

}

interface IBillReceivableCollection {
	String getReason();
	double getAmount();
	String getPayCode();
	String getCashierName();
}

interface IParkingCollection {
    String getAmount();
    String getPayCode();
    String getDateTime();
    String getReason();
    String getVehicleEquipmentCategory();
    String getVehicleEquipmentName();
    String getOwnerFirstName();
    String getOwnerLastName();
    String getCardNo();
    String getOwnerPhoneNo();
    String getChasisNo();
    String getCreatedDateTime();
    double getDays();
    double getDiscount();
    String getCashierName();
}

interface IParkingServiceCollection {
    String getAmount();
    String getPayCode();
    String getDateTime();
    String getReason();
    String getVehicleEquipmentCategory();
    String getVehicleEquipmentName();
    String getOwnerFirstName();
    String getOwnerLastName();
    String getCardNo();
    String getOwnerPhoneNo();
    String getChasisNo();
    String getCreatedDateTime();
    double getQty();
    String getServiceDescription();
    double getDiscount();
    String getCashierName();
}

interface ISalesCollection {
	String getProductName();
    String getAmount();
    String getPayCode();
    String getDateTime();
    String getCreatedDateTime();
    double getQty();
    double getDiscount();
    String getCashierName();
}

interface IRestaurantSalesCollection {
	String getProductName();
    String getAmount();
    String getPayCode();
    String getDateTime();
    String getCreatedDateTime();
    double getQty();
    double getDiscount();
    String getCashierName();
}

interface IStorageCollection {
    String getAmount();
    String getPayCode();
    String getDateTime();
    String getReason();
    String getGoodName();
    String getOwnerFirstName();
    String getOwnerLastName();
    String getOwnerPhoneNo();
    String getCreatedDateTime();
    double getDays();
    double getDiscount();
    String getCashierName();
}

interface IBondItemCollection {
    double getAmount();
    String getPayCode();
    String getDateTime();
    String getReason();
    String getBondItemName();
    String getChasisNo();
    String getOwnerFirstName();
    String getOwnerLastName();
    String getOwnerPhoneNo();
    String getCreatedDateTime();
    String getCheckedInDate();
    String getCheckedOutDate();
    Double getDays();
    double getDiscount();
    String getDiscountApprovedBy();
    String getCashierName();
    Long getBondZoneId();
    String getBondZoneName();
}

interface IMaintenanceCollection {
	String getIssueName();
    String getAmount();
    String getPayCode();
    String getDateTime();
    String getReason();
    String getVehicleEquipmentCategory();
    String getVehicleEquipmentName();
    String getOwnerFirstName();
    String getOwnerLastName();
    String getCardNo();
    String getOwnerPhoneNo();
    String getChasisNo();
    String getCreatedDateTime();
    double getDays();
    double getDiscount();
    String getCashierName();
}

interface IWeighCollection {
    String getAmount();
    String getPayCode();
    String getDateTime();
    String getReason();
    String getRegNo();
    String getOwnerFirstName();
    String getOwnerLastName();
    String getOwnerPhoneNo();
    String getCreatedDateTime();
    double getDiscount();
    String getCashierName();
    String getWeightOne();
    String getWeightTwo();
    String getWeightThree();
    String getWeightFour();
}

interface IWorkshopCollection {
	String getSn();
    String getAmount();
    String getPayCode();
    String getDateTime();
    String getReason();
    String getRegNo();
    String getOwnerName();
    String getMachineName();
    String getServiceName();
    String getOwnerPhoneNo();
    String getCreatedDateTime();
    String getCashierName();
    String getWeightOne();
    String getWeightTwo();
    String getWeightThree();
    String getWeightFour();
}


