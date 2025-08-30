package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface ServiceService {
	List<ServiceResponseDTO> getAllServicels(HttpServletRequest request);
	ServiceResponseDTO get(Long id, HttpServletRequest request);
	ServiceResponseDTO getCompanyService(Long serviceId, HttpServletRequest request);
	ServiceResponseDTO createService(ServiceRequestDTO service, HttpServletRequest request);
	ServiceResponseDTO updateService(ServiceRequestDTO service, HttpServletRequest request);
	ApiCustomResponse activateService(ServiceRequestDTO service, HttpServletRequest request);
	ApiCustomResponse deactivateService(ServiceRequestDTO service, HttpServletRequest request);
	
	List<ServiceResponseDTO> getServicesByCompany(String serviceName, HttpServletRequest request);
	List<ServiceResponseDTO> getCompanyServices(HttpServletRequest request);
	
	List<ServiceResponseDTO> getServicesByCompanyAndName(String serviceName, HttpServletRequest request);
}
