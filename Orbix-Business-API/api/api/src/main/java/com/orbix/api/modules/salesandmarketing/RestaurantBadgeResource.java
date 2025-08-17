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
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRepository;
import com.orbix.api.modules.identityandaccess.UserRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.inventoryandprocurement.ProductRequestDTO;
import com.orbix.api.modules.inventoryandprocurement.ProductResponseDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantBadgeResource {
	
	private final RestaurantBadgeService restaurantBadgeService;
	
	@GetMapping("/restaurants/get_all_badges")
	public ResponseEntity<List<RestaurantBadgeResponseDTO>> getAllBadges(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok()
				.body(restaurantBadgeService.getAllRestaurantBadgesByRestaurantId(restaurantId, request));
	}
	
	@GetMapping("/restaurants/generate_badge_code")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public IStringData generate(
			HttpServletRequest request){
		String code = restaurantBadgeService.generateBadge(request);
		IStringData data = new IStringData();
		data.setRestaurantBadgeCode(code);
		
		return data;
	}
	
	@PostMapping("/restaurants/create_badge")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantBadgeResponseDTO>create(
			@RequestBody RestaurantBadgeRequestDTO restaurantBadgeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurants/create_badge").toUriString());
		return ResponseEntity.created(uri).body(restaurantBadgeService.createBadge(restaurantBadgeRequest, request));
	}
	
	@PostMapping("/restaurant-badges/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody RestaurantBadgeRequestDTO restaurantBadgeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant-badges/activate").toUriString());
		return ResponseEntity.created(uri).body(restaurantBadgeService.activateBadge(restaurantBadgeRequest, request));
	}
	
	@PostMapping("/restaurant-badges/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody RestaurantBadgeRequestDTO restaurantBadgeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant-badges/deactivate").toUriString());
		return ResponseEntity.created(uri).body(restaurantBadgeService.deactivateBadge(restaurantBadgeRequest, request));
	}

}

@Data
class IStringData{
	String restaurantBadgeCode;
}
