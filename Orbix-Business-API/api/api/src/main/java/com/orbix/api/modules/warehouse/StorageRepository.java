package com.orbix.api.modules.warehouse;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.identityandaccess.User;

public interface StorageRepository extends JpaRepository<Storage, Long> {

	List<Storage> findAllByStatusIn(List<String> statuses);

	List<Storage> findAllByStatusInAndCheckedOutDateTimeBetween(List<String> statuses, LocalDateTime startOfToday,
			LocalDateTime endOfYesterday);

	List<Storage> findAllByWarehouseAndStatusIn(Warehouse warehouse, List<String> statuses);
	
	/////////////////////////////////
	

//	List<Storage> findAllByVehicleEquipmentAndStatusIn(VehicleEquipment vehicleEquipment, List<String> statuses);
	
	
	@Query("SELECT COUNT(p) FROM Storage p WHERE p.checkedInDateTime BETWEEN :startDate AND :endDate AND status IN :statuses")
    long countByDateRangeAndRegistered(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, List<String> statuses);

	@Query("SELECT COUNT(p) FROM Storage p WHERE p.checkedOutDateTime BETWEEN :startDate AND :endDate AND status IN :statuses")
    long countByDateRangeAndCheckedOut(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, List<String> statuses);

	
	@Query("SELECT COUNT(p) FROM Storage p WHERE p.status = 'CHECKED-IN'")
    long countRegistered();

	List<Storage> findAllByCreatedByUserAndCreatedDateTimeBetweenAndStatusIn(User user, LocalDateTime atStartOfDay,
			LocalDateTime plusDays, List<String> statuses);

	List<Storage> findAllByCreatedDateTimeBetweenAndStatusIn(LocalDateTime atStartOfDay, LocalDateTime plusDays,
			List<String> statuses);
	
	List<Storage> findAllByCheckedInByUserAndCheckedInDateTimeBetweenAndStatusIn(User user, LocalDateTime atStartOfDay,
			LocalDateTime plusDays, List<String> statuses);
	
	List<Storage> findAllByCheckedInDateTimeBetweenAndStatusIn(LocalDateTime atStartOfDay, LocalDateTime plusDays,
			List<String> statuses);

	/////////////////////////////////
	
	List<Storage> findAllByWarehouseAndStatusInAndCheckedOutDateTimeAfter(Warehouse warehouse, List<String> statuses, LocalDateTime checkedOutAfter);

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
		    "    FROM storages " +
		    "    WHERE checked_in_date_time IS NOT NULL AND YEAR(checked_in_date_time) = :year " +
		    "    GROUP BY MONTH(checked_in_date_time) " +
		    ") AS checked_in ON checked_in.month = months.month " +
		    "LEFT JOIN ( " +
		    "    SELECT MONTH(checked_out_date_time) AS month, COUNT(*) AS count " +
		    "    FROM storages " +
		    "    WHERE checked_out_date_time IS NOT NULL AND YEAR(checked_out_date_time) = :year " +
		    "    GROUP BY MONTH(checked_out_date_time) " +
		    ") AS checked_out ON checked_out.month = months.month " +
		    "ORDER BY months.month",
		    nativeQuery = true)
		List<Object[]> getMonthlyStats(@Param("year") int year);

	int countByStatus(String string);

	// Report rows read only the columns the report shows, instead of loading whole entities with their eager relations
	@Query("SELECT p.createdDateTime AS createdDateTime, u.nickname AS createdByNickname FROM Storage p LEFT JOIN p.createdByUser u WHERE p.createdDateTime BETWEEN :from AND :to AND p.status IN :statuses")
	List<IStorageRegistration> getRegistrationReport(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("statuses") List<String> statuses);

	@Query("SELECT p.createdDateTime AS createdDateTime, u.nickname AS createdByNickname FROM Storage p LEFT JOIN p.createdByUser u WHERE p.createdByUser = :user AND p.createdDateTime BETWEEN :from AND :to AND p.status IN :statuses")
	List<IStorageRegistration> getRegistrationReportByCreatedByUser(@Param("user") User user, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("statuses") List<String> statuses);

	@Query("SELECT p.no AS no, p.goodName AS goodName, p.ownerFirstName AS ownerFirstName, p.ownerLastName AS ownerLastName, p.ownerPhoneNo AS ownerPhoneNo, "
			+ "p.billingAmount AS billingAmount, p.initialQty AS initialQty, p.billingType AS billingType, p.checkedInDateTime AS checkedInDateTime, p.checkedOutDateTime AS checkedOutDateTime, "
			+ "p.status AS status, u.nickname AS createdByNickname, ciu.nickname AS checkedInByNickname, cou.nickname AS checkedOutByNickname "
			+ "FROM Storage p LEFT JOIN p.createdByUser u LEFT JOIN p.checkedInByUser ciu LEFT JOIN p.checkedOutByUser cou "
			+ "WHERE p.checkedInDateTime BETWEEN :from AND :to AND p.status IN :statuses")
	List<IStorageReportRow> getStorageReport(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("statuses") List<String> statuses);
}

interface IStorageRegistration {
	LocalDateTime getCreatedDateTime();
	String getCreatedByNickname();
}

interface IStorageReportRow {
	String getNo();
	String getGoodName();
	String getOwnerFirstName();
	String getOwnerLastName();
	String getOwnerPhoneNo();
	double getBillingAmount();
	double getInitialQty();
	String getBillingType();
	LocalDateTime getCheckedInDateTime();
	LocalDateTime getCheckedOutDateTime();
	String getStatus();
	String getCreatedByNickname();
	String getCheckedInByNickname();
	String getCheckedOutByNickname();
}
