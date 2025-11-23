package com.orbix.api.modules.inventoryandprocurement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantProductLogRepository extends JpaRepository<RestaurantProductLog, Long> {
	
	@Query(value = 
	        "SELECT " +
	        "    spl.created_date_time AS dateTime, " +
	        "    p.name AS productName, " +	
	        "    spl.reference AS reference, " +	
	        "    spl.qty_in AS qtyIn, " +
	        "    spl.qty_out AS qtyOut, " +
	        "    spl.balance AS balance, " +
	        "    u.nickname AS nickname " +
	        "FROM restaurant_product_logs spl " +
	        "LEFT JOIN users u ON spl.created_by_user_id = u.id " +
	        "LEFT JOIN products p ON spl.product_id = p.id " +
	        "WHERE spl.restaurant_id = :restaurantId " +
	        "AND spl.created_date_time BETWEEN :startDateTime AND :endDateTime " +
	        "AND (:productId IS NULL OR spl.product_id = :productId) " +
	        "ORDER BY spl.created_date_time DESC",
	        nativeQuery = true)
	    List<RestaurantStockLogReportProjection> getStockLogReportByRestaurant(
	        @Param("restaurantId") Long restaurantId,
	        @Param("productId") Long productId,
	        @Param("startDateTime") LocalDateTime startDateTime,
	        @Param("endDateTime") LocalDateTime endDateTime
	    );

}

interface RestaurantStockLogReportProjection {
    String getDateTime();
    String getProductName();
    double getQtyIn();
    double getQtyOut();
    double getBalance();
    String getNickname();
    String getReference();
}
