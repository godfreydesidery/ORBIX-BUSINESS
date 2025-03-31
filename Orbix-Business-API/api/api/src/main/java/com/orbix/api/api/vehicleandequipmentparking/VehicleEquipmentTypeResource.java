package com.orbix.api.api.vehicleandequipmentparking;

import java.net.URI;
import java.util.List;
import java.util.Optional;

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
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class VehicleEquipmentTypeResource {
	
	private final VehicleEquipmentTypeService vehicleEquipmentTypeService;
	private final UserService userService;
	private final VehicleEquipmentTypeRepository vehicleEquipmentTypeRepository;
	
	@GetMapping("/vehicle_equipment_types")
	public ResponseEntity<List<VehicleEquipmentTypeResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(vehicleEquipmentTypeService.getAllVehicleEquipmentTypes(request));
	}
	
	@GetMapping("/vehicle_equipment_types/get_all_company_active")
	public ResponseEntity<List<VehicleEquipmentTypeResponseDTO>>getAllCompanyActive(HttpServletRequest request){		
		return ResponseEntity.ok().body(vehicleEquipmentTypeService.getAllCompanyActiveVehicleEquipmentTypes(request));
	}
	
	
	@GetMapping("/vehicle_equipment_types/get")
	public ResponseEntity<VehicleEquipmentTypeResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(vehicleEquipmentTypeService.get(id, request));		
	}
	
	@PostMapping("/vehicle_equipment_types/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<VehicleEquipmentTypeResponseDTO>create(
			@RequestBody VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_equipment_types/create").toUriString());
		return ResponseEntity.created(uri).body(vehicleEquipmentTypeService.createVehicleEquipmentType(vehicleEquipmentTypeRequest, request));
	}
	
	@PostMapping("/vehicle_equipment_types/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<VehicleEquipmentTypeResponseDTO>update(
			@RequestBody VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_equipment_types/update").toUriString());
		return ResponseEntity.created(uri).body(vehicleEquipmentTypeService.updateVehicleEquipmentType(vehicleEquipmentTypeRequest, request));
	}
	
	@PostMapping("/vehicle_equipment_types/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_equipment_types/activate").toUriString());
		return ResponseEntity.created(uri).body(vehicleEquipmentTypeService.activateVehicleEquipmentType(vehicleEquipmentTypeRequest, request));
	}
	
	
	@PostMapping("/vehicle_equipment_types/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_equipment_types/deactivate").toUriString());
		return ResponseEntity.created(uri).body(vehicleEquipmentTypeService.deactivateVehicleEquipmentType(vehicleEquipmentTypeRequest, request));
	}
	
	
}
