package com.orbix.api.modules.salesandmarketing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantBadgeRepository extends JpaRepository<RestaurantBadge, Long> {
	
	RestaurantBadge findTopByOrderByIdDesc();

	List<RestaurantBadge> findAllByRestaurant(Restaurant restaurant);

	Optional<RestaurantBadge> findByCode(String restaurantBadgeCode);

	
}
