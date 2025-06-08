package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipmentType;

public interface BillReceivableCollectionRepository extends JpaRepository<BillReceivableCollection, Long> {
	
	@Query(value = "SELECT " +
	        "p.chasis_no AS chasisNo, " +
	        "DATE(p.checked_in_date_time) AS checkedInDate, " +
	        "TIME(p.checked_in_date_time) AS checkedInTime, " +
	        "DATE(p.checked_out_date_time) AS checkedOutDate, " +
	        "TIME(p.checked_out_date_time) AS checkedOutTime, " +
	        "p.checked_in_date_time AS checkedInDateTime, " +
	        "p.checked_out_date_time AS checkedOutDateTime, " +
	        "vt.name AS vehicleEquipmentTypeName, " +
	        "br.qty AS qty, " +
	        "CASE " +
	        "   WHEN psbr.id IS NOT NULL THEN 'Service' " +
	        "   ELSE 'Parking' " +
	        "END AS serviceType, " +
	        "pbr.discount AS discount, " +
	        "brc.amount AS amount, " +
	        "c.pay_code AS payCode, " +
	        "u.nickname AS cashierName, " +
	        "ua.nickname AS discountApprovedBy, " +
	        "pbr.discount_approved_date_time AS discountApprovedDateTime, " +
	        "DATE(pbr.discount_approved_date_time) AS discountApprovedDate, " +
	        "TIME(pbr.discount_approved_date_time) AS discountApprovedTime " +
	        "FROM " +
	        "bill_receivable_collections brc " +
	        "JOIN " +
	        "bill_receivables br ON brc.bill_receivable_id = br.id " +
	        "JOIN " +
	        "collections c ON brc.collection_id = c.id " +
	        "JOIN " +
	        "users u ON c.collected_by_user_id = u.id " +
	        "JOIN " +
	        "parking_bill_receivables pbr ON pbr.bill_receivable_id = br.id " +
	        "JOIN " +
	        "parkings p ON pbr.parking_id = p.id " +
	        "JOIN " +
	        "vehicle_equipment_types vt ON p.vehicle_and_equipment_type_id = vt.id " +
	        "LEFT JOIN " +
	        "parking_service_bill_receivables psbr ON psbr.bill_receivable_id = br.id " + 
	        "LEFT JOIN users ua ON pbr.discount_approved_by_user_id = ua.id " +  
	        "WHERE " +
	        "(:cashierNickname = '' OR u.nickname = :cashierNickname) " +  // Filter by cashier's nickname
	        "AND c.collection_date_time BETWEEN :startDate AND :endDate",  // Filter by collection date
	        nativeQuery = true)
	List<ICashierCollection> getCashierCollectionsByDateAndCashier(
	    @Param("startDate") LocalDateTime startDate,
	    @Param("endDate") LocalDateTime endDate,
	    @Param("cashierNickname") String cashierNickname
	);
}

interface ICashierCollection {
	String getChasisNo(); // from parking
	String getCheckedInDateTime();
	String getCheckedOutDateTime();
	String getCheckedInDate();
	String getCheckedInTime();
	String getCheckedOutDate();
	String getCheckedOutTime();
	String getVehicleEquipmentTypeName(); // from parking/vehicleequipment type name
	String getQty(); // from bill receivable
	String getServiceType(); // if parkingservice, then service, if parking then parking
	String getAmount(); // from billreceivablecollection
	String getDiscount();
	String getPayCode();
	String getCashierName(); //nickname
	String getDiscountApprovedBy();
	String getDiscountApprovedDateTime();
	String getDiscountApprovedDate();
	String getDiscountApprovedTime();
}

// Primary entity billreceivable collection
