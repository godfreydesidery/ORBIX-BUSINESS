package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleEquipmentRepository extends JpaRepository<VehicleEquipment, Long> {

	List<VehicleEquipment> findAllByActiveTrue();
	
	List<VehicleEquipment> findTop2000ByActiveTrue();

	//Optional<VehicleEquipment> findByChasisNoAndActiveTrue(String chasisNo);
	
	Optional<VehicleEquipment> findFirstByChasisNoAndActiveTrue(String chasisNo);

}
