package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface RestaurantDineableService {
	List<RestaurantDineableResponseDTO> getAllRestaurantDineables(Long restaurantId, HttpServletRequest request);
//	List<RestaurantDineableResponseDTO> getUnderstockRestaurantDineables(Long restaurantId, HttpServletRequest request);
//	List<RestaurantDineableResponseDTO> getOutofstockRestaurantDineables(Long restaurantId, HttpServletRequest request);
	RestaurantDineableResponseDTO get(Long id, Long restaurantId, HttpServletRequest request);
	RestaurantDineableResponseDTO getDineableInRestaurant(Long dineableId, Long restaurantId, HttpServletRequest request);
	RestaurantDineableResponseDTO createRestaurantDineable(RestaurantDineableRequestDTO restaurantDineableRequest, HttpServletRequest request);
	RestaurantDineableResponseDTO updateRestaurantDineable(RestaurantDineableRequestDTO restaurantDineableRequest, HttpServletRequest request);
	RestaurantDineableResponseDTO adjustRestaurantStock(RestaurantDineableRequestDTO restaurantDineableRequest, HttpServletRequest request);
	ApiCustomResponse activateRestaurantDineable(RestaurantDineableRequestDTO restaurantDineableRequest, HttpServletRequest request);
	ApiCustomResponse deactivateRestaurantDineable(RestaurantDineableRequestDTO restaurantDineableRequest, HttpServletRequest request);
	
	List<DineableResponseDTO> getDineablesByRestaurantAndName(Long restaurantId, String dineableName);
	
//	long checkUnderstockByRestaurant(Long restaurantId, HttpServletRequest request);
//	long checkOutofstockByRestaurant(Long restaurantId, HttpServletRequest request);
}
