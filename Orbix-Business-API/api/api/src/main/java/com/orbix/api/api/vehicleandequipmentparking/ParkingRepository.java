package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.identityandaccess.User;

public interface ParkingRepository extends JpaRepository<Parking, Long> {

	List<Parking> findAllByStatusIn(List<String> statuses);

	List<Parking> findAllByVehicleEquipmentAndStatusIn(VehicleEquipment vehicleEquipment, List<String> statuses);
	
	
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




	

}
