package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipment;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

	List<Maintenance> findAllByStatusIn(List<String> statuses);

	List<Maintenance> findAllByStatusInAndCheckedOutDateTimeBetween(List<String> statuses, LocalDateTime before,
			LocalDateTime now);

	List<Maintenance> findAllByVehicleEquipmentAndStatusIn(VehicleEquipment vehicleEquipment, List<String> statuses);

}
