package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class RestaurantAgentRequestDTO {
	Long id;
	String name;
	String phoneNo;
	Long restaurantId;
}
