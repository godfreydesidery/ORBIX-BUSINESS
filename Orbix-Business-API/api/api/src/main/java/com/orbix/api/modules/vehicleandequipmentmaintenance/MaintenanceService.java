package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface MaintenanceService {
	List<MaintenanceResponseDTO> getAllMaintenances(HttpServletRequest request);	
	List<MaintenanceResponseDTO> getAllPendingOrCheckedInMaintenances(HttpServletRequest request);	
	List<MaintenanceResponseDTO> getAllCheckedInMaintenancesWithOpenJobs(HttpServletRequest request);	
	List<MaintenanceResponseDTO> getAllCleared(HttpServletRequest request);
	List<MaintenanceResponseDTO> getTodayCheckedOut(HttpServletRequest request);
	List<MaintenanceResponseDTO> getRecentCheckedOut(HttpServletRequest request);
	List<MaintenanceResponseDTO> getAllCheckedInMaintenances(HttpServletRequest request);	
	MaintenanceResponseDTO get(Long id, HttpServletRequest request);
	List<MaintenanceJobCardIssueBillReceivableResponseDTO> getMaintenanceJobCardIssueBillReceivables(Long id, HttpServletRequest request);
	MaintenanceResponseDTO createMaintenance(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request);
	MaintenanceResponseDTO updateMaintenance(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request);
	
	MaintenanceResponseDTO checkIn(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request);
	MaintenanceResponseDTO checkOut(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request);
	
	MaintenanceJobCardResponseDTO createMaintenanceJobCard(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request);
	MaintenanceJobCardResponseDTO openMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard, HttpServletRequest request);
	MaintenanceJobCardResponseDTO closeMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard, HttpServletRequest request);
	MaintenanceJobCardResponseDTO reopenMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard, HttpServletRequest request);
}
