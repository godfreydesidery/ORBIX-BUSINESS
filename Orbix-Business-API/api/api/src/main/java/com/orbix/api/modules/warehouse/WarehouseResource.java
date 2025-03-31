package com.orbix.api.modules.warehouse;

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
import com.orbix.api.modules.adminunits.ShopResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class WarehouseResource {

	private final WarehouseService warehouseService;
	
	@GetMapping("/warehouses")
	public ResponseEntity<List<WarehouseResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(warehouseService.getAllWarehouses(request));
	}
	
	@GetMapping("/warehouses/get_all_branch_active")
	public ResponseEntity<List<WarehouseResponseDTO>>getAllBranchActive(HttpServletRequest request){		
		return ResponseEntity.ok().body(warehouseService.getAllBranchActiveWarehouses(request));
	}
	
	
	@GetMapping("/warehouses/get")
	public ResponseEntity<WarehouseResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(warehouseService.get(id, request));		
	}
	
	@PostMapping("/warehouses/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<WarehouseResponseDTO>create(
			@RequestBody WarehouseRequestDTO warehouseRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/warehouses/create").toUriString());
		return ResponseEntity.created(uri).body(warehouseService.createWarehouse(warehouseRequest, request));
	}
	
	@PostMapping("/warehouses/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<WarehouseResponseDTO>update(
			@RequestBody WarehouseRequestDTO warehouseRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/warehouses/update").toUriString());
		return ResponseEntity.created(uri).body(warehouseService.updateWarehouse(warehouseRequest, request));
	}
	
	@PostMapping("/warehouses/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody WarehouseRequestDTO warehouseRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/warehouses/activate").toUriString());
		return ResponseEntity.created(uri).body(warehouseService.activateWarehouse(warehouseRequest, request));
	}
	
	
	@PostMapping("/warehouses/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody WarehouseRequestDTO warehouseRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/warehouses/deactivate").toUriString());
		return ResponseEntity.created(uri).body(warehouseService.deactivateWarehouse(warehouseRequest, request));
	}
	
	@GetMapping("/warehouses/get_branch_available_warehouses_by_user")
	public ResponseEntity<List<WarehouseResponseDTO>>getBranchAvailableByUser(HttpServletRequest request){
		return ResponseEntity.ok().body(warehouseService.getBranchAvailableWarehousesByUser(request));
	}
	
	@GetMapping("/warehouses/get_selected_warehouse")
	public ResponseEntity<WarehouseResponseDTO>getSelectedWarehouseByUser(
			@RequestParam(name = "warehouse_id")Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(warehouseService.get(id, request));		
	}
	
}
