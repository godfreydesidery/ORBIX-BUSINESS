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
public class BondZoneResource {

	private final BondZoneService bondZoneService;
	
	@GetMapping("/bond_zones")
	public ResponseEntity<List<BondZoneResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(bondZoneService.getAllBondZones(request));
	}
	
	@GetMapping("/bond_zones/get_all_branch_active")
	public ResponseEntity<List<BondZoneResponseDTO>>getAllBranchActive(HttpServletRequest request){		
		return ResponseEntity.ok().body(bondZoneService.getAllBranchActiveBondZones(request));
	}
	
	
	@GetMapping("/bond_zones/get")
	public ResponseEntity<BondZoneResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(bondZoneService.get(id, request));		
	}
	
	@PostMapping("/bond_zones/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondZoneResponseDTO>create(
			@RequestBody BondZoneRequestDTO bondZoneRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_zones/create").toUriString());
		return ResponseEntity.created(uri).body(bondZoneService.createBondZone(bondZoneRequest, request));
	}
	
	@PostMapping("/bond_zones/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondZoneResponseDTO>update(
			@RequestBody BondZoneRequestDTO bondZoneRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_zones/update").toUriString());
		return ResponseEntity.created(uri).body(bondZoneService.updateBondZone(bondZoneRequest, request));
	}
	
	@PostMapping("/bond_zones/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody BondZoneRequestDTO bondZoneRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_zones/activate").toUriString());
		return ResponseEntity.created(uri).body(bondZoneService.activateBondZone(bondZoneRequest, request));
	}
	
	
	@PostMapping("/bond_zones/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody BondZoneRequestDTO bondZoneRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_zones/deactivate").toUriString());
		return ResponseEntity.created(uri).body(bondZoneService.deactivateBondZone(bondZoneRequest, request));
	}
	
	@GetMapping("/bond_zones/get_branch_available_bond_zones_by_user")
	public ResponseEntity<List<BondZoneResponseDTO>>getBranchAvailableByUser(HttpServletRequest request){
		return ResponseEntity.ok().body(bondZoneService.getBranchAvailableBondZonesByUser(request));
	}
	
	@GetMapping("/bond_zones/get_selected_bond_zone")
	public ResponseEntity<BondZoneResponseDTO>getSelectedBondZoneByUser(
			@RequestParam(name = "bond_zone_id")Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(bondZoneService.get(id, request));		
	}
	
}
