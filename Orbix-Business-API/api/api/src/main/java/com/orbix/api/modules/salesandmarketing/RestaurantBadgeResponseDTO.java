package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class RestaurantBadgeResponseDTO {
	String id;
	String code;
	String restaurantId;
	String activeStatus;
}
