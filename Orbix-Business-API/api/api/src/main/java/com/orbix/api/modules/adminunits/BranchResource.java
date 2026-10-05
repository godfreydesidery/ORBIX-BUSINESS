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
public class BranchResource {
	
	private final BranchService branchService;
	
	@GetMapping("/branches")
	public ResponseEntity<List<BranchResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(branchService.getAllBranches(request));
	}
	@GetMapping("/branches/get")
	public ResponseEntity<BranchResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(branchService.get(id, request));		
	}
	
	@PostMapping("/branches/create")
	@Audited(category = "SETTINGS", action = "RECORD_CREATED", entityType = "Branch", summary = "Created branch {ref}")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BranchResponseDTO>create(
			@RequestBody BranchRequestDTO branchRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/branches/create").toUriString());
		return ResponseEntity.created(uri).body(branchService.createBranch(branchRequest, request));
	}
	
	@PostMapping("/branches/update")
	@Audited(category = "SETTINGS", action = "RECORD_UPDATED", entityType = "Branch", summary = "Updated branch {ref}", changeOf = Branch.class, changeId = "branchRequest.id", changedFieldPattern = "(?i).*price.*", changedAction = "PRICE_CHANGED")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BranchResponseDTO>update(
			@RequestBody BranchRequestDTO branchRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/branches/update").toUriString());
		return ResponseEntity.created(uri).body(branchService.updateBranch(branchRequest, request));
	}
	
	@PostMapping("/branches/activate")
	@Audited(category = "SETTINGS", action = "RECORD_ACTIVATED", entityType = "Branch", entityId = "branchRequest.id", summary = "Activated branch {ref}", changeOf = Branch.class, changeId = "branchRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody BranchRequestDTO branchRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/branches/activate").toUriString());
		return ResponseEntity.created(uri).body(branchService.activateBranch(branchRequest, request));
	}
	
	@PostMapping("/branches/deactivate")
	@Audited(category = "SETTINGS", action = "RECORD_DEACTIVATED", entityType = "Branch", entityId = "branchRequest.id", summary = "Deactivated branch {ref}", changeOf = Branch.class, changeId = "branchRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody BranchRequestDTO branchRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/branches/deactivate").toUriString());
		return ResponseEntity.created(uri).body(branchService.deactivateBranch(branchRequest, request));
	}
}
