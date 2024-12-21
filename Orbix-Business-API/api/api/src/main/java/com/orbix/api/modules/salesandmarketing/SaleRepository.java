package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleRepository extends JpaRepository<Sale, Long> {
	
	@Query(value = 
	        "SELECT " +
	        "    ROW_NUMBER() OVER (ORDER BY s.created_date_time) AS sn, " +
	        "    p.name AS productName, " +
	        "    sd.qty AS qty, " +
	        "    CAST(sd.qty * sd.selling_price_vat_incl AS CHAR) AS amount, " +
	        "    u.nickname AS createdBy, " +
	        "    s.created_date_time AS timeDate " +
	        "FROM sale_details sd " +
	        "JOIN sales s ON sd.sale_id = s.id " +
	        "JOIN products p ON sd.product_id = p.id " +
	        "JOIN users u ON s.created_by_user_id = u.id " +
	        "WHERE s.created_date_time BETWEEN :startDate AND :endDate",
	        nativeQuery = true
	    )
	List<ISalesListing> getSalesListingReportByDates(
		    @Param("startDate") LocalDateTime startDate, 
		    @Param("endDate") LocalDateTime endDate);
	
	@Query(value = 
	        "SELECT " +
	        "    ROW_NUMBER() OVER (ORDER BY SUM(sd.qty) DESC) AS sn, " +
	        "    p.name AS productName, " +
	        "    SUM(sd.qty) AS qty, " +
	        "    CAST(SUM(sd.qty * sd.selling_price_vat_incl) AS CHAR) AS amount " +
	        "FROM sale_details sd " +
	        "JOIN products p ON sd.product_id = p.id " +
	        "WHERE sd.sale_id IN (SELECT s.id FROM sales s WHERE s.created_date_time BETWEEN :startDate AND :endDate) " +
	        "GROUP BY p.name " +
	        "ORDER BY SUM(sd.qty) DESC",
	        nativeQuery = true
	    )
	List<IFastMovingProducts> getFastMovingProductsByDates(
	        @Param("startDate") LocalDateTime startDate, 
	        @Param("endDate") LocalDateTime endDate
	);
}


interface ISalesListing {	
	String getProductName();
	double getQty();
	String getAmount();
	String getCreatedBy();
	String getTimeDate();
}

interface IFastMovingProducts {	
	String getProductName();
	double getQty();
	String getAmount();
}

