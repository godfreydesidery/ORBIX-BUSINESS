package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface RestaurantProductService {
	List<RestaurantProductResponseDTO> getAllRestaurantProducts(Long restaurantId, HttpServletRequest request);
	List<RestaurantProductResponseDTO> getUnderstockRestaurantProducts(Long restaurantId, HttpServletRequest request);
	List<RestaurantProductResponseDTO> getOutofstockRestaurantProducts(Long restaurantId, HttpServletRequest request);
	RestaurantProductResponseDTO get(Long id, Long restaurantId, HttpServletRequest request);
	RestaurantProductResponseDTO getProductInRestaurant(Long productId, Long restaurantId, HttpServletRequest request);
	RestaurantProductResponseDTO createRestaurantProduct(RestaurantProductRequestDTO restaurantProductRequest, HttpServletRequest request);
	RestaurantProductResponseDTO updateRestaurantProduct(RestaurantProductRequestDTO restaurantProductRequest, HttpServletRequest request);
	RestaurantProductResponseDTO adjustRestaurantStock(RestaurantProductRequestDTO restaurantProductRequest, HttpServletRequest request);
	RestaurantProductResponseDTO addRestaurantStock(RestaurantProductRequestDTO restaurantProductRequest, HttpServletRequest request);
	RestaurantProductResponseDTO deductRestaurantStock(RestaurantProductRequestDTO restaurantProductRequest, HttpServletRequest request);
	ApiCustomResponse activateRestaurantProduct(RestaurantProductRequestDTO restaurantProductRequest, HttpServletRequest request);
	ApiCustomResponse deactivateRestaurantProduct(RestaurantProductRequestDTO restaurantProductRequest, HttpServletRequest request);
	
	List<ProductResponseDTO> getProductsByRestaurantAndName(Long restaurantId, String productName);
	
	long checkUnderstockByRestaurant(Long restaurantId, HttpServletRequest request);
	long checkOutofstockByRestaurant(Long restaurantId, HttpServletRequest request);
}
