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
public class ParkingResource {
	
private final ParkingService parkingService;
	
	@GetMapping("/parkings")
	public ResponseEntity<List<ParkingResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingService.getAllParkings(request));
	}
	
	@GetMapping("/parkings/get_all_pending_or_checked_in")
	public ResponseEntity<List<ParkingResponseDTO>>getAllPendingAndCheckedIn(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingService.getAllPendingOrCheckedInParkings(request));
	}
	
	
	@GetMapping("/parkings/get")
	public ResponseEntity<ParkingResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(parkingService.get(id, request));		
	}
	
	@PostMapping("/parkings/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingResponseDTO>create(
			@RequestBody ParkingRequestDTO parkingRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/create").toUriString());
		return ResponseEntity.created(uri).body(parkingService.createParking(parkingRequest, request));
	}
	
	@PostMapping("/parkings/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingResponseDTO>update(
			@RequestBody ParkingRequestDTO parkingRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/update").toUriString());
		return ResponseEntity.created(uri).body(parkingService.updateParking(parkingRequest, request));
	}
	
	@PostMapping("/parkings/check_in")
	public ResponseEntity<ParkingResponseDTO>checkIn(
			@RequestBody ParkingRequestDTO parkingRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(parkingService.checkIn(parkingRequest, request));		
	}
	
	@PostMapping("/parkings/check_out")
	public ResponseEntity<ParkingResponseDTO>checkOut(
			@RequestBody ParkingRequestDTO parkingRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(parkingService.checkOut(parkingRequest, request));		
	}
	
//	@PostMapping("/parkings/activate")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ApiCustomResponse>activate(
//			@RequestBody ParkingRequestDTO parkingRequest,
//			HttpServletRequest request){
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/activate").toUriString());
//		return ResponseEntity.created(uri).body(parkingService.activateParking(parkingRequest, request));
//	}
	
	
//	@PostMapping("/parkings/deactivate")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ApiCustomResponse>deactivate(
//			@RequestBody ParkingRequestDTO parkingRequest,
//			HttpServletRequest request){
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/deactivate").toUriString());
//		return ResponseEntity.created(uri).body(parkingService.deactivateParking(parkingRequest, request));
//	}
	
}
