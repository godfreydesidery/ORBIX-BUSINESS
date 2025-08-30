package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantSaleRepository extends JpaRepository<RestaurantSale, Long> {
	
	@Query(value = 
	        "SELECT " +
	        "    ROW_NUMBER() OVER (ORDER BY SUM(sd.qty) DESC) AS sn, " +
	        "    p.name AS dineableName, " +
	        "    SUM(sd.qty) AS qty, " +
	        "    CAST(SUM(sd.qty * sd.selling_price_vat_incl - sd.discount) AS CHAR) AS amount " +
	        "FROM restaurant_sale_details sd " +
	        "JOIN dineables p ON sd.dineable_id = p.id " +
	        "WHERE sd.restaurant_sale_id IN (SELECT s.id FROM restaurant_sales s WHERE s.created_date_time BETWEEN :startDate AND :endDate) " +
	        "GROUP BY p.name " +
	        "ORDER BY SUM(sd.qty) DESC",
	        nativeQuery = true
	    )
	List<IFastMovingDineable> getFastMovingDineablesByDates(
	        @Param("startDate") LocalDateTime startDate, 
	        @Param("endDate") LocalDateTime endDate
	);

}

interface IFastMovingDineable {	
	String getDineableName();
	double getQty();
	String getAmount();
}
