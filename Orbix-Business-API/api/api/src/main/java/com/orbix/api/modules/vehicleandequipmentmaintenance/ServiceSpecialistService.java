package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface ServiceSpecialistService {
	List<ServiceSpecialistResponseDTO> getAllServiceSpecialists(HttpServletRequest request);	
	List<ServiceSpecialistResponseDTO> getAllBranchActiveServiceSpecialists(HttpServletRequest request);		
	ServiceSpecialistResponseDTO get(Long id, HttpServletRequest request);
	ServiceSpecialistResponseDTO createServiceSpecialist(ServiceSpecialistRequestDTO serviceSpecialistRequest, HttpServletRequest request);
	ServiceSpecialistResponseDTO updateServiceSpecialist(ServiceSpecialistRequestDTO serviceSpecialistRequest, HttpServletRequest request);
	ApiCustomResponse activateServiceSpecialist(ServiceSpecialistRequestDTO serviceSpecialistRequest, HttpServletRequest request);
	ApiCustomResponse deactivateServiceSpecialist(ServiceSpecialistRequestDTO serviceSpecialistRequest, HttpServletRequest request);
}
