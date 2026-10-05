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
public class DineableResource {
	private final DineableService dineableService;
	
	@GetMapping("/dineables")
	public ResponseEntity<List<DineableResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(dineableService.getAllDineablees(request));
	}
	
	@GetMapping("/dineables/get_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<DineableResponseDTO>>getPage(
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(dineableService.getDineablePage(page, size, search, request));
	}
	@GetMapping("/dineables/get")
	public ResponseEntity<DineableResponseDTO>get(
			@RequestParam(name = "id")Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(dineableService.get(id, request));		
	}
	
	@GetMapping("/dineables/get_company_dineable")
	public ResponseEntity<DineableResponseDTO>getCompanyDineable(
			@RequestParam(name = "dineable_id")Long dineableId,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(dineableService.getCompanyDineable(dineableId, request));		
	}
	
	@PostMapping("/dineables/create")
	@Audited(category = "INVENTORY", action = "RECORD_CREATED", entityType = "Dineable", summary = "Created dineable {ref}")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<DineableResponseDTO>create(
			@RequestBody DineableRequestDTO dineableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/dineables/create").toUriString());
		return ResponseEntity.created(uri).body(dineableService.createDineable(dineableRequest, request));
	}
	
	@PostMapping("/dineables/update")
	@Audited(category = "INVENTORY", action = "RECORD_UPDATED", entityType = "Dineable", summary = "Updated dineable {ref}", changeOf = Dineable.class, changeId = "dineableRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<DineableResponseDTO>update(
			@RequestBody DineableRequestDTO dineableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/dineables/update").toUriString());
		return ResponseEntity.created(uri).body(dineableService.updateDineable(dineableRequest, request));
	}
	
	@PostMapping("/dineables/activate")
	@Audited(category = "INVENTORY", action = "RECORD_ACTIVATED", entityType = "Dineable", entityId = "dineableRequest.id", summary = "Activated dineable {ref}", changeOf = Dineable.class, changeId = "dineableRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody DineableRequestDTO dineableRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/dineables/activate").toUriString());
		return ResponseEntity.created(uri).body(dineableService.activateDineable(dineableRequest, request));
	}
	
	@PostMapping("/dineables/deactivate")
	@Audited(category = "INVENTORY", action = "RECORD_DEACTIVATED", entityType = "Dineable", entityId = "dineableRequest.id", summary = "Deactivated dineable {ref}", changeOf = Dineable.class, changeId = "dineableRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody DineableRequestDTO dineableRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/dineables/deactivate").toUriString());
		return ResponseEntity.created(uri).body(dineableService.deactivateDineable(dineableRequest, request));
	}
	
	@GetMapping("/dineables/get_dineables_by_company")
	public ResponseEntity<List<DineableResponseDTO>>getDineablesByCompany(
			@RequestParam(name = "dineable_name_like") String dineableNameLike,
			HttpServletRequest request){
		return ResponseEntity.ok().body(dineableService.getDineablesByCompany(dineableNameLike, request));
	}
	
	@GetMapping("/dineables/get_company_dineables")
	public ResponseEntity<List<DineableResponseDTO>>getCompanyDineables(
			HttpServletRequest request){
		return ResponseEntity.ok().body(dineableService.getCompanyDineables(request));
	}
	
	@GetMapping("/dineables/get_company_sellable_dineables")
	public ResponseEntity<List<DineableResponseDTO>>getCompanySellableDineables(
			HttpServletRequest request){
		return ResponseEntity.ok().body(dineableService.getCompanySellableDineables(request));
	}
	
	@GetMapping("/dineables/get_company_sellable_dineables_by_restaurant")
	public ResponseEntity<List<DineableResponseDTO>>getCompanySellableDineablesByRestaurant(
			@RequestParam(name = "restaurant_id") Long restaurantId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(dineableService.getCompanySellableDineablesByRestaurant(restaurantId, request));
	}

	@GetMapping("/dineables/get_company_sellable_dineables_by_restaurant_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<DineableResponseDTO>>getCompanySellableDineablesByRestaurantPage(
			@RequestParam(name = "restaurant_id") Long restaurantId,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(dineableService.getCompanySellableDineablePageByRestaurant(restaurantId, page, size, search, request));
	}
	
	@GetMapping("/dineables/get_dineables_by_company_containing")
	public ResponseEntity<List<DineableResponseDTO>>getAllDineablesByCompanyContaining( 
			@RequestParam(name = "dineable_name_like")String dineableNameLike,
			HttpServletRequest request){

		return ResponseEntity.ok().body(dineableService.getDineablesByCompanyAndName(dineableNameLike, request));

	}
}
