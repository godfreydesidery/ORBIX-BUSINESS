package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Branch;

public interface ParkingZoneRepository extends JpaRepository<ParkingZone, Long> {

	List<ParkingZone> findAllByBranchAndActive(Branch branch, boolean b);

	Optional<ParkingZone> findByNameAndBranch(String parkingZoneName, Branch branch);

}
