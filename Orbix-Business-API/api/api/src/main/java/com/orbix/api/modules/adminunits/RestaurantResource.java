package com.orbix.api.modules.adminunits;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantResource {
	
	private final RestaurantService restaurantService;
	
	@GetMapping("/restaurants")
	public ResponseEntity<List<RestaurantResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantService.getAllRestaurants(request));
	}
	@GetMapping("/restaurants/get")
	public ResponseEntity<RestaurantResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(restaurantService.get(id, request));		
	}
	
	@PostMapping("/restaurants/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantResponseDTO>create(
			@RequestBody RestaurantRequestDTO restaurantRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurants/create").toUriString());
		return ResponseEntity.created(uri).body(restaurantService.createRestaurant(restaurantRequest, request));
	}
	
	@PostMapping("/restaurants/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantResponseDTO>update(
			@RequestBody RestaurantRequestDTO restaurantRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurants/update").toUriString());
		return ResponseEntity.created(uri).body(restaurantService.updateRestaurant(restaurantRequest, request));
	}
	
	@PostMapping("/restaurants/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody RestaurantRequestDTO restaurantRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurants/activate").toUriString());
		return ResponseEntity.created(uri).body(restaurantService.activateRestaurant(restaurantRequest, request));
	}
	
	@PostMapping("/restaurants/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody RestaurantRequestDTO restaurantRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurants/deactivate").toUriString());
		return ResponseEntity.created(uri).body(restaurantService.deactivateRestaurant(restaurantRequest, request));
	}
	
	
	@GetMapping("/restaurants/get_branch_available_restaurants_by_user")
	public ResponseEntity<List<RestaurantResponseDTO>>getBranchAvailableByUser(HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantService.getBranchAvailableRestaurantsByUser(request));
	}
	
	@GetMapping("/restaurants/get_selected_restaurant")
	public ResponseEntity<RestaurantResponseDTO>getSelectedRestaurantByUser(
			@RequestParam(name = "restaurant_id")Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(restaurantService.get(id, request));		
	}
	
	@GetMapping("/restaurants/get_branch_restaurants")
	public ResponseEntity<List<RestaurantResponseDTO>>getBranchRestaurants(HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantService.getBranchRestaurants(request));
	}
}
