package com.orbix.api.modules.adminunits;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.identityandaccess.User;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
	
	List<Restaurant> findAllByBranch(Branch branch);

	Optional<Restaurant> findByIdAndBranch(Long id, User user);
}
