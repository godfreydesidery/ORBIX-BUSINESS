package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.inventoryandprocurement.Dineable;

public interface RestaurantSalesOrderDetailRepository extends JpaRepository<RestaurantSalesOrderDetail, Long> {
	
	List<RestaurantSalesOrderDetail> findAllByRestaurantSalesOrder(RestaurantSalesOrder restaurantSalesOrder);

	boolean existsByRestaurantSalesOrderAndDineable(RestaurantSalesOrder restaurantSalesOrder, Dineable dineable);
}
