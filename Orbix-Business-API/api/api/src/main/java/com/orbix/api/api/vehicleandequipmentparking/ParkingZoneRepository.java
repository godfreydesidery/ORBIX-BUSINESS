package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Branch;

public interface ParkingZoneRepository extends JpaRepository<ParkingZone, Long> {

	List<ParkingZone> findAllByBranchAndActive(Branch branch, boolean b);

	Optional<ParkingZone> findByNameAndBranch(String parkingZoneName, Branch branch);

	/** The zone, locked until the transaction ends, so two check-ins cannot both take its last space */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT z FROM ParkingZone z WHERE z.id = :id")
	Optional<ParkingZone> findByIdForUpdate(@Param("id") Long id);

}
