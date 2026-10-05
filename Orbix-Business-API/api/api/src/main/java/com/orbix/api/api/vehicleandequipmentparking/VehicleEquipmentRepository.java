package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.orbix.api.modules.adminunits.Branch;

public interface VehicleEquipmentRepository extends JpaRepository<VehicleEquipment, Long> {

	List<VehicleEquipment> findAllByActiveTrue();

	List<VehicleEquipment> findTop2000ByActiveTrue();

	@Query("SELECT v.chasisNo FROM VehicleEquipment v WHERE v.active = true ORDER BY v.id")
	List<String> getActiveChasisNos(Pageable pageable);

	//Optional<VehicleEquipment> findByChasisNoAndActiveTrue(String chasisNo);
	
	Optional<VehicleEquipment> findFirstByChasisNoAndActiveTrue(String chasisNo);

	List<VehicleEquipment> findAllByBranchAndChasisNoContainingIgnoreCase(Branch userBranch, String chasisNoLike);

}
