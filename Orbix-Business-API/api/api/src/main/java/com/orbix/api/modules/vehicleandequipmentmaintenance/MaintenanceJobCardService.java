package com.orbix.api.modules.vehicleandequipmentmaintenance;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.modules.identityandaccess.User;

public interface MaintenanceJobCardService {
	
	MaintenanceJobCardResponseDTO showMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard, User filterByUser);
	MaintenanceJobCardResponseDTO createMaintenanceJobCard(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request);
	MaintenanceJobCardResponseDTO openMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard, HttpServletRequest request);
	MaintenanceJobCardResponseDTO closeMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard, HttpServletRequest request);
	MaintenanceJobCardResponseDTO reopenMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard, HttpServletRequest request);

}
