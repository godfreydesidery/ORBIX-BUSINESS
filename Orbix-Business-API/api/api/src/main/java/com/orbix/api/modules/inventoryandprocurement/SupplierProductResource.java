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
public class SupplierProductResource {
private final SupplierProductRepository supplierProductRepository;
	
	private final SupplierProductService supplierProductService;
	
	@GetMapping("/supplier_products")
	public ResponseEntity<List<SupplierProductResponseDTO>>getAllSupplierProducts(
			@RequestParam(name = "supplier_id")Long supplierId, 
			HttpServletRequest request){
		return ResponseEntity.ok().body(supplierProductService.getAllSupplierProductsByBranch(supplierId, request));
	}
	@GetMapping("/supplier_products/get")
	public ResponseEntity<SupplierProductResponseDTO>get(
			@RequestParam(name = "id")Long id,
			@RequestParam(name = "supplier_id")Long supplierId,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(supplierProductService.get(id, supplierId, request));		
	}
	
	@GetMapping("/supplier_products/get_product")
	public ResponseEntity<SupplierProductResponseDTO>getSupplierProduct(
			@RequestParam(name = "supplier_id")Long supplierId,
			@RequestParam(name = "product_id")Long productId,			
			HttpServletRequest request){		
		return ResponseEntity.ok().body(supplierProductService.getSupplierProduct(supplierId, productId, request));		
	}
	
	@GetMapping("/supplier_products/get_product_in_supplier")
	public ResponseEntity<SupplierProductResponseDTO>getProductInSupplier(
			@RequestParam(name = "product_id")Long productId,
			@RequestParam(name = "supplier_id")Long supplierId,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(supplierProductService.getProductInSupplier(productId, supplierId, request));		
	}
	
	@PostMapping("/supplier_products/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<SupplierProductResponseDTO>create(
			@RequestBody SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/supplier_products/create").toUriString());
		return ResponseEntity.created(uri).body(supplierProductService.createSupplierProduct(supplierProductRequest, request));
	}
	
	@PostMapping("/supplier_products/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<SupplierProductResponseDTO>update(
			@RequestBody SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/supplier_products/update").toUriString());
		return ResponseEntity.created(uri).body(supplierProductService.updateSupplierProduct(supplierProductRequest, request));
	}
	
	@PostMapping("/supplier_products/adjust_stock")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<SupplierProductResponseDTO>adjustStock(
			@RequestBody SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/supplier_products/adjust_stock").toUriString());
		return ResponseEntity.created(uri).body(supplierProductService.adjustSupplierStock(supplierProductRequest, request));
	}
	
	@PostMapping("/supplier_products/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/supplier_products/activate").toUriString());
		return ResponseEntity.created(uri).body(supplierProductService.activateSupplierProduct(supplierProductRequest, request));
	}
	
	@PostMapping("/supplier_products/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/supplier_products/deactivate").toUriString());
		return ResponseEntity.created(uri).body(supplierProductService.activateSupplierProduct(supplierProductRequest, request));
	}
	
	@GetMapping("/supplier_products/get_all_by_supplier_and_branch")
	public ResponseEntity<List<SupplierProductResponseDTO>>getAllSupplierProductsBySupplier(
			@RequestParam(name = "supplier_id")Long supplierId, 
			HttpServletRequest request){
		return ResponseEntity.ok().body(supplierProductService.getAllSupplierProductsByBranch(supplierId, request));
	}
	
	
	@GetMapping("/supplier_products/get_products_by_supplier_containing")
	public ResponseEntity<List<ProductResponseDTO>>getAllSupplierProductsBySupplierContaining(
			@RequestParam(name = "supplier_id")Long supplierId, 
			@RequestParam(name = "product_name_like")String productNameLike,
			HttpServletRequest request){

		return ResponseEntity.ok().body(supplierProductService.getProductsBySupplierAndName(supplierId, productNameLike));

	}
	
	

}
