package com.orbix.api.modules.salesandmarketing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantBadgeRepository extends JpaRepository<RestaurantBadge, Long> {
	
	RestaurantBadge findTopByOrderByIdDesc();

	@Query("SELECT MAX(b.id) FROM RestaurantBadge b")
	Long getMaxId();

	List<RestaurantBadge> findAllByRestaurant(Restaurant restaurant);

	Optional<RestaurantBadge> findByCode(String restaurantBadgeCode);

	// A restaurant's badges, searched on the badge code (the only text the badge list shows)
	@Query("SELECT b FROM RestaurantBadge b WHERE b.restaurant = :restaurant AND (:search = '%%' OR LOWER(b.code) LIKE :search)")
	Page<RestaurantBadge> getPageByRestaurant(@Param("restaurant") Restaurant restaurant, @Param("search") String search, Pageable pageable);
}
