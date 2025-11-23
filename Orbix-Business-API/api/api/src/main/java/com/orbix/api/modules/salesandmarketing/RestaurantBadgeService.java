package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface RestaurantBadgeService {
	List<RestaurantBadgeResponseDTO> getAllRestaurantBadgesByRestaurantId(Long restaurantId, HttpServletRequest request);
	String generateBadge(HttpServletRequest request);
	RestaurantBadgeResponseDTO createBadge(RestaurantBadgeRequestDTO badgeRequest, HttpServletRequest request);
	
	ApiCustomResponse activateBadge(RestaurantBadgeRequestDTO agent, HttpServletRequest request);
	ApiCustomResponse deactivateBadge(RestaurantBadgeRequestDTO agent, HttpServletRequest request);
}
