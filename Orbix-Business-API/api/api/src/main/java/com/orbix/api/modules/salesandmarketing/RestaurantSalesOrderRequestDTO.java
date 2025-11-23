package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class RestaurantSalesOrderRequestDTO {
	Long id;
	String no;	
	String summary;
	String status;	
    Long restaurantId;
    Long restaurantAgentId;
    
    String customerName;
}
