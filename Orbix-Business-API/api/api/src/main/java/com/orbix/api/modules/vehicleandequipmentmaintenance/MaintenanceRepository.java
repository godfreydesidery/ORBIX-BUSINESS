package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipment;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

	List<Maintenance> findAllByStatusIn(List<String> statuses);

	List<Maintenance> findAllByStatusInAndCheckedOutDateTimeBetween(List<String> statuses, LocalDateTime before,
			LocalDateTime now);

	List<Maintenance> findAllByVehicleEquipmentAndStatusIn(VehicleEquipment vehicleEquipment, List<String> statuses);
	
	@Query("SELECT DISTINCT m FROM Maintenance m JOIN m.maintenanceJobCards mjc JOIN mjc.maintenanceJobCardIssues mjci WHERE mjci.status = 'OPEN' AND m.status IN :statuses")
	List<Maintenance> findAllByStatusInAndOpenMaintenanceJobCardIssues(@Param("statuses") List<String> statuses);

}
