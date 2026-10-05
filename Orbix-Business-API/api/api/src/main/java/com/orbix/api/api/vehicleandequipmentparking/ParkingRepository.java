package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.api.commons.PayStatus;

public interface ParkingRepository extends JpaRepository<Parking, Long> {

	List<Parking> findAllByStatusIn(List<String> statuses);

	List<Parking> findAllByVehicleEquipmentAndStatusIn(VehicleEquipment vehicleEquipment, List<String> statuses);

	@Query("SELECT p.id FROM Parking p WHERE p.vehicleEquipment = :vehicleEquipment AND p.status IN :statuses ORDER BY p.id")
	List<Long> getIdsByVehicleEquipmentAndStatusIn(@Param("vehicleEquipment") VehicleEquipment vehicleEquipment, @Param("statuses") List<String> statuses);
	
	
	@Query("SELECT COUNT(p) FROM Parking p WHERE p.checkedInDateTime BETWEEN :startDate AND :endDate AND status IN :statuses")
    long countByDateRangeAndRegistered(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, List<String> statuses);

	@Query("SELECT COUNT(p) FROM Parking p WHERE p.checkedOutDateTime BETWEEN :startDate AND :endDate AND status IN :statuses")
    long countByDateRangeAndCheckedOut(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, List<String> statuses);

	
	@Query("SELECT COUNT(p) FROM Parking p WHERE p.status = 'CHECKED-IN'")
    long countRegistered();

	List<Parking> findAllByCreatedByUserAndCreatedDateTimeBetweenAndStatusIn(User user, LocalDateTime atStartOfDay,
			LocalDateTime plusDays, List<String> statuses);

	List<Parking> findAllByCreatedDateTimeBetweenAndStatusIn(LocalDateTime atStartOfDay, LocalDateTime plusDays,
			List<String> statuses);
	
	List<Parking> findAllByCheckedInByUserAndCheckedInDateTimeBetweenAndStatusIn(User user, LocalDateTime atStartOfDay,
			LocalDateTime plusDays, List<String> statuses);
	
	List<Parking> findAllByCheckedInDateTimeBetweenAndStatusIn(LocalDateTime atStartOfDay, LocalDateTime plusDays,
			List<String> statuses);

	List<Parking> findAllByStatusInAndCheckedOutDateTimeBetween(List<String> statuses, LocalDateTime startOfYesterday,
			LocalDateTime endOfYesterday);

	//boolean existsByChasisNoAndStatus(String chasisNo, String string);

	boolean existsByChasisNoAndStatusIn(String chasisNo, List<String> statuses);
	
	
	@Query(value = 
		    "SELECT " +
		    "    months.month AS month, " +
		    "    COALESCE(checked_in.count, 0) AS checkedIn, " +
		    "    COALESCE(checked_out.count, 0) AS checkedOut " +
		    "FROM ( " +
		    "    SELECT 1 AS month UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 " +
		    "    UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 " +
		    ") AS months " +
		    "LEFT JOIN ( " +
		    "    SELECT MONTH(checked_in_date_time) AS month, COUNT(*) AS count " +
		    "    FROM parkings " +
		    "    WHERE checked_in_date_time IS NOT NULL AND YEAR(checked_in_date_time) = :year " +
		    "    GROUP BY MONTH(checked_in_date_time) " +
		    ") AS checked_in ON checked_in.month = months.month " +
		    "LEFT JOIN ( " +
		    "    SELECT MONTH(checked_out_date_time) AS month, COUNT(*) AS count " +
		    "    FROM parkings " +
		    "    WHERE checked_out_date_time IS NOT NULL AND YEAR(checked_out_date_time) = :year " +
		    "    GROUP BY MONTH(checked_out_date_time) " +
		    ") AS checked_out ON checked_out.month = months.month " +
		    "ORDER BY months.month",
		    nativeQuery = true)
		List<Object[]> getMonthlyStats(@Param("year") int year);

	// Report rows read only the columns the report shows, instead of loading whole entities with their eager relations
	@Query("SELECT p.chasisNo AS chasisNo, t.name AS vehicleEquipmentTypeName, p.createdDateTime AS createdDateTime, u.nickname AS createdByNickname, p.hasKeys AS hasKeys FROM Parking p LEFT JOIN p.vehicleEquipmentType t LEFT JOIN p.createdByUser u WHERE p.createdDateTime BETWEEN :from AND :to AND p.status IN :statuses")
	List<IParkingRegistration> getRegistrationReport(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("statuses") List<String> statuses);

	@Query("SELECT p.chasisNo AS chasisNo, t.name AS vehicleEquipmentTypeName, p.createdDateTime AS createdDateTime, u.nickname AS createdByNickname, p.hasKeys AS hasKeys FROM Parking p LEFT JOIN p.vehicleEquipmentType t LEFT JOIN p.createdByUser u WHERE p.createdByUser = :user AND p.createdDateTime BETWEEN :from AND :to AND p.status IN :statuses")
	List<IParkingRegistration> getRegistrationReportByCreatedByUser(@Param("user") User user, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("statuses") List<String> statuses);

	// Search on the columns the parking lists show (p is the parking, t its vehicle or equipment type)
	String PARKING_SEARCH = "(:search = '%%' OR LOWER(p.no) LIKE :search OR LOWER(p.chasisNo) LIKE :search"
			+ " OR LOWER(p.ownerFirstName) LIKE :search OR LOWER(p.ownerMiddleName) LIKE :search OR LOWER(p.ownerLastName) LIKE :search"
			+ " OR LOWER(p.ownerPhoneNo) LIKE :search OR LOWER(p.agentName) LIKE :search OR LOWER(p.cardNo) LIKE :search"
			+ " OR LOWER(p.billingType) LIKE :search OR LOWER(p.status) LIKE :search OR LOWER(t.name) LIKE :search)";

	@Query("SELECT p FROM Parking p LEFT JOIN p.vehicleEquipmentType t WHERE p.status IN :statuses AND " + PARKING_SEARCH)
	Page<Parking> getPageByStatusIn(@Param("statuses") List<String> statuses, @Param("search") String search, Pageable pageable);

	// Parkings with at least one bill whose discount has the given status
	@Query("SELECT p FROM Parking p LEFT JOIN p.vehicleEquipmentType t WHERE p.status IN :statuses"
			+ " AND EXISTS (SELECT b.id FROM ParkingBillReceivable b WHERE b.parking = p AND b.discountStatus = :discountStatus) AND " + PARKING_SEARCH)
	Page<Parking> getPageByStatusInAndDiscountStatus(@Param("statuses") List<String> statuses, @Param("discountStatus") String discountStatus, @Param("search") String search, Pageable pageable);

	// Checked out in the period and cleared: no parking or service bill that is not paid
	@Query("SELECT p FROM Parking p LEFT JOIN p.vehicleEquipmentType t WHERE p.status IN :statuses AND p.checkedOutDateTime BETWEEN :from AND :to"
			+ " AND NOT EXISTS (SELECT b.id FROM ParkingBillReceivable b WHERE b.parking = p AND (b.billReceivable.payStatus IS NULL OR b.billReceivable.payStatus <> :paid))"
			+ " AND NOT EXISTS (SELECT sb.id FROM ParkingServiceBillReceivable sb WHERE sb.parking = p AND (sb.billReceivable.payStatus IS NULL OR sb.billReceivable.payStatus <> :paid))"
			+ " AND " + PARKING_SEARCH)
	Page<Parking> getClearedPageByStatusInAndCheckedOutDateTimeBetween(@Param("statuses") List<String> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("paid") PayStatus paid, @Param("search") String search, Pageable pageable);
}

interface IParkingRegistration {
	String getChasisNo();
	String getVehicleEquipmentTypeName();
	LocalDateTime getCreatedDateTime();
	String getCreatedByNickname();
	boolean getHasKeys();
}
