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
import com.orbix.api.api.commons.PageResponseDTO;
import com.orbix.api.modules.audit.Audited;

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

	@GetMapping("/restaurants/get_all_agents_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<RestaurantAgentResponseDTO>>getAllAgentsPage(
			@RequestParam(name = "restaurant_id") Long restaurantId,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantAgentService.getRestaurantAgentPageByRestaurantId(restaurantId, page, size, search, request));
	}
	
	@GetMapping("/restaurants/get_available_agents")
	public ResponseEntity<List<RestaurantAgentResponseDTO>> getAvailableAgents(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok()
				.body(restaurantAgentService.getAvailableRestaurantAgentsByRestaurantId(restaurantId, request));
	}
	
	
	@PostMapping("/restaurants/create_agent")
	@Audited(category = "SETTINGS", action = "RECORD_CREATED", entityType = "RestaurantAgent", entityRef = "result.name", summary = "Created restaurant agent {result.name}")
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
	@Audited(category = "SETTINGS", action = "BADGE_ASSIGNED", entityType = "RestaurantAgent", entityId = "agentBadgeData.restaurantAgentId", summary = "Gave badge {agentBadgeData.restaurantBadgeId} to restaurant agent {ref}", changeOf = RestaurantAgent.class, changeId = "agentBadgeData.restaurantAgentId")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<Boolean>assignBadge(
			@RequestBody AgentBadgeData agentBadgeData,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurants/create_agent").toUriString());
		return ResponseEntity.created(uri).body(restaurantAgentService.assignBadge(agentBadgeData, request));
	}
	
	@PostMapping("/restaurant-agents/activate")
	@Audited(category = "SETTINGS", action = "RECORD_ACTIVATED", entityType = "RestaurantAgent", entityId = "restaurantAgentRequest.id", summary = "Activated restaurant agent {ref}", changeOf = RestaurantAgent.class, changeId = "restaurantAgentRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody RestaurantAgentRequestDTO restaurantAgentRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant-agents/activate").toUriString());
		return ResponseEntity.created(uri).body(restaurantAgentService.activateAgent(restaurantAgentRequest, request));
	}
	
	@PostMapping("/restaurant-agents/deactivate")
	@Audited(category = "SETTINGS", action = "RECORD_DEACTIVATED", entityType = "RestaurantAgent", entityId = "restaurantAgentRequest.id", summary = "Deactivated restaurant agent {ref}", changeOf = RestaurantAgent.class, changeId = "restaurantAgentRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody RestaurantAgentRequestDTO restaurantAgentRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant-agents/deactivate").toUriString());
		return ResponseEntity.created(uri).body(restaurantAgentService.deactivateAgent(restaurantAgentRequest, request));
	}
	
	@PostMapping("/restaurant-agents/unassign-badge")
	@Audited(category = "SETTINGS", action = "BADGE_UNASSIGNED", entityType = "RestaurantAgent", entityId = "restaurantAgentRequest.id", summary = "Took the badge from restaurant agent {ref}", changeOf = RestaurantAgent.class, changeId = "restaurantAgentRequest.id")
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
