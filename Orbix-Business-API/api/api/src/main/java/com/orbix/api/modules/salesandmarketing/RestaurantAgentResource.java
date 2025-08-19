package com.orbix.api.modules.salesandmarketing;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.modules.inventoryandprocurement.ProductRequestDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantAgentResource {
	
	private final RestaurantAgentService restaurantAgentService;
	
	@GetMapping("/restaurants/get_all_agents")
	public ResponseEntity<List<RestaurantAgentResponseDTO>> getAllAgents(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok()
				.body(restaurantAgentService.getAllRestaurantAgentsByRestaurantId(restaurantId, request));
	}
	
	@GetMapping("/restaurants/get_available_agents")
	public ResponseEntity<List<RestaurantAgentResponseDTO>> getAvailableAgents(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok()
				.body(restaurantAgentService.getAvailableRestaurantAgentsByRestaurantId(restaurantId, request));
	}
	
	
	@PostMapping("/restaurants/create_agent")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantAgentResponseDTO>create(
			@RequestBody RestaurantAgentRequestDTO restaurantAgentRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurants/create_agent").toUriString());
		return ResponseEntity.created(uri).body(restaurantAgentService.createAgent(restaurantAgentRequest, request));
	}
	
	@GetMapping("/restaurants/get_available_badges_by_restaurant_id")
	public ResponseEntity<List<RestaurantBadgeResponseDTO>> getAvailableBadges(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok()
				.body(restaurantAgentService.getAvailableBadgeByRestaurantId(restaurantId, request));
	}
	
	@PostMapping("/restaurant-agents/assign-badge-to-agent")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<Boolean>assignBadge(
			@RequestBody AgentBadgeData agentBadgeData,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurants/create_agent").toUriString());
		return ResponseEntity.created(uri).body(restaurantAgentService.assignBadge(agentBadgeData, request));
	}
	
	@PostMapping("/restaurant-agents/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody RestaurantAgentRequestDTO restaurantAgentRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant-agents/activate").toUriString());
		return ResponseEntity.created(uri).body(restaurantAgentService.activateAgent(restaurantAgentRequest, request));
	}
	
	@PostMapping("/restaurant-agents/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody RestaurantAgentRequestDTO restaurantAgentRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant-agents/deactivate").toUriString());
		return ResponseEntity.created(uri).body(restaurantAgentService.deactivateAgent(restaurantAgentRequest, request));
	}
	
	@PostMapping("/restaurant-agents/unassign-badge")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>unassignBadge(
			@RequestBody RestaurantAgentRequestDTO restaurantAgentRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant-agents/unassign-badge").toUriString());
		return ResponseEntity.created(uri).body(restaurantAgentService.unassignBadge(restaurantAgentRequest, request));
	}
}

@Data
class AgentBadgeData{
	Long restaurantAgentId;
	Long restaurantBadgeId;
}
