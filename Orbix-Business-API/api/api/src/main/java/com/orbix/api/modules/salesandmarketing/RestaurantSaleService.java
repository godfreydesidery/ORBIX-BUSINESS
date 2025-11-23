package com.orbix.api.modules.salesandmarketing;

import javax.servlet.http.HttpServletRequest;

public interface RestaurantSaleService {
	public RestaurantSale createRestaurantSale(RestaurantSaleRequestDTO restaurantSaleRequest, HttpServletRequest request);
}
