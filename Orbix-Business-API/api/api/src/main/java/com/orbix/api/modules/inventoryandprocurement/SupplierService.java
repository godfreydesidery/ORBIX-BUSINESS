package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface SupplierService {
	List<SupplierResponseDTO> getAllSuppliers(HttpServletRequest request);
	List<SupplierResponseDTO> getAllCompanySuppliers(HttpServletRequest request);
	SupplierResponseDTO get(Long id, HttpServletRequest request);
	SupplierResponseDTO createSupplier(SupplierRequestDTO supplier, HttpServletRequest request);
	SupplierResponseDTO updateSupplier(SupplierRequestDTO supplier, HttpServletRequest request);
	ApiCustomResponse activateSupplier(SupplierRequestDTO supplier, HttpServletRequest request);
	ApiCustomResponse deactivateSupplier(SupplierRequestDTO supplier, HttpServletRequest request);
	
	List<SupplierResponseDTO> getSuppliersByCompany(String supplierName, HttpServletRequest request);
	
}
