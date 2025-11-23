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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class ServiceResource {
	
	private final ServiceService serviceService;
	
	@GetMapping("/services")
	public ResponseEntity<List<ServiceResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(serviceService.getAllServicels(request));
	}
	@GetMapping("/services/get")
	public ResponseEntity<ServiceResponseDTO>get(
			@RequestParam(name = "id")Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(serviceService.get(id, request));		
	}
	
	@GetMapping("/services/get_company_service")
	public ResponseEntity<ServiceResponseDTO>getCompanyService(
			@RequestParam(name = "service_id")Long serviceId,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(serviceService.getCompanyService(serviceId, request));		
	}
	
	@PostMapping("/services/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ServiceResponseDTO>create(
			@RequestBody ServiceRequestDTO serviceRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/services/create").toUriString());
		return ResponseEntity.created(uri).body(serviceService.createService(serviceRequest, request));
	}
	
	@PostMapping("/services/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ServiceResponseDTO>update(
			@RequestBody ServiceRequestDTO serviceRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/services/update").toUriString());
		return ResponseEntity.created(uri).body(serviceService.updateService(serviceRequest, request));
	}
	
	@PostMapping("/services/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody ServiceRequestDTO serviceRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/services/activate").toUriString());
		return ResponseEntity.created(uri).body(serviceService.activateService(serviceRequest, request));
	}
	
	@PostMapping("/services/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody ServiceRequestDTO serviceRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/services/deactivate").toUriString());
		return ResponseEntity.created(uri).body(serviceService.deactivateService(serviceRequest, request));
	}
	
	@GetMapping("/services/get_services_by_company")
	public ResponseEntity<List<ServiceResponseDTO>>getServicesByCompany(
			@RequestParam(name = "service_name_like") String serviceNameLike,
			HttpServletRequest request){
		return ResponseEntity.ok().body(serviceService.getServicesByCompany(serviceNameLike, request));
	}
	
	@GetMapping("/services/get_company_services")
	public ResponseEntity<List<ServiceResponseDTO>>getCompanyServices(
			HttpServletRequest request){
		return ResponseEntity.ok().body(serviceService.getCompanyServices(request));
	}
	
//	@GetMapping("/services/get_company_sellable_services")
//	public ResponseEntity<List<ServiceResponseDTO>>getCompanySellableServices(
//			HttpServletRequest request){
//		return ResponseEntity.ok().body(serviceService.getCompanySellableServices(request));
//	}
//	
//	@GetMapping("/services/get_company_sellable_services_by_shop")
//	public ResponseEntity<List<ServiceResponseDTO>>getCompanySellableServicesByShop(
//			@RequestParam(name = "shop_id") Long shopId,
//			HttpServletRequest request){
//		return ResponseEntity.ok().body(serviceService.getCompanySellableServicesByShop(shopId, request));
//	}
//	
//	@GetMapping("/services/get_company_sellable_services_by_restaurant")
//	public ResponseEntity<List<ServiceResponseDTO>>getCompanySellableServicesByRestaurant(
//			@RequestParam(name = "restaurant_id") Long restaurantId,
//			HttpServletRequest request){
//		return ResponseEntity.ok().body(serviceService.getCompanySellableServicesByRestaurant(restaurantId, request));
//	}
	
	@GetMapping("/services/get_services_by_company_containing")
	public ResponseEntity<List<ServiceResponseDTO>> getAllServicesByCompanyContaining(
			@RequestParam(name = "service_name_like") String serviceNameLike, HttpServletRequest request) {

		return ResponseEntity.ok().body(serviceService.getServicesByCompanyAndName(serviceNameLike, request));

	}
	
//	@GetMapping("/services/get_services_containing")
//	public ResponseEntity<List<ServiceResponseDTO>>getAllRestaurantDineablesByRestaurantContaining(
//			@RequestParam(name = "dineable_name_like")String dineableNameLike,
//			HttpServletRequest request){
//
//		return ResponseEntity.ok().body(serviceService.getServicesByCompanyAndName(dineableNameLike));
//
//	}
}
