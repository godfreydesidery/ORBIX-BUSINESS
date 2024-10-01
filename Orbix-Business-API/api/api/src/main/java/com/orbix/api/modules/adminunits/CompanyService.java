package com.orbix.api.modules.adminunits;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface CompanyService {
	List<CompanyResponseDTO> getAllCompanies(HttpServletRequest request);	
	CompanyResponseDTO get(Long id, HttpServletRequest request);
	CompanyResponseDTO createCompany(CompanyRequestDTO company, HttpServletRequest request);
	CompanyResponseDTO updateCompany(CompanyRequestDTO company, HttpServletRequest request);
}
