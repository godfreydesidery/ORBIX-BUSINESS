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
public class MaintenanceIssueTypeResource {
	private final MaintenanceIssueTypeService maintenanceIssueTypeService;
	private final UserService userService;
	private final MaintenanceIssueTypeRepository maintenanceIssueTypeRepository;
	
	@GetMapping("/maintenance_issue_types")
	public ResponseEntity<List<MaintenanceIssueTypeResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(maintenanceIssueTypeService.getAllMaintenanceIssueTypes(request));
	}
	
	@GetMapping("/maintenance_issue_types/get_all_company_active")
	public ResponseEntity<List<MaintenanceIssueTypeResponseDTO>>getAllCompanyActive(HttpServletRequest request){		
		return ResponseEntity.ok().body(maintenanceIssueTypeService.getAllCompanyActiveMaintenanceIssueTypes(request));
	}
	
	
	@GetMapping("/maintenance_issue_types/get")
	public ResponseEntity<MaintenanceIssueTypeResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(maintenanceIssueTypeService.get(id, request));		
	}
	
	@PostMapping("/maintenance_issue_types/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceIssueTypeResponseDTO>create(
			@RequestBody MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_issue_types/create").toUriString());
		return ResponseEntity.created(uri).body(maintenanceIssueTypeService.createMaintenanceIssueType(maintenanceIssueTypeRequest, request));
	}
	
	@PostMapping("/maintenance_issue_types/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceIssueTypeResponseDTO>update(
			@RequestBody MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_issue_types/update").toUriString());
		return ResponseEntity.created(uri).body(maintenanceIssueTypeService.updateMaintenanceIssueType(maintenanceIssueTypeRequest, request));
	}
	
	@PostMapping("/maintenance_issue_types/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_issue_types/activate").toUriString());
		return ResponseEntity.created(uri).body(maintenanceIssueTypeService.activateMaintenanceIssueType(maintenanceIssueTypeRequest, request));
	}
	
	
	@PostMapping("/maintenance_issue_types/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_issue_types/deactivate").toUriString());
		return ResponseEntity.created(uri).body(maintenanceIssueTypeService.deactivateMaintenanceIssueType(maintenanceIssueTypeRequest, request));
	}
}
