package com.orbix.api.api.vehicleandequipmentparking;

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

import com.orbix.api.exceptions.InvalidOperationException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class VehicleEquipmentResource {
	
	private final VehicleEquipmentService vehicleEquipmentService;
	
	@GetMapping("/vehicle_equipments")
	public ResponseEntity<List<VehicleEquipmentResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(vehicleEquipmentService.getAllVehicleEquipments(request));
	}
	
	@GetMapping("/vehicle_equipments/get_all_active")
	public ResponseEntity<List<VehicleEquipmentResponseDTO>>getAllActive(HttpServletRequest request){
		return ResponseEntity.ok().body(vehicleEquipmentService.getAllActiveVehicleEquipments(request));
	}
	
	@GetMapping("/vehicle_equipments/get_by_chasis_no")
	public ResponseEntity<VehicleEquipmentResponseDTO>getByChasisNo(
			@RequestParam(name = "chasis_no") String chasisNo,
			HttpServletRequest request){
		return ResponseEntity.ok().body(vehicleEquipmentService.getByChasisNo(chasisNo, request));
	}
	
	@GetMapping("/vehicle_equipments/get_maintenance_by_chasis_no")
	public ResponseEntity<VehicleEquipmentResponseDTO>getMaintenanceByChasisNo(
			@RequestParam(name = "chasis_no") String chasisNo,
			HttpServletRequest request){
		return ResponseEntity.ok().body(vehicleEquipmentService.getMaintenanceByChasisNo(chasisNo, request));
	}
	
	@GetMapping("/vehicle_equipments/get_chasis_nos")
	public ResponseEntity<List<String>>getChasisNos(
			HttpServletRequest request){
		return ResponseEntity.ok().body(vehicleEquipmentService.getChasisNos(request));
	}
	
	
	@GetMapping("/vehicle_equipments/get")
	public ResponseEntity<VehicleEquipmentResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(vehicleEquipmentService.get(id, request));		
	}
	
	@PostMapping("/vehicle_equipments/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<VehicleEquipmentResponseDTO>create(
			@RequestBody VehicleEquipmentRequestDTO vehicleEquipmentRequest,
			HttpServletRequest request){	
		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_equipments/create").toUriString());
		return ResponseEntity.created(uri).body(vehicleEquipmentService.createVehicleEquipment(vehicleEquipmentRequest, request));
	}
	
	@PostMapping("/vehicle_equipments/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<VehicleEquipmentResponseDTO>update(
			@RequestBody VehicleEquipmentRequestDTO vehicleEquipmentRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/vehicle_equipments/update").toUriString());
		return ResponseEntity.created(uri).body(vehicleEquipmentService.updateVehicleEquipment(vehicleEquipmentRequest, request));
	}
}
