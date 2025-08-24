package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantReportResource {
	
	private final RestaurantSalesOrderDetailRepository restaurantSalesOrderDetailRepository;
	
	@GetMapping("/get_restaurant_sales_listing_report")
	public ResponseEntity<List<IRestaurantSaleListingReport>> getSalesListingReport(
	        @RequestParam(name = "restaurant_id", required = false) Long restaurantId, // <-- required = false
	        @RequestParam(name = "start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
	        @RequestParam(name = "end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
	        @RequestParam(name = "agent_name", required = false) String agentName,
	        @RequestParam(name = "nickname", required = false) String confirmedBy
	) {
	    List<IRestaurantSaleListingReport> report =
	            restaurantSalesOrderDetailRepository.getRestaurantSalesListingReport(
	                    restaurantId, startDate, endDate, agentName, confirmedBy
	            );
	    return ResponseEntity.ok(report);
	}

}

