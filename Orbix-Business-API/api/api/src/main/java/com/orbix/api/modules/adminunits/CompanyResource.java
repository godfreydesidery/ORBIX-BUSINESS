package com.orbix.api.modules.adminunits;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class CompanyResource {
	
	private final CompanyService companyService;
	
	@GetMapping("/companies/get_all_companies")
	public ResponseEntity<List<CompanyResponseDTO>>getAllCompanies(HttpServletRequest request){
		return ResponseEntity.ok().body(companyService.getAllCompanies(request));
	}
	
	@PostMapping("/companies/create_company")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<CompanyResponseDTO>createCompany(
			@RequestBody CompanyRequestDTO companyRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/companies/create_company").toUriString());
		return ResponseEntity.created(uri).body(companyService.createCompany(companyRequest, request));
	}
	
	@PostMapping("/companies/update_company")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<CompanyResponseDTO>updateCompany(
			@RequestBody CompanyRequestDTO companyRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/companies/update_company").toUriString());
		return ResponseEntity.created(uri).body(companyService.updateCompany(companyRequest, request));
	}
	
	
}
