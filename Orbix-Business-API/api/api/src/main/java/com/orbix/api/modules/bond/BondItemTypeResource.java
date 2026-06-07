package com.orbix.api.modules.bond;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class BondItemTypeResource {
	private final BondItemTypeService bondItemTypeService;
	private final UserService userService;
	private final BondItemTypeRepository bondItemTypeRepository;
	
	@GetMapping("/bond_item_types")
	public ResponseEntity<List<BondItemTypeResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemTypeService.getAllBondItemTypes(request));
	}
	
	@GetMapping("/bond_item_types/get_all_company_active")
	public ResponseEntity<List<BondItemTypeResponseDTO>>getAllCompanyActive(HttpServletRequest request){		
		return ResponseEntity.ok().body(bondItemTypeService.getAllCompanyActiveBondItemTypes(request));
	}
	
	
	@GetMapping("/bond_item_types/get")
	public ResponseEntity<BondItemTypeResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(bondItemTypeService.get(id, request));		
	}
	
	@PostMapping("/bond_item_types/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemTypeResponseDTO>create(
			@RequestBody BondItemTypeRequestDTO bondItemTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_item_types/create").toUriString());
		return ResponseEntity.created(uri).body(bondItemTypeService.createBondItemType(bondItemTypeRequest, request));
	}
	
	@PostMapping("/bond_item_types/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemTypeResponseDTO>update(
			@RequestBody BondItemTypeRequestDTO bondItemTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_item_types/update").toUriString());
		return ResponseEntity.created(uri).body(bondItemTypeService.updateBondItemType(bondItemTypeRequest, request));
	}
	
	@PostMapping("/bond_item_types/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody BondItemTypeRequestDTO bondItemTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_item_types/activate").toUriString());
		return ResponseEntity.created(uri).body(bondItemTypeService.activateBondItemType(bondItemTypeRequest, request));
	}
	
	
	@PostMapping("/bond_item_types/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody BondItemTypeRequestDTO bondItemTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_item_types/deactivate").toUriString());
		return ResponseEntity.created(uri).body(bondItemTypeService.deactivateBondItemType(bondItemTypeRequest, request));
	}
}
