package com.orbix.api.modules.adminunits;

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
import com.orbix.api.modules.audit.Audited;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class WorkshopResource {
	private final WorkshopService workshopService;

	@GetMapping("/workshops")
	public ResponseEntity<List<WorkshopResponseDTO>> getAll(HttpServletRequest request) {
		return ResponseEntity.ok().body(workshopService.getAllWorkshops(request));
	}

	@GetMapping("/workshops/get")
	public ResponseEntity<WorkshopResponseDTO> get(Long id, HttpServletRequest request) {
		return ResponseEntity.ok().body(workshopService.get(id, request));
	}

	@PostMapping("/workshops/create")
	@Audited(category = "SETTINGS", action = "RECORD_CREATED", entityType = "Workshop", summary = "Created workshop {ref}")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<WorkshopResponseDTO> create(@RequestBody WorkshopRequestDTO workshopRequest, HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/workshops/create").toUriString());
		return ResponseEntity.created(uri).body(workshopService.createWorkshop(workshopRequest, request));
	}

	@PostMapping("/workshops/update")
	@Audited(category = "SETTINGS", action = "RECORD_UPDATED", entityType = "Workshop", summary = "Updated workshop {ref}", changeOf = Workshop.class, changeId = "workshopRequest.id", changedFieldPattern = "(?i).*price.*", changedAction = "PRICE_CHANGED")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<WorkshopResponseDTO> update(@RequestBody WorkshopRequestDTO workshopRequest, HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/workshops/update").toUriString());
		return ResponseEntity.created(uri).body(workshopService.updateWorkshop(workshopRequest, request));
	}

	@PostMapping("/workshops/activate")
	@Audited(category = "SETTINGS", action = "RECORD_ACTIVATED", entityType = "Workshop", entityId = "workshopRequest.id", summary = "Activated workshop {ref}", changeOf = Workshop.class, changeId = "workshopRequest.id")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse> activate(@RequestBody WorkshopRequestDTO workshopRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/workshops/activate").toUriString());
		return ResponseEntity.created(uri).body(workshopService.activateWorkshop(workshopRequest, request));
	}

	@PostMapping("/workshops/deactivate")
	@Audited(category = "SETTINGS", action = "RECORD_DEACTIVATED", entityType = "Workshop", entityId = "workshopRequest.id", summary = "Deactivated workshop {ref}", changeOf = Workshop.class, changeId = "workshopRequest.id")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse> deactivate(@RequestBody WorkshopRequestDTO workshopRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/workshops/deactivate").toUriString());
		return ResponseEntity.created(uri).body(workshopService.deactivateWorkshop(workshopRequest, request));
	}

	@GetMapping("/workshops/get_branch_available_workshops_by_user")
	public ResponseEntity<List<WorkshopResponseDTO>> getBranchAvailableByUser(HttpServletRequest request) {
		return ResponseEntity.ok().body(workshopService.getBranchAvailableWorkshopsByUser(request));
	}

	@GetMapping("/workshops/get_selected_workshop")
	public ResponseEntity<WorkshopResponseDTO> getSelectedWorkshopByUser(@RequestParam(name = "workshop_id") Long id,
			HttpServletRequest request) {
		return ResponseEntity.ok().body(workshopService.get(id, request));
	}

	@GetMapping("/workshops/get_branch_workshops")
	public ResponseEntity<List<WorkshopResponseDTO>> getBranchWorkshops(HttpServletRequest request) {
		return ResponseEntity.ok().body(workshopService.getBranchWorkshops(request));
	}
}
