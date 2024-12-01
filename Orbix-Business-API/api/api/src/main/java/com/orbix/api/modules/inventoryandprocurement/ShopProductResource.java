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
public class ShopProductResource {
	
	private final ShopProductRepository shopProductRepository;
	
	private final ShopProductService shopProductService;
	
	@GetMapping("/shop_products")
	public ResponseEntity<List<ShopProductResponseDTO>>getAllShopProducts(
			@RequestParam(name = "shop_id")Long shopId, 
			HttpServletRequest request){
		return ResponseEntity.ok().body(shopProductService.getAllShopProducts(shopId, request));
	}
	@GetMapping("/shop_products/get")
	public ResponseEntity<ShopProductResponseDTO>get(
			@RequestParam(name = "id")Long id,
			@RequestParam(name = "shop_id")Long shopId,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(shopProductService.get(id, shopId, request));		
	}
	
	@PostMapping("/shop_products/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ShopProductResponseDTO>create(
			@RequestBody ShopProductRequestDTO shopProductRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shop_products/create").toUriString());
		return ResponseEntity.created(uri).body(shopProductService.createShopProduct(shopProductRequest, request));
	}
	
	@PostMapping("/shop_products/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ShopProductResponseDTO>update(
			@RequestBody ShopProductRequestDTO shopProductRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shop_products/update").toUriString());
		return ResponseEntity.created(uri).body(shopProductService.updateShopProduct(shopProductRequest, request));
	}
	
	@PostMapping("/shop_products/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody ShopProductRequestDTO shopProductRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shop_products/activate").toUriString());
		return ResponseEntity.created(uri).body(shopProductService.activateShopProduct(shopProductRequest, request));
	}
	
	@PostMapping("/shop_products/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody ShopProductRequestDTO shopProductRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shop_products/deactivate").toUriString());
		return ResponseEntity.created(uri).body(shopProductService.activateShopProduct(shopProductRequest, request));
	}
	
	@GetMapping("/shop_products/get_stock_by_shop")
	public ResponseEntity<List<ShopProductResponseDTO>>getAllShopProductsByShop(
			@RequestParam(name = "shop_id")Long shopId, 
			HttpServletRequest request){
		return ResponseEntity.ok().body(shopProductService.getAllShopProducts(shopId, request));
	}
	
	
//	@GetMapping("/shop_products/get_products_by_shop_containing")
//	public ResponseEntity<List<ShopProductResponseDTO>>getAllShopProductsByShopContaining(
//			@RequestParam(name = "shop_id")Long shopId, 
//			HttpServletRequest request){
//		
//		List<ShopProduct> shopProducts = shopProductRepository.findAllByShopAndProduct
//		
//				return shopProductRepository.findAllByShopAndProduct_NameContainingIgnoreCase(shop, productName);
//		return ResponseEntity.ok().body(shopProductService.getAllShopProducts(shopId, request));
//	}
	

}
