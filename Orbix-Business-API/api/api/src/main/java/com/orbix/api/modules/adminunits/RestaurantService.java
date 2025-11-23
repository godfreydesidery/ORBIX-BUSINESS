package com.orbix.api.modules.adminunits;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface RestaurantService {
	List<RestaurantResponseDTO> getAllRestaurants(HttpServletRequest request);
	RestaurantResponseDTO get(Long id, HttpServletRequest request);
	RestaurantResponseDTO createRestaurant(RestaurantRequestDTO restaurant, HttpServletRequest request);
	RestaurantResponseDTO updateRestaurant(RestaurantRequestDTO restaurant, HttpServletRequest request);
	ApiCustomResponse activateRestaurant(RestaurantRequestDTO restaurant, HttpServletRequest request);
	ApiCustomResponse deactivateRestaurant(RestaurantRequestDTO restaurant, HttpServletRequest request);
	
	
	List<RestaurantResponseDTO> getBranchAvailableRestaurantsByUser(HttpServletRequest request);
	RestaurantResponseDTO getSelectedRestaurant(Long id, HttpServletRequest request);
	
	List<RestaurantResponseDTO> getBranchRestaurants(HttpServletRequest request);
}
