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

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.api.commons.PageResponseDTO;
import com.orbix.api.modules.audit.Audited;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantDineableResource {
	
	private final RestaurantDineableRepository restaurantDineableRepository;
	
	private final RestaurantDineableService restaurantDineableService;
	
	@GetMapping("/restaurant_dineables")
	public ResponseEntity<List<RestaurantDineableResponseDTO>>getAllRestaurantDineables(
			@RequestParam(name = "restaurant_id")Long restaurantId, 
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantDineableService.getAllRestaurantDineables(restaurantId, request));
	}
	@GetMapping("/restaurant_dineables/get")
	public ResponseEntity<RestaurantDineableResponseDTO>get(
			@RequestParam(name = "id")Long id,
			@RequestParam(name = "restaurant_id")Long restaurantId,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(restaurantDineableService.get(id, restaurantId, request));		
	}
	
	@GetMapping("/restaurant_dineables/get_dineable_in_restaurant")
	public ResponseEntity<RestaurantDineableResponseDTO>getDineableInRestaurant(
			@RequestParam(name = "dineable_id")Long dineableId,
			@RequestParam(name = "restaurant_id")Long restaurantId,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(restaurantDineableService.getDineableInRestaurant(dineableId, restaurantId, request));		
	}
	
	@PostMapping("/restaurant_dineables/create")
	@Audited(category = "INVENTORY", action = "RECORD_CREATED", entityType = "RestaurantDineable", entityRef = "result.dineableCode", summary = "Created restaurant dineable {ref}")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantDineableResponseDTO>create(
			@RequestBody RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant_dineables/create").toUriString());
		return ResponseEntity.created(uri).body(restaurantDineableService.createRestaurantDineable(restaurantDineableRequest, request));
	}
	
	@PostMapping("/restaurant_dineables/update")
	@Audited(category = "INVENTORY", action = "RECORD_UPDATED", entityType = "RestaurantDineable", entityRef = "result.dineableCode", summary = "Updated restaurant dineable {ref}", changeOf = RestaurantDineable.class, changeId = "restaurantDineableRequest.id", changeQuery = "select d.id from RestaurantDineable d where d.restaurant.id = ?1 and d.dineable.id = ?2", changeKeys = {"restaurantDineableRequest.restaurantId", "restaurantDineableRequest.dineableId"}, changedFieldPattern = "(?i).*price.*", changedAction = "PRICE_CHANGED")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantDineableResponseDTO>update(
			@RequestBody RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant_dineables/update").toUriString());
		return ResponseEntity.created(uri).body(restaurantDineableService.updateRestaurantDineable(restaurantDineableRequest, request));
	}
	
	@PostMapping("/restaurant_dineables/adjust_stock")
	@Audited(category = "INVENTORY", action = "STOCK_ADJUSTED", entityType = "RestaurantDineable", entityRef = "result.dineableCode", summary = "Adjusted the stock of {result.dineableCode} {result.dineableName} to {result.currentStock}", changeOf = RestaurantDineable.class, changeId = "restaurantDineableRequest.id", changeQuery = "select d.id from RestaurantDineable d where d.restaurant.id = ?1 and d.dineable.id = ?2", changeKeys = {"restaurantDineableRequest.restaurantId", "restaurantDineableRequest.dineableId"})
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantDineableResponseDTO>adjustStock(
			@RequestBody RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant_dineables/adjust_stock").toUriString());
		return ResponseEntity.created(uri).body(restaurantDineableService.adjustRestaurantStock(restaurantDineableRequest, request));
	}
	
	@PostMapping("/restaurant_dineables/activate")
	@Audited(category = "INVENTORY", action = "RECORD_ACTIVATED", entityType = "RestaurantDineable", entityId = "restaurantDineableRequest.id", summary = "Activated restaurant dineable {ref}", changeOf = RestaurantDineable.class, changeId = "restaurantDineableRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant_dineables/activate").toUriString());
		return ResponseEntity.created(uri).body(restaurantDineableService.activateRestaurantDineable(restaurantDineableRequest, request));
	}
	
	@PostMapping("/restaurant_dineables/deactivate")
	@Audited(category = "INVENTORY", action = "RECORD_DEACTIVATED", entityType = "RestaurantDineable", entityId = "restaurantDineableRequest.id", summary = "Deactivated restaurant dineable {ref}", changeOf = RestaurantDineable.class, changeId = "restaurantDineableRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/restaurant_dineables/deactivate").toUriString());
		return ResponseEntity.created(uri).body(restaurantDineableService.activateRestaurantDineable(restaurantDineableRequest, request));
	}
	
	@GetMapping("/restaurant_dineables/get_stock_by_restaurant")
	public ResponseEntity<List<RestaurantDineableResponseDTO>>getAllRestaurantDineablesByRestaurant(
			@RequestParam(name = "restaurant_id")Long restaurantId, 
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantDineableService.getAllRestaurantDineables(restaurantId, request));
	}

	@GetMapping("/restaurant_dineables/get_stock_by_restaurant_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<RestaurantDineableResponseDTO>>getStockByRestaurantPage(
			@RequestParam(name = "restaurant_id") Long restaurantId,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantDineableService.getRestaurantDineablePage(restaurantId, page, size, search, request));
	}

	
//	@GetMapping("/restaurant_dineables/get_under_stock_by_restaurant")
//	public ResponseEntity<List<RestaurantDineableResponseDTO>>getUnderstockRestaurantDineablesByRestaurant(
//			@RequestParam(name = "restaurant_id")Long restaurantId, 
//			HttpServletRequest request){
//		return ResponseEntity.ok().body(restaurantDineableService.getUnderstockRestaurantDineables(restaurantId, request));
//	}
	
//	@GetMapping("/restaurant_dineables/get_out_of_stock_by_restaurant")
//	public ResponseEntity<List<RestaurantDineableResponseDTO>>getOutofstockRestaurantDineablesByRestaurant(
//			@RequestParam(name = "restaurant_id")Long restaurantId, 
//			HttpServletRequest request){
//		return ResponseEntity.ok().body(restaurantDineableService.getOutofstockRestaurantDineables(restaurantId, request));
//	}
	
	
	@GetMapping("/restaurant_dineables/get_dineables_by_restaurant_containing")
	public ResponseEntity<List<DineableResponseDTO>>getAllRestaurantDineablesByRestaurantContaining(
			@RequestParam(name = "restaurant_id")Long restaurantId, 
			@RequestParam(name = "dineable_name_like")String dineableNameLike,
			HttpServletRequest request){

		return ResponseEntity.ok().body(restaurantDineableService.getDineablesByRestaurantAndName(restaurantId, dineableNameLike));

	}
	
//	@GetMapping("/restaurant_dineables/get_check_under_stock_by_restaurant")
//	public long checkUnderStockByRestaurant(
//			@RequestParam(name = "restaurant_id")Long restaurantId, 
//			HttpServletRequest request){
//		return restaurantDineableService.checkUnderstockByRestaurant(restaurantId, request);
//	}
	
//	@GetMapping("/restaurant_dineables/get_check_out_of_stock_by_restaurant")
//	public long checkOutofStockByRestaurant(
//			@RequestParam(name = "restaurant_id")Long restaurantId, 
//			HttpServletRequest request){
//		return restaurantDineableService.checkOutofstockByRestaurant(restaurantId, request);
//	}
	
	

}
