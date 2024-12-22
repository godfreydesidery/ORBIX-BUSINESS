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


