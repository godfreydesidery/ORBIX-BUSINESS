package com.orbix.api.modules.salesandmarketing;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantAgentRepository extends JpaRepository<RestaurantAgent, Long> {

	List<RestaurantAgent> findAllByRestaurant(Restaurant restaurant);
	
	List<RestaurantAgent> findAllByRestaurantAndRestaurantBadgeIsNotNull(Restaurant restaurant);
	
	boolean existsByRestaurantBadgeId(Long restaurantBadgeId);

	List<RestaurantAgent> findAllByRestaurantAndRestaurantBadgeNotNull(Restaurant restaurant);

	@Query("SELECT a.name FROM RestaurantAgent a ORDER BY a.id")
	List<String> getNames();

	// A restaurant's agents, searched on the columns the agent list shows (b is the agent's badge)
	@Query("SELECT a FROM RestaurantAgent a LEFT JOIN a.restaurantBadge b WHERE a.restaurant = :restaurant"
			+ " AND (:search = '%%' OR LOWER(a.name) LIKE :search OR LOWER(a.phoneNo) LIKE :search OR LOWER(b.code) LIKE :search)")
	Page<RestaurantAgent> getPageByRestaurant(@Param("restaurant") Restaurant restaurant, @Param("search") String search, Pageable pageable);
}
