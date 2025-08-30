package com.orbix.api.modules.adminunits;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface WorkshopService {
	List<WorkshopResponseDTO> getAllWorkshops(HttpServletRequest request);
	WorkshopResponseDTO get(Long id, HttpServletRequest request);
	WorkshopResponseDTO createWorkshop(WorkshopRequestDTO workshop, HttpServletRequest request);
	WorkshopResponseDTO updateWorkshop(WorkshopRequestDTO workshop, HttpServletRequest request);
	ApiCustomResponse activateWorkshop(WorkshopRequestDTO workshop, HttpServletRequest request);
	ApiCustomResponse deactivateWorkshop(WorkshopRequestDTO workshop, HttpServletRequest request);
	
	
	List<WorkshopResponseDTO> getBranchAvailableWorkshopsByUser(HttpServletRequest request);
	WorkshopResponseDTO getSelectedWorkshop(Long id, HttpServletRequest request);
	
	List<WorkshopResponseDTO> getBranchWorkshops(HttpServletRequest request);
}
