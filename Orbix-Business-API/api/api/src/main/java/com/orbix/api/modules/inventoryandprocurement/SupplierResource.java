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
import com.orbix.api.modules.adminunits.ShopResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class SupplierResource {
private final SupplierService supplierService;
	
	@GetMapping("/suppliers")
	public ResponseEntity<List<SupplierResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(supplierService.getAllSuppliers(request));
	}
	
	@GetMapping("/suppliers/get_all_by_company")
	public ResponseEntity<List<SupplierResponseDTO>>getAllByCompany(HttpServletRequest request){
		return ResponseEntity.ok().body(supplierService.getAllCompanySuppliers(request));
	}
	
	@GetMapping("/suppliers/get")
	public ResponseEntity<SupplierResponseDTO>get(
			@RequestParam(name = "id")Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(supplierService.get(id, request));		
	}
	
	@PostMapping("/suppliers/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<SupplierResponseDTO>create(
			@RequestBody SupplierRequestDTO supplierRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/suppliers/create").toUriString());
		return ResponseEntity.created(uri).body(supplierService.createSupplier(supplierRequest, request));
	}
	
	@PostMapping("/suppliers/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<SupplierResponseDTO>update(
			@RequestBody SupplierRequestDTO supplierRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/suppliers/update").toUriString());
		return ResponseEntity.created(uri).body(supplierService.updateSupplier(supplierRequest, request));
	}
	
	@PostMapping("/suppliers/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody SupplierRequestDTO supplierRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/suppliers/activate").toUriString());
		return ResponseEntity.created(uri).body(supplierService.activateSupplier(supplierRequest, request));
	}
	
	@PostMapping("/suppliers/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody SupplierRequestDTO supplierRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/suppliers/deactivate").toUriString());
		return ResponseEntity.created(uri).body(supplierService.deactivateSupplier(supplierRequest, request));
	}
	
	@GetMapping("/suppliers/get_suppliers_by_company")
	public ResponseEntity<List<SupplierResponseDTO>>getSuppliersByCompany(
			@RequestParam(name = "supplier_name_like") String supplierNameLike,
			HttpServletRequest request){
		return ResponseEntity.ok().body(supplierService.getSuppliersByCompany(supplierNameLike, request));
	}	
}
