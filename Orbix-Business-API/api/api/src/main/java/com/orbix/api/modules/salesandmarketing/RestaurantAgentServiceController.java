package com.orbix.api.modules.salesandmarketing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.RestaurantRepository;
import com.orbix.api.modules.inventoryandprocurement.Product;
import com.orbix.api.modules.inventoryandprocurement.ProductRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RestaurantAgentServiceController implements RestaurantAgentService {
	
	private final RestaurantRepository restaurantRepository;
	private final RestaurantAgentRepository restaurantAgentRepository;
	
	private final RestaurantBadgeRepository restaurantBadgeRepository;

	@Override
	public List<RestaurantAgentResponseDTO> getAllRestaurantAgentsByRestaurantId(Long restaurantId, HttpServletRequest request) {
		
		Restaurant restaurant = restaurantRepository.findById(restaurantId)
			    .orElseThrow(() -> new NotFoundException("Restaurant not found, with id " + restaurantId));

		return restaurantAgentRepository.findAllByRestaurant(restaurant).stream()
		        .map(this::agentToDto)
		        .toList();
	}

	@Override
	public RestaurantAgentResponseDTO createAgent(RestaurantAgentRequestDTO agentRequest, HttpServletRequest request) {
		Restaurant restaurant = restaurantRepository.findById(agentRequest.getRestaurantId())
			    .orElseThrow(() -> new NotFoundException("Restaurant not found"));
		
		RestaurantAgent agent = new RestaurantAgent();
		agent.setName(agentRequest.getName());
		agent.setPhoneNo(agentRequest.getPhoneNo());
		agent.setRestaurant(restaurant);
		agent = restaurantAgentRepository.save(agent);
		
		return agentToDto(agent);
	}
	
	@Override
	public List<RestaurantBadgeResponseDTO> getAvailableBadgeByRestaurantId(Long restaurantId,
			HttpServletRequest request) {
		Restaurant restaurant = restaurantRepository.findById(restaurantId)
			    .orElseThrow(() -> new NotFoundException("Restaurant not found"));
		
		// All badges for the restaurant
		List<RestaurantBadge> restaurantBadges = restaurantBadgeRepository.findAllByRestaurant(restaurant);

		// All agents with a badge
		List<RestaurantAgent> restaurantAgents = restaurantAgentRepository
		        .findAllByRestaurantAndRestaurantBadgeIsNotNull(restaurant);

		// Extract badges assigned to agents
		Set<RestaurantBadge> assignedBadges = restaurantAgents.stream()
		        .map(RestaurantAgent::getRestaurantBadge)
		        .collect(Collectors.toSet());

		// Get available badges = all - assigned
		List<RestaurantBadge> availableBadges = restaurantBadges.stream()
		        .filter(badge -> !assignedBadges.contains(badge))
		        .collect(Collectors.toList());
		
		// Convert to DTOs
		List<RestaurantBadgeResponseDTO> responses = new ArrayList<>();
		for(RestaurantBadge badge : availableBadges) {
			RestaurantBadgeResponseDTO res = badgeToDto(badge);
			responses.add(res);
		}

		return responses;

		
		
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateAgent(RestaurantAgentRequestDTO agent, HttpServletRequest request) {
		Optional<RestaurantAgent> agent_ = restaurantAgentRepository.findById(agent.getId());		
		if(agent_.isEmpty()) {
			throw new NotFoundException("Agent not found");
		}		
		if(agent_.get().isActive() == true) {
			throw new InvalidOperationException("Agent already active");
		}
		agent_.get().setActive(true);
		restaurantAgentRepository.save(agent_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Agent Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateAgent(RestaurantAgentRequestDTO agent, HttpServletRequest request) {
		Optional<RestaurantAgent> agent_ = restaurantAgentRepository.findById(agent.getId());		
		if(agent_.isEmpty()) {
			throw new NotFoundException("Agent not found");
		}		
		if(agent_.get().isActive() == false) {
			throw new InvalidOperationException("Agent already inactive");
		}
		agent_.get().setActive(false);
		restaurantAgentRepository.save(agent_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Agent Deactivated successifully");
	}
	
	@Override
	public boolean assignBadge(AgentBadgeData agentBadgeData, HttpServletRequest request) {
		RestaurantAgent restaurantAgent = restaurantAgentRepository.findById(agentBadgeData.getRestaurantAgentId())
			    .orElseThrow(() -> new NotFoundException("Agent not found"));
		if(restaurantAgent.getRestaurantBadge() != null) {
			throw new InvalidOperationException("Agent already assigned badge. Please remove badge from agent");
		}		
		RestaurantBadge restaurantBadge = restaurantBadgeRepository.findById(agentBadgeData.getRestaurantBadgeId())
			    .orElseThrow(() -> new NotFoundException("Badge not found"));
		boolean exists = restaurantAgentRepository.existsByRestaurantBadgeId(restaurantBadge.getId());		
		if (exists) throw new InvalidOperationException("Badge already assigned to an agent. Please remove it first");		
		restaurantAgent.setRestaurantBadge(restaurantBadge);
		restaurantAgentRepository.saveAndFlush(restaurantAgent);
		return true;
	}
	
	@Override
	public ApiCustomResponse unassignBadge(RestaurantAgentRequestDTO agent, HttpServletRequest request) {
		Optional<RestaurantAgent> agent_ = restaurantAgentRepository.findById(agent.getId());
		if(agent_.isEmpty()) throw new NotFoundException("Agent not found");	
		if(agent_.get().getRestaurantBadge() == null) throw new InvalidOperationException("Agent has no badge");				
		agent_.get().setRestaurantBadge(null);
		restaurantAgentRepository.save(agent_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Badge removed successifully");
	}
	
	private RestaurantAgentResponseDTO agentToDto(RestaurantAgent agent) {
		RestaurantAgentResponseDTO res = new RestaurantAgentResponseDTO();
		res.setId(agent.getId().toString());
		res.setName(agent.getName());
		res.setPhoneNo(agent.getPhoneNo());
		res.setRestaurantId(agent.getRestaurant().getId().toString());
		res.setActiveStatus(agent != null && agent.isActive() ? "Active" : "Inactive");
		res.setRestaurantBadgeCode(agent.getRestaurantBadge() != null ? agent.getRestaurantBadge().getCode() : "");
		return res;
	}
	
	private RestaurantBadgeResponseDTO badgeToDto(RestaurantBadge badge) {
		RestaurantBadgeResponseDTO res = new RestaurantBadgeResponseDTO();
		res.setId(badge.getId().toString());
		res.setCode(badge.getCode());
		res.setRestaurantId(badge.getRestaurant().getId().toString());
		res.setActiveStatus(badge != null && badge.isActive() ? "Active" : "Inactive");
		return res;
	}

	

	

	

}
