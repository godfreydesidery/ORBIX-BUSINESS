package com.orbix.api.modules.inventoryandprocurement;

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
public class UomResource {
	
private final UomService uomService;
	
	@GetMapping("/uoms")
	public ResponseEntity<List<UomResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(uomService.getAllUomes(request));
	}
	@GetMapping("/uoms/get")
	public ResponseEntity<UomResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(uomService.get(id, request));		
	}
	
	@PostMapping("/uoms/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<UomResponseDTO>create(
			@RequestBody UomRequestDTO uomRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/uoms/create").toUriString());
		return ResponseEntity.created(uri).body(uomService.createUom(uomRequest, request));
	}
	
	@PostMapping("/uoms/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<UomResponseDTO>update(
			@RequestBody UomRequestDTO uomRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/uoms/update").toUriString());
		return ResponseEntity.created(uri).body(uomService.updateUom(uomRequest, request));
	}
	
	@PostMapping("/uoms/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody UomRequestDTO uomRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/uoms/activate").toUriString());
		return ResponseEntity.created(uri).body(uomService.activateUom(uomRequest, request));
	}
	
	@PostMapping("/uoms/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody UomRequestDTO uomRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/uoms/deactivate").toUriString());
		return ResponseEntity.created(uri).body(uomService.deactivateUom(uomRequest, request));
	}
}
