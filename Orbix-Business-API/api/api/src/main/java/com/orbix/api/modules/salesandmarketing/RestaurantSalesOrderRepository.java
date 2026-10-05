package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.Shop;

public interface RestaurantSalesOrderRepository extends JpaRepository<RestaurantSalesOrder, Long> {
	List<RestaurantSalesOrder> findAllByRestaurantAndStatus(Restaurant restaurant, WorkFlowStatus pending);
	
	List<RestaurantSalesOrder> findAllByRestaurantAndStatusAndCreatedDateTimeAfter(
            Restaurant restaurant,
            WorkFlowStatus status,
            LocalDateTime createdDateTime
    );

	// Same rows as findAllByRestaurantAndStatusAndCreatedDateTimeAfter, one page at a time, searched on the columns
	// the order list shows (ag is the order's agent, bd its badge)
	@Query("SELECT o FROM RestaurantSalesOrder o LEFT JOIN o.restaurantAgent ag LEFT JOIN o.restaurantBadge bd"
			+ " WHERE o.restaurant = :restaurant AND o.status = :status AND o.createdDateTime > :createdAfter"
			+ " AND (:search = '%%' OR LOWER(o.no) LIKE :search OR LOWER(o.customerName) LIKE :search OR LOWER(ag.name) LIKE :search"
			+ " OR LOWER(bd.code) LIKE :search OR LOWER(str(o.status)) LIKE :search)")
	Page<RestaurantSalesOrder> getPageByRestaurantAndStatusAndCreatedDateTimeAfter(@Param("restaurant") Restaurant restaurant, @Param("status") WorkFlowStatus status, @Param("createdAfter") LocalDateTime createdAfter, @Param("search") String search, Pageable pageable);
}
