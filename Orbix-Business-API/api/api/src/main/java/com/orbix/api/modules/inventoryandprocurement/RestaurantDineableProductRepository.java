package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantDineableProductRepository extends JpaRepository<RestaurantDineableProduct, Long> {

	List<RestaurantDineableProduct> findAllByRestaurantAndDineable(Restaurant restaurant, Dineable dineable);

	boolean existsByRestaurantAndDineableAndProduct(Restaurant restaurant, Dineable dineable, Product product);

}
