package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.api.commons.PageResponseDTO;

public interface RestaurantBadgeService {
	List<RestaurantBadgeResponseDTO> getAllRestaurantBadgesByRestaurantId(Long restaurantId, HttpServletRequest request);
	PageResponseDTO<RestaurantBadgeResponseDTO> getRestaurantBadgePageByRestaurantId(Long restaurantId, int page, int size, String search, HttpServletRequest request);
	String generateBadge(HttpServletRequest request);
	RestaurantBadgeResponseDTO createBadge(RestaurantBadgeRequestDTO badgeRequest, HttpServletRequest request);
	
	ApiCustomResponse activateBadge(RestaurantBadgeRequestDTO agent, HttpServletRequest request);
	ApiCustomResponse deactivateBadge(RestaurantBadgeRequestDTO agent, HttpServletRequest request);
}
