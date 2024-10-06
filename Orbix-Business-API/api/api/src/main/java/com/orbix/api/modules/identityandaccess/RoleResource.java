package com.orbix.api.modules.identityandaccess;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RoleResource {
	
	private final RoleService roleService;
	
	
	@GetMapping("/roles/get_all_roles")
	public ResponseEntity<List<RoleResponseDTO>>getAllRoles(HttpServletRequest request){
		return ResponseEntity.ok().body(roleService.getAllRoles(request));
	}
	
	@PostMapping("/roles/create_role")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RoleResponseDTO>createRole(
			@RequestBody RoleRequestDTO roleRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/roles/create_role").toUriString());
		return ResponseEntity.created(uri).body(roleService.createRole(roleRequest, request));
	}
	
	@PostMapping("/companies/update_role")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RoleResponseDTO>updateRole(
			@RequestBody RoleRequestDTO roleRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/roles/update_role").toUriString());
		return ResponseEntity.created(uri).body(roleService.updateRole(roleRequest, request));
	}
}
