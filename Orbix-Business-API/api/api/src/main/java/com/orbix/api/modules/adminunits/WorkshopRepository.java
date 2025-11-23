package com.orbix.api.modules.adminunits;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.identityandaccess.User;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {
	
	List<Workshop> findAllByBranch(Branch branch);

	Optional<Workshop> findByIdAndBranch(Long id, User user);
}
