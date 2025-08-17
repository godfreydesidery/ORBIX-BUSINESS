package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface DineableService {
	List<DineableResponseDTO> getAllDineablees(HttpServletRequest request);
	DineableResponseDTO get(Long id, HttpServletRequest request);
	DineableResponseDTO getCompanyDineable(Long dineableId, HttpServletRequest request);
	DineableResponseDTO createDineable(DineableRequestDTO dineable, HttpServletRequest request);
	DineableResponseDTO updateDineable(DineableRequestDTO dineable, HttpServletRequest request);
	ApiCustomResponse activateDineable(DineableRequestDTO dineable, HttpServletRequest request);
	ApiCustomResponse deactivateDineable(DineableRequestDTO dineable, HttpServletRequest request);
	
	List<DineableResponseDTO> getDineablesByCompany(String dineableName, HttpServletRequest request);
	List<DineableResponseDTO> getCompanyDineables(HttpServletRequest request);
	List<DineableResponseDTO> getCompanySellableDineables(HttpServletRequest request);
	List<DineableResponseDTO> getCompanySellableDineablesByRestaurant(Long restaurantId, HttpServletRequest request);
	
	List<DineableResponseDTO> getDineablesByCompanyAndName(String dineableName, HttpServletRequest request);
}
