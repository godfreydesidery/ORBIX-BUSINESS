package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface RestaurantDineableProductService {
	List<RestaurantDineableProductResponseDTO> getAllRestaurantDineableProducts(Long restaurantId, Long dineableId, HttpServletRequest request);	
	RestaurantDineableProductResponseDTO createRestaurantDineableProduct(RestaurantDineableProductRequestDTO restaurantDineableProductRequest, HttpServletRequest request);
	RestaurantDineableProductResponseDTO updateRestaurantDineableProduct(RestaurantDineableProductRequestDTO restaurantDineableProductRequest, HttpServletRequest request);
	boolean remove(Long id, HttpServletRequest request);
	RestaurantDineableProductResponseDTO get(Long id, HttpServletRequest request);
}
