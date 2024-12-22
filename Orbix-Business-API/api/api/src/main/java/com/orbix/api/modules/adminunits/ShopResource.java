package com.orbix.api.modules.adminunits;

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
public class ShopResource {
	
	private final ShopService shopService;
	
	@GetMapping("/shops")
	public ResponseEntity<List<ShopResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(shopService.getAllShops(request));
	}
	@GetMapping("/shops/get")
	public ResponseEntity<ShopResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(shopService.get(id, request));		
	}
	
	@PostMapping("/shops/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ShopResponseDTO>create(
			@RequestBody ShopRequestDTO shopRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shops/create").toUriString());
		return ResponseEntity.created(uri).body(shopService.createShop(shopRequest, request));
	}
	
	@PostMapping("/shops/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ShopResponseDTO>update(
			@RequestBody ShopRequestDTO shopRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shops/update").toUriString());
		return ResponseEntity.created(uri).body(shopService.updateShop(shopRequest, request));
	}
	
	@PostMapping("/shops/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody ShopRequestDTO shopRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shops/activate").toUriString());
		return ResponseEntity.created(uri).body(shopService.activateShop(shopRequest, request));
	}
	
	@PostMapping("/shops/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody ShopRequestDTO shopRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shops/deactivate").toUriString());
		return ResponseEntity.created(uri).body(shopService.deactivateShop(shopRequest, request));
	}
	
	
	@GetMapping("/shops/get_branch_available_shops_by_user")
	public ResponseEntity<List<ShopResponseDTO>>getBranchAvailableByUser(HttpServletRequest request){
		return ResponseEntity.ok().body(shopService.getBranchAvailableShopsByUser(request));
	}
	
	@GetMapping("/shops/get_selected_shop")
	public ResponseEntity<ShopResponseDTO>getSelectedShopByUser(
			@RequestParam(name = "shop_id")Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(shopService.get(id, request));		
	}
}
