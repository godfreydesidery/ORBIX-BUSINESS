package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.Shop;

public interface RestaurantSalesOrderRepository extends JpaRepository<RestaurantSalesOrder, Long> {
	List<RestaurantSalesOrder> findAllByRestaurantAndStatus(Restaurant restaurant, WorkFlowStatus pending);
}
