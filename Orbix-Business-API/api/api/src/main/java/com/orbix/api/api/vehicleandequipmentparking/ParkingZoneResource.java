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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.api.commons.ApiCustomResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class ParkingZoneResource {
	private final ParkingZoneService parkingZoneService;
	
	@GetMapping("/parking_zones")
	public ResponseEntity<List<ParkingZoneResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingZoneService.getAllParkingZones(request));
	}
	
	
	@GetMapping("/parking_zones/get")
	public ResponseEntity<ParkingZoneResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(parkingZoneService.get(id, request));		
	}
	
	@PostMapping("/parking_zones/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingZoneResponseDTO>create(
			@RequestBody ParkingZoneRequestDTO parkingZoneRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_zones/create").toUriString());
		return ResponseEntity.created(uri).body(parkingZoneService.createParkingZone(parkingZoneRequest, request));
	}
	
	@PostMapping("/parking_zones/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingZoneResponseDTO>update(
			@RequestBody ParkingZoneRequestDTO parkingZoneRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_zones/update").toUriString());
		return ResponseEntity.created(uri).body(parkingZoneService.updateParkingZone(parkingZoneRequest, request));
	}
	
	@PostMapping("/parking_zones/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody ParkingZoneRequestDTO parkingZoneRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_zones/activate").toUriString());
		return ResponseEntity.created(uri).body(parkingZoneService.activateParkingZone(parkingZoneRequest, request));
	}
	
	
	@PostMapping("/parking_zones/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody ParkingZoneRequestDTO parkingZoneRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_zones/deactivate").toUriString());
		return ResponseEntity.created(uri).body(parkingZoneService.deactivateParkingZone(parkingZoneRequest, request));
	}
	
	
}
