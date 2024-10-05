package com.orbix.api.modules.adminunits;

import java.net.URI;
import java.util.Collections;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.api.commons.ApiCustomResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class CompanyResource {
	
	private final CompanyService companyService;
	/**
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/companies")
	public ResponseEntity<List<CompanyResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(companyService.getAllCompanies(request));
	}
	
	/**
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@GetMapping("/companies/get")
	public ResponseEntity<CompanyResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(companyService.get(id, request));		
	}
	/**
	 * 
	 * @param companyRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/companies/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<CompanyResponseDTO>create(
			@RequestBody CompanyRequestDTO companyRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/companies/create").toUriString());
		return ResponseEntity.created(uri).body(companyService.createCompany(companyRequest, request));
	}
	/**
	 * 
	 * @param companyRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/companies/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<CompanyResponseDTO>update(
			@RequestBody CompanyRequestDTO companyRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/companies/update").toUriString());
		return ResponseEntity.created(uri).body(companyService.updateCompany(companyRequest, request));
	}
	/**
	 * 
	 * @param companyRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/companies/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody CompanyRequestDTO companyRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/companies/activate").toUriString());
		return ResponseEntity.created(uri).body(companyService.activateCompany(companyRequest, request));
	}
	
	/**
	 * 
	 * @param companyRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/companies/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody CompanyRequestDTO companyRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/companies/deactivate").toUriString());
		return ResponseEntity.created(uri).body(companyService.deactivateCompany(companyRequest, request));
	}
	
	
}
