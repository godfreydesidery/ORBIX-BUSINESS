package com.orbix.api.modules.warehouse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Branch;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

	List<Warehouse> findAllByBranchAndActive(Branch branch, boolean b);

	Optional<Warehouse> findByNameAndBranch(String warehouseName, Branch branch);

	List<Warehouse> findAllByBranch(Branch branch);

}
