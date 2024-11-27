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

	List<Parking> findAllByStatusInAndCheckedOutDateTimeBetween(List<String> statuses, LocalDateTime startOfYesterday,
			LocalDateTime endOfYesterday);

	

}
