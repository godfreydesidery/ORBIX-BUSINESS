package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantDineableRepository extends JpaRepository<RestaurantDineable, Long> {
	List<RestaurantDineable> findAllByRestaurant(Object object);
	Optional<RestaurantDineable> findByIdAndRestaurant(Long id, Restaurant restaurant);
	Optional<RestaurantDineable> findByRestaurantAndDineable(Restaurant restaurant, Dineable dineable);
	boolean existsByRestaurantAndDineable(Restaurant restaurant, Dineable dineable);
	List<RestaurantDineable> findAllByRestaurantAndDineable_NameContainingIgnoreCase(Restaurant restaurant, String name);
	Optional<RestaurantDineable> findByDineableAndRestaurant(Dineable dineable, Restaurant restaurant);
}
