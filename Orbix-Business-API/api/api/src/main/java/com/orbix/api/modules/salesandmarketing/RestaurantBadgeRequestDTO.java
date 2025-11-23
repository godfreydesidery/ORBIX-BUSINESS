package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class RestaurantBadgeRequestDTO {
	Long id;
	String code;
	Long restaurantId;
}
