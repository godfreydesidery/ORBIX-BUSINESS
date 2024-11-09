package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CashCollectionRepository extends JpaRepository<CashCollection, Long> {
	
	
	@Query(value = "SELECT " +
            "SUM(c.amount) AS amount, " +
            "c.reason AS reason, " +
            "c.payment_type AS paymentType, " +
            "u.nickname AS cashierName " +
        "FROM cash_collections c " +
        "JOIN users u ON u.id = c.collected_by_user_id " +
        "WHERE c.collection_date_time BETWEEN :startDate AND :endDate " +
        "AND (:nickname IS NULL OR :nickname = '' OR u.nickname = :nickname) " +
        "GROUP BY c.reason, c.payment_type", 
    nativeQuery = true)
	List<ICashCollection> findTotalCollectionByDateRangeAndCashier(
     @Param("startDate") LocalDateTime startDate, 
     @Param("endDate") LocalDateTime endDate, 
     @Param("nickname") String nickname);


	@Query(value = "SELECT " +
	        "SUM(c.amount) AS amount, " +
	        "c.reason AS reason, " +
	        "c.payment_type AS paymentType, " +
	        "u.nickname AS cashierName " +
	        "FROM cash_collections c " +
	        "JOIN users u ON u.id = c.collected_by_user_id " +
	        "WHERE c.collection_date_time BETWEEN :startDate AND :endDate " +
	        "GROUP BY c.reason, c.payment_type", 
	nativeQuery = true)
	List<ICashCollection> findTotalCollectionByDateRange(
	 @Param("startDate") LocalDateTime startDate, 
	 @Param("endDate") LocalDateTime endDate);

	

	@Query(value = "SELECT " +
	            "cc.amount AS amount, " +
	            "cc.payment_type AS paymentType, " +
	            "cc.collection_date_time AS dateTime, " +
	            "cc.reason AS reason, " +
	            "p.vehicle_equipment_category AS vehicleEquipmentCategory, " +
	            "p.vehicle_equipment_name AS vehicleEquipmentName, " +
	            "p.owner_first_name AS ownerFirstName, " +
	            "p.owner_last_name AS ownerLastName, " +
	            "p.card_no AS cardNo, " +
	            "p.owner_phone_no AS ownerPhoneNo, " +
	            "p.chasis_no AS chasisNo, " +
	            "p.created_date_time AS createdDateTime, " +
	            "pr.qty AS days, " +
	            "pr.discount AS discount, " +
	            "u.nickname AS cashierName " +
	        "FROM " +
	            "cash_collections cc " +
	        "JOIN " +
	            "bill_receivables br ON br.id = cc.bill_receivable_id " +
	        "JOIN " +
	            "parking_bill_receivables pr ON br.id = pr.bill_receivable_id " +
	        "JOIN " +
	            "parkings p ON p.id = pr.parking_id " +
	        "JOIN " +
	            "users u ON cc.collected_by_user_id = u.id " +
	        "WHERE " +
	            "cc.collection_date_time BETWEEN :startDate AND :endDate " 
	        , nativeQuery = true)
	    List<IParkingCashCollection> findCashCollectionsBetweenDates(
	            @Param("startDate") LocalDateTime startDate,
	            @Param("endDate") LocalDateTime endDate);
	
	
	@Query(value = "SELECT " +
            "cc.amount AS amount, " +
            "cc.payment_type AS paymentType, " +
            "cc.collection_date_time AS dateTime, " +
            "cc.reason AS reason, " +
            "p.vehicle_equipment_category AS vehicleEquipmentCategory, " +
            "p.vehicle_equipment_name AS vehicleEquipmentName, " +
            "p.owner_first_name AS ownerFirstName, " +
            "p.owner_last_name AS ownerLastName, " +
            "p.card_no AS cardNo, " +
            "p.owner_phone_no AS ownerPhoneNo, " +
            "p.chasis_no AS chasisNo, " +
            "p.created_date_time AS createdDateTime, " +
            "psbr.qty AS qty, " +
            "psbr.discount AS discount, " +
            "psbr.description AS serviceDescription, " +
            "u.nickname AS cashierName " +
        "FROM " +
            "cash_collections cc " +
        "JOIN " +
            "bill_receivables br ON br.id = cc.bill_receivable_id " +
        "JOIN " +
            "parking_service_bill_receivables psbr ON br.id = psbr.bill_receivable_id " +
        "JOIN " +
            "parkings p ON p.id = psbr.parking_id " +
        "JOIN " +
            "users u ON cc.collected_by_user_id = u.id " +
        "WHERE " +
            "cc.collection_date_time BETWEEN :startDate AND :endDate", 
        nativeQuery = true)
List<IParkingServiceCashCollection> findParkingServiceCashCollectionsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);


}

interface ICashCollection {
	String getReason();
	double getAmount();
	String getPaymentType();
	String getCashierName();
}

interface IParkingCashCollection {
    String getAmount();
    String getPaymentType();
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

interface IParkingServiceCashCollection {
    String getAmount();
    String getPaymentType();
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
