package com.orbix.api.modules.bond;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Branch;

public interface BondZoneRepository extends JpaRepository<BondZone, Long> {

	List<BondZone> findAllByBranchAndActive(Branch branch, boolean b);

	Optional<BondZone> findByNameAndBranch(String bondZoneName, Branch branch);

	List<BondZone> findAllByBranch(Branch branch);

	Optional<BondZone> findByIdAndBranch(Long bondZoneId, Branch branch);

}
