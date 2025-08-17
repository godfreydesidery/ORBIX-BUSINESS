package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class RestaurantAgentResponseDTO {
	String id;
	String name;
	String phoneNo;
	String restaurantId;
	String activeStatus;
	String restaurantBadgeCode;
}
