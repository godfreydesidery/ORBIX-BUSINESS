package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import lombok.Data;

@Data
public class RestaurantSalesOrderResponseDTO {
	String id;
	String no;	
	String customerName;
	String summary;
	String status;	
    String restaurantId;
    String createdBy;
    String  createdAt;
    
    String restaurantBadgeCode;
    String restaurantAgentName;
    
    List<RestaurantSalesOrderDetailResponseDTO> restaurantSalesOrderDetails;
}
