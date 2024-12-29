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
public class ProductResource {

	private final ProductService productService;
	
	@GetMapping("/products")
	public ResponseEntity<List<ProductResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(productService.getAllProductes(request));
	}
	@GetMapping("/products/get")
	public ResponseEntity<ProductResponseDTO>get(
			@RequestParam(name = "id")Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(productService.get(id, request));		
	}
	
	@PostMapping("/products/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ProductResponseDTO>create(
			@RequestBody ProductRequestDTO productRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/products/create").toUriString());
		return ResponseEntity.created(uri).body(productService.createProduct(productRequest, request));
	}
	
	@PostMapping("/products/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ProductResponseDTO>update(
			@RequestBody ProductRequestDTO productRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/products/update").toUriString());
		return ResponseEntity.created(uri).body(productService.updateProduct(productRequest, request));
	}
	
	@PostMapping("/products/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody ProductRequestDTO productRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/products/activate").toUriString());
		return ResponseEntity.created(uri).body(productService.activateProduct(productRequest, request));
	}
	
	@PostMapping("/products/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody ProductRequestDTO productRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/products/deactivate").toUriString());
		return ResponseEntity.created(uri).body(productService.deactivateProduct(productRequest, request));
	}
	
	@GetMapping("/products/get_products_by_company")
	public ResponseEntity<List<ProductResponseDTO>>getProductsByCompany(
			@RequestParam(name = "product_name_like") String productNameLike,
			HttpServletRequest request){
		return ResponseEntity.ok().body(productService.getProductsByCompany(productNameLike, request));
	}
	
	@GetMapping("/products/get_company_products")
	public ResponseEntity<List<ProductResponseDTO>>getCompanyProducts(
			HttpServletRequest request){
		return ResponseEntity.ok().body(productService.getCompanyProducts(request));
	}
	
	@GetMapping("/products/get_company_sellable_products")
	public ResponseEntity<List<ProductResponseDTO>>getCompanySellableProducts(
			HttpServletRequest request){
		return ResponseEntity.ok().body(productService.getCompanySellableProducts(request));
	}
	
	@GetMapping("/products/get_company_sellable_products_by_shop")
	public ResponseEntity<List<ProductResponseDTO>>getCompanySellableProductsByShop(
			@RequestParam(name = "shop_id") Long shopId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(productService.getCompanySellableProductsByShop(shopId, request));
	}
	
	@GetMapping("/products/get_products_by_company_containing")
	public ResponseEntity<List<ProductResponseDTO>>getAllProductsByCompanyContaining( 
			@RequestParam(name = "product_name_like")String productNameLike,
			HttpServletRequest request){

		return ResponseEntity.ok().body(productService.getProductsByCompanyAndName(productNameLike, request));

	}
}
