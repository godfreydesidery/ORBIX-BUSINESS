package com.orbix.api.modules.vehicleandequipmentmaintenance;

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

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class MaintenanceJobCardIssueResource {
private final MaintenanceJobCardIssueService maintenanceJobCardIssueService;	
	
//	@GetMapping("/maintenance_job_cards")
//	public ResponseEntity<List<MaintenanceResponseDTO>>getAll(HttpServletRequest request){
//		return ResponseEntity.ok().body(maintenanceService.getAllMaintenances(request));
//	}
	
//	@GetMapping("/maintenances/get_all_pending_or_checked_in")
//	public ResponseEntity<List<MaintenanceResponseDTO>>getAllPendingAndCheckedIn(HttpServletRequest request){
//		return ResponseEntity.ok().body(maintenanceService.getAllPendingOrCheckedInMaintenances(request));
//	}
	
//	@GetMapping("/maintenances/get_all_checked_in")
//	public ResponseEntity<List<MaintenanceResponseDTO>>getAllCheckedIn(HttpServletRequest request){
//		return ResponseEntity.ok().body(maintenanceService.getAllCheckedInMaintenances(request));
//	}
//	
//	@GetMapping("/maintenances/get_all_cleared")
//	public ResponseEntity<List<MaintenanceResponseDTO>>getAllCleared(HttpServletRequest request){
//		return ResponseEntity.ok().body(maintenanceService.getAllCleared(request));
//	}
//	
//	@GetMapping("/maintenances/get_today_checked_out")
//	public ResponseEntity<List<MaintenanceResponseDTO>>getTodayCheckedOut(HttpServletRequest request){
//		return ResponseEntity.ok().body(maintenanceService.getTodayCheckedOut(request));
//	}
//	
//	@GetMapping("/maintenances/get_recent_checked_out")
//	public ResponseEntity<List<MaintenanceResponseDTO>>getRecentCheckedOut(HttpServletRequest request){
//		return ResponseEntity.ok().body(maintenanceService.getRecentCheckedOut(request));
//	}
//	
//	
//	@GetMapping("/maintenances/get")
//	public ResponseEntity<MaintenanceResponseDTO>get(
//			Long id,
//			HttpServletRequest request){		
//		return ResponseEntity.ok().body(maintenanceService.get(id, request));		
//	}
	
//	@GetMapping("/maintenances/get_maintenance_bill_receivables")
//	public ResponseEntity<List<MaintenanceBillReceivableResponseDTO>>getMaintenanceBillReceivables(
//			@RequestParam(name = "maintenance_id") Long id,
//			HttpServletRequest request){		
//		return ResponseEntity.ok().body(maintenanceService.getMaintenanceBillReceivables(id, request));		
//	}



	@GetMapping("/maintenance_job_card_issues/get")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardIssueResponseDTO>get(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_job_card_issues/get").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueService.get(id, request));
	}

	@GetMapping("/maintenance_job_card_issues/get_my_jobs")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<List<MaintenanceJobCardIssueResponseDTO>>getMyJobs(HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_job_card_issues/get_my_jobs").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueService.getMyJobs(request));
	}

	
	@PostMapping("/maintenance_job_card_issues/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardIssueResponseDTO>create(
			@RequestBody MaintenanceJobCardRequestDTO maintenanceJobCardRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_job_card_issues/create").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueService.createMaintenanceJobCardIssue(maintenanceJobCardRequest, request));
	}
	
	@PostMapping("/maintenance_job_card_issues/open")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardIssueResponseDTO>create(
			@RequestBody MaintenanceJobCardIssueRequestDTO maintenanceJobCardIssueRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_job_card_issues/open").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueService.openMaintenanceJobCardIssue(maintenanceJobCardIssueRequest, request));
	}
	
	@PostMapping("/maintenance_job_card_issues/close")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardIssueResponseDTO>close(
			@RequestBody MaintenanceJobCardIssueRequestDTO maintenanceJobCardIssueRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_job_card_issues/close").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueService.closeMaintenanceJobCardIssue(maintenanceJobCardIssueRequest, request));
	}
	
	@PostMapping("/maintenance_job_card_issues/remove")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardIssueResponseDTO>remove(
			@RequestBody MaintenanceJobCardIssueRequestDTO maintenanceJobCardIssueRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_job_card_issues/remove").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueService.removeMaintenanceJobCardIssue(maintenanceJobCardIssueRequest, request));
	}
	
	@PostMapping("/maintenance_job_card_issues/done")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardIssueResponseDTO>done(
			@RequestBody MaintenanceJobCardIssueRequestDTO maintenanceJobCardIssueRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_job_card_issues/done").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueService.doneMaintenanceJobCardIssue(maintenanceJobCardIssueRequest, request));
	}
	
	@PostMapping("/maintenance_job_card_issues/save_comments")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardIssueResponseDTO>saveComments(
			@RequestBody MaintenanceJobCardIssueRequestDTO maintenanceJobCardIssueRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_job_card_issues/save_comments").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueService.saveComments(maintenanceJobCardIssueRequest, request));
	}
	
//	@PostMapping("/maintenances/update")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<MaintenanceResponseDTO>update(
//			@RequestBody MaintenanceRequestDTO maintenanceRequest,
//			HttpServletRequest request){		
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenances/update").toUriString());
//		return ResponseEntity.created(uri).body(maintenanceService.updateMaintenance(maintenanceRequest, request));
//	}
//	
//	@PostMapping("/maintenances/check_in")
//	public ResponseEntity<MaintenanceResponseDTO>checkIn(
//			@RequestBody MaintenanceRequestDTO maintenanceRequest,
//			HttpServletRequest request){		
//		return ResponseEntity.ok().body(maintenanceService.checkIn(maintenanceRequest, request));		
//	}
//	
//	@PostMapping("/maintenances/check_out")
//	public ResponseEntity<MaintenanceResponseDTO>checkOut(
//			@RequestBody MaintenanceRequestDTO maintenanceRequest,
//			HttpServletRequest request){		
//		return ResponseEntity.ok().body(maintenanceService.checkOut(maintenanceRequest, request));		
//	}
//	
//	@PostMapping("/maintenances/create_maintenance_job_card")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<MaintenanceJobCardResponseDTO>createJobCard(
//			@RequestBody MaintenanceRequestDTO maintenanceRequest,
//			HttpServletRequest request){		
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenances/create_maintenance_job_card").toUriString());
//		return ResponseEntity.created(uri).body(maintenanceService.createMaintenanceJobCard(maintenanceRequest, request));
//	}
}

//@Data
//class Model{
//	String stringData = "";
//}