package com.orbix.api.modules.inventoryandprocurement;

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

import lombok.RequiredArgsConstructor;
import com.orbix.api.modules.audit.Audited;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantDineableProductResource {
	
	private final RestaurantDineableProductService restaurantDineableProductService;
	
	@GetMapping("/restaurant_dineable_products")
	public ResponseEntity<List<RestaurantDineableProductResponseDTO>>getAllRestaurantDineableProducts(
			@RequestParam(name = "restaurant_id")Long restaurantId, 
			@RequestParam(name = "dineable_id")Long dineableId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantDineableProductService.getAllRestaurantDineableProducts(restaurantId, dineableId, request));
	}
	
	@GetMapping("/restaurant_dineable_products/get")
	public ResponseEntity<RestaurantDineableProductResponseDTO>get(
			@RequestParam(name = "id")Long id, 
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantDineableProductService.get(id, request));
	}

	
	@PostMapping("/restaurant_dineable_products/create")
	@Audited(category = "INVENTORY", action = "RECORD_CREATED", entityType = "RestaurantDineableProduct", summary = "Created restaurant dineable product {ref}")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantDineableProductResponseDTO>create(
			@RequestBody RestaurantDineableProductRequestDTO restaurantDineableProductRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant_dineables/create").toUriString());
		return ResponseEntity.created(uri).body(restaurantDineableProductService.createRestaurantDineableProduct(restaurantDineableProductRequest, request));
	}
	
	@PostMapping("/restaurant_dineable_products/update")
	@Audited(category = "INVENTORY", action = "RECORD_UPDATED", entityType = "RestaurantDineableProduct", summary = "Updated restaurant dineable product {ref}", changeOf = RestaurantDineableProduct.class, changeId = "restaurantDineableProductRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantDineableProductResponseDTO>cupdate(
			@RequestBody RestaurantDineableProductRequestDTO restaurantDineableProductRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant_dineables/create").toUriString());
		return ResponseEntity.created(uri).body(restaurantDineableProductService.updateRestaurantDineableProduct(restaurantDineableProductRequest, request));
	}
	
	@GetMapping("/restaurant_dineable_products/remove")
	public ResponseEntity<Boolean>remove(
			@RequestParam(name = "id")Long id,
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantDineableProductService.remove(id, request));
	}
}
