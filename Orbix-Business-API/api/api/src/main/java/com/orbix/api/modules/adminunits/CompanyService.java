package com.orbix.api.modules.adminunits;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface CompanyService {
	List<CompanyResponseDTO> getAllCompanies(HttpServletRequest request);	
	CompanyResponseDTO get(Long id, HttpServletRequest request);
	CompanyResponseDTO createCompany(CompanyRequestDTO company, HttpServletRequest request);
	CompanyResponseDTO updateCompany(CompanyRequestDTO company, HttpServletRequest request);
	ApiCustomResponse activateCompany(CompanyRequestDTO company, HttpServletRequest request);
	ApiCustomResponse deactivateCompany(CompanyRequestDTO company, HttpServletRequest request);
	
}
