package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Branch;

public interface VehicleEquipmentRepository extends JpaRepository<VehicleEquipment, Long> {

	List<VehicleEquipment> findAllByActiveTrue();

	List<VehicleEquipment> findTop2000ByActiveTrue();

	@Query("SELECT v.chasisNo FROM VehicleEquipment v WHERE v.active = true ORDER BY v.id")
	List<String> getActiveChasisNos(Pageable pageable);

	//Optional<VehicleEquipment> findByChasisNoAndActiveTrue(String chasisNo);
	
	Optional<VehicleEquipment> findFirstByChasisNoAndActiveTrue(String chasisNo);

	/** A vehicle with this chassis in the branch that has never had a parking: registered, its inspection not yet submitted */
	@Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VehicleEquipment v WHERE v.chasisNo = :chasisNo AND v.branch = :branch"
			+ " AND NOT EXISTS (SELECT p.id FROM Parking p WHERE p.vehicleEquipment = v)")
	boolean existsAwaitingInspection(@Param("chasisNo") String chasisNo, @Param("branch") Branch branch);

	List<VehicleEquipment> findAllByBranchAndChasisNoContainingIgnoreCase(Branch userBranch, String chasisNoLike);

}
