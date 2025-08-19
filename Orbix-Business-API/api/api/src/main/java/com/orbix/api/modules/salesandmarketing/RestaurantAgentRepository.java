package com.orbix.api.modules.salesandmarketing;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantAgentRepository extends JpaRepository<RestaurantAgent, Long> {

	List<RestaurantAgent> findAllByRestaurant(Restaurant restaurant);
	
	List<RestaurantAgent> findAllByRestaurantAndRestaurantBadgeIsNotNull(Restaurant restaurant);
	
	boolean existsByRestaurantBadgeId(Long restaurantBadgeId);

	List<RestaurantAgent> findAllByRestaurantAndRestaurantBadgeNotNull(Restaurant restaurant);

}
