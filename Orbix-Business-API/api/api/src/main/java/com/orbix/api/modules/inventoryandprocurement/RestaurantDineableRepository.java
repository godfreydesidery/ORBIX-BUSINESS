package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantDineableRepository extends JpaRepository<RestaurantDineable, Long> {
	List<RestaurantDineable> findAllByRestaurant(Object object);
	Optional<RestaurantDineable> findByIdAndRestaurant(Long id, Restaurant restaurant);
	Optional<RestaurantDineable> findByRestaurantAndDineable(Restaurant restaurant, Dineable dineable);
	boolean existsByRestaurantAndDineable(Restaurant restaurant, Dineable dineable);
	List<RestaurantDineable> findAllByRestaurantAndDineable_NameContainingIgnoreCase(Restaurant restaurant, String name);
	Optional<RestaurantDineable> findByDineableAndRestaurant(Dineable dineable, Restaurant restaurant);
	@Query("SELECT rd.dineable.id FROM RestaurantDineable rd WHERE rd.restaurant = :restaurant")
	List<Long> getDineableIdsByRestaurant(@Param("restaurant") Restaurant restaurant);

	// A restaurant's dineables, searched on the dineable columns the screens show
	@Query("SELECT x FROM RestaurantDineable x LEFT JOIN x.dineable d WHERE x.restaurant = :restaurant AND (:search = '%%' OR LOWER(d.code) LIKE :search OR LOWER(d.name) LIKE :search OR LOWER(d.description) LIKE :search OR LOWER(d.baseUom) LIKE :search)")
	Page<RestaurantDineable> getPageByRestaurant(@Param("restaurant") Restaurant restaurant, @Param("search") String search, Pageable pageable);
}
