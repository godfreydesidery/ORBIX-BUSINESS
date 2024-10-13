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
public class VehicleAndEquipmentTypeResource {
	
private final VehicleAndEquipmentTypeService vehicleAndEquipmentTypeService;
private final UserService userService;
private final VehicleAndEquipmentTypeRepository vehicleAndEquipmentTypeRepository;
	
	@GetMapping("/vehicle_and_equipment_types")
	public ResponseEntity<List<VehicleAndEquipmentTypeResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(vehicleAndEquipmentTypeService.getAllVehicleAndEquipmentTypes(request));
	}
	
	@GetMapping("/vehicle_and_equipment_types/get_all_company_active")
	public ResponseEntity<List<VehicleAndEquipmentTypeResponseDTO>>getAllCompanyActive(HttpServletRequest request){		
		return ResponseEntity.ok().body(vehicleAndEquipmentTypeService.getAllCompanyActiveVehicleAndEquipmentTypes(request));
	}
	
	
	@GetMapping("/vehicle_and_equipment_types/get")
	public ResponseEntity<VehicleAndEquipmentTypeResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(vehicleAndEquipmentTypeService.get(id, request));		
	}
	
	@PostMapping("/vehicle_and_equipment_types/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<VehicleAndEquipmentTypeResponseDTO>create(
			@RequestBody VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_and_equipment_types/create").toUriString());
		return ResponseEntity.created(uri).body(vehicleAndEquipmentTypeService.createVehicleAndEquipmentType(vehicleAndEquipmentTypeRequest, request));
	}
	
	@PostMapping("/vehicle_and_equipment_types/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<VehicleAndEquipmentTypeResponseDTO>update(
			@RequestBody VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_and_equipment_types/update").toUriString());
		return ResponseEntity.created(uri).body(vehicleAndEquipmentTypeService.updateVehicleAndEquipmentType(vehicleAndEquipmentTypeRequest, request));
	}
	
	@PostMapping("/vehicle_and_equipment_types/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_and_equipment_types/activate").toUriString());
		return ResponseEntity.created(uri).body(vehicleAndEquipmentTypeService.activateVehicleAndEquipmentType(vehicleAndEquipmentTypeRequest, request));
	}
	
	
	@PostMapping("/vehicle_and_equipment_types/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_and_equipment_types/deactivate").toUriString());
		return ResponseEntity.created(uri).body(vehicleAndEquipmentTypeService.deactivateVehicleAndEquipmentType(vehicleAndEquipmentTypeRequest, request));
	}
	
	
}
