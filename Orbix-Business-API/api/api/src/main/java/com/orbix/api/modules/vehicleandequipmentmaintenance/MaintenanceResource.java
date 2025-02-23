package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class MaintenanceResource {
	
	private final MaintenanceService maintenanceService;	
	
	@GetMapping("/maintenances")
	public ResponseEntity<List<MaintenanceResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(maintenanceService.getAllMaintenances(request));
	}
	
	@GetMapping("/maintenances/get_all_pending_or_checked_in")
	public ResponseEntity<List<MaintenanceResponseDTO>>getAllPendingAndCheckedIn(HttpServletRequest request){
		return ResponseEntity.ok().body(maintenanceService.getAllPendingOrCheckedInMaintenances(request));
	}
	
	@GetMapping("/maintenances/get_all_checked_in")
	public ResponseEntity<List<MaintenanceResponseDTO>>getAllCheckedIn(HttpServletRequest request){
		return ResponseEntity.ok().body(maintenanceService.getAllCheckedInMaintenances(request));
	}
	
	@GetMapping("/maintenances/get_all_cleared")
	public ResponseEntity<List<MaintenanceResponseDTO>>getAllCleared(HttpServletRequest request){
		return ResponseEntity.ok().body(maintenanceService.getAllCleared(request));
	}
	
	@GetMapping("/maintenances/get_today_checked_out")
	public ResponseEntity<List<MaintenanceResponseDTO>>getTodayCheckedOut(HttpServletRequest request){
		return ResponseEntity.ok().body(maintenanceService.getTodayCheckedOut(request));
	}
	
	@GetMapping("/maintenances/get_recent_checked_out")
	public ResponseEntity<List<MaintenanceResponseDTO>>getRecentCheckedOut(HttpServletRequest request){
		return ResponseEntity.ok().body(maintenanceService.getRecentCheckedOut(request));
	}
	
	
	@GetMapping("/maintenances/get")
	public ResponseEntity<MaintenanceResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(maintenanceService.get(id, request));		
	}
	
	@GetMapping("/maintenances/get_maintenance_bill_receivables")
	public ResponseEntity<List<MaintenanceJobCardIssueBillReceivableResponseDTO>>getMaintenanceBillReceivables(
			@RequestParam(name = "maintenance_id") Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(maintenanceService.getMaintenanceJobCardIssueBillReceivables(id, request));		
	}
	
	@PostMapping("/maintenances/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceResponseDTO>create(
			@RequestBody MaintenanceRequestDTO maintenanceRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenances/create").toUriString());
		return ResponseEntity.created(uri).body(maintenanceService.createMaintenance(maintenanceRequest, request));
	}
	
	@PostMapping("/maintenances/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceResponseDTO>update(
			@RequestBody MaintenanceRequestDTO maintenanceRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenances/update").toUriString());
		return ResponseEntity.created(uri).body(maintenanceService.updateMaintenance(maintenanceRequest, request));
	}
	
	@PostMapping("/maintenances/check_in")
	public ResponseEntity<MaintenanceResponseDTO>checkIn(
			@RequestBody MaintenanceRequestDTO maintenanceRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(maintenanceService.checkIn(maintenanceRequest, request));		
	}
	
	@PostMapping("/maintenances/check_out")
	public ResponseEntity<MaintenanceResponseDTO>checkOut(
			@RequestBody MaintenanceRequestDTO maintenanceRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(maintenanceService.checkOut(maintenanceRequest, request));		
	}
	
	@PostMapping("/maintenances/create_maintenance_job_card")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardResponseDTO>createJobCard(
			@RequestBody MaintenanceRequestDTO maintenanceRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenances/create_maintenance_job_card").toUriString());
		return ResponseEntity.created(uri).body(maintenanceService.createMaintenanceJobCard(maintenanceRequest, request));
	}
}

@Data
class Model{
	String stringData = "";
}