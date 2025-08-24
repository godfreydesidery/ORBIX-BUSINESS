package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.inventoryandprocurement.Dineable;

public interface RestaurantSalesOrderDetailRepository extends JpaRepository<RestaurantSalesOrderDetail, Long> {
	
	List<RestaurantSalesOrderDetail> findAllByRestaurantSalesOrder(RestaurantSalesOrder restaurantSalesOrder);

	boolean existsByRestaurantSalesOrderAndDineable(RestaurantSalesOrder restaurantSalesOrder, Dineable dineable);
	
	@Query(value =
		    "SELECT " +
		    "   d.name AS dineableName, " +
		    "   SUM(rsod.qty) AS qty, " +
		    "   SUM(rsod.qty * rsod.sellingPriceVatIncl) AS amount, " +
		    "   FUNCTION('DATE_FORMAT', rso.confirmedDateTime, '%Y-%m-%d %H:%i:%s') AS dateTime, " +
		    "   COALESCE(ra.name, '') AS agentName, " +        // ensure non-null
		    "   COALESCE(u.nickname, '') AS confirmedBy " +   // ensure non-null
		    "FROM RestaurantSalesOrderDetail rsod " +
		    "JOIN rsod.dineable d " +
		    "JOIN rsod.restaurantSalesOrder rso " +
		    "LEFT JOIN rso.restaurantAgent ra " +           // optional agent
		    "LEFT JOIN rso.confirmedByUser u " +           // optional confirmedBy
		    "WHERE rso.status = 'CONFIRMED' " +
		    "AND rso.confirmedDateTime BETWEEN :startDate AND :endDate " +
		    "AND (:restaurantId IS NULL OR rso.restaurant.id = :restaurantId) " +
		    "AND (:agent_name IS NULL OR ra.name = :agent_name) " +
		    "AND (:nickname IS NULL OR u.nickname = :nickname) " +
		    "GROUP BY d.name, rso.confirmedDateTime, ra.name, u.nickname " +
		    "ORDER BY rso.confirmedDateTime DESC"
		)
		List<IRestaurantSaleListingReport> getRestaurantSalesListingReport(
		        @Param("restaurantId") Long restaurantId,
		        @Param("startDate") LocalDateTime startDate,
		        @Param("endDate") LocalDateTime endDate,
		        @Param("agent_name") String agentName,
		        @Param("nickname") String confirmedBy 
		);


}

interface IRestaurantSaleListingReport {
	String getDineableName();
	double getQty();
	double getAmount();
	String getDateTime();
	String getAgentName();
	String getConfirmedBy();
}

