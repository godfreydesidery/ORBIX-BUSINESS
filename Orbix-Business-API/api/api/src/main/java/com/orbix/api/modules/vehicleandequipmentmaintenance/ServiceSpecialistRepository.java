package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.identityandaccess.User;

public interface ServiceSpecialistRepository extends JpaRepository<ServiceSpecialist, Long> {

	List<ServiceSpecialist> findAllByBranchAndActive(Branch branch, boolean b);

	boolean existsByUserAndBranch(User user, Branch branch);

}
