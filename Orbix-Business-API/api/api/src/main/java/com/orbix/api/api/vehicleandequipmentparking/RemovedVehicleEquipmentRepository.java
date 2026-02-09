package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RemovedVehicleEquipmentRepository extends JpaRepository<RemovedVehicleEquipment, Long> {
	
	List<RemovedVehicleEquipment> findAllByCreatedDateTimeBetween(LocalDateTime atStartOfDay, LocalDateTime atTime);

}
