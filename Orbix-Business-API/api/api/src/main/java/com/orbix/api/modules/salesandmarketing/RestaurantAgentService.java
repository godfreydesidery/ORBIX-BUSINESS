package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.modules.inventoryandprocurement.ProductRequestDTO;

public interface RestaurantAgentService {
	List<RestaurantAgentResponseDTO> getAllRestaurantAgentsByRestaurantId(Long restaurantId, HttpServletRequest request);
	RestaurantAgentResponseDTO createAgent(RestaurantAgentRequestDTO agentRequest, HttpServletRequest request);
	List<RestaurantBadgeResponseDTO> getAvailableBadgeByRestaurantId(Long restaurantId, HttpServletRequest request);
	
	boolean assignBadge(AgentBadgeData agentBadgeData, HttpServletRequest request);
	
	ApiCustomResponse activateAgent(RestaurantAgentRequestDTO agent, HttpServletRequest request);
	ApiCustomResponse deactivateAgent(RestaurantAgentRequestDTO agent, HttpServletRequest request);
	
	ApiCustomResponse unassignBadge(RestaurantAgentRequestDTO agent, HttpServletRequest request);
}
