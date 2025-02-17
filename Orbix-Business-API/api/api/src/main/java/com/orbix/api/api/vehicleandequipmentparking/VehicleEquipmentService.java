package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface VehicleEquipmentService {
	List<VehicleEquipmentResponseDTO> getAllVehicleEquipments(HttpServletRequest request);	
	List<VehicleEquipmentResponseDTO> getAllActiveVehicleEquipments(HttpServletRequest request);	
	VehicleEquipmentResponseDTO get(Long id, HttpServletRequest request);
	VehicleEquipmentResponseDTO getByChasisNo(String chasis_no, HttpServletRequest request);
	VehicleEquipmentResponseDTO getMaintenanceByChasisNo(String chasis_no, HttpServletRequest request);
	List<String> getChasisNos(HttpServletRequest request);
	VehicleEquipmentResponseDTO createVehicleEquipment(VehicleEquipmentRequestDTO vehicleEquipmentRequest, HttpServletRequest request);
	VehicleEquipmentResponseDTO updateVehicleEquipment(VehicleEquipmentRequestDTO vehicleEquipmentRequest, HttpServletRequest request);
	
//	ApiCustomResponse activateVehicleEquipment(VehicleEquipmentRequestDTO vehicleEquipmentRequest, HttpServletRequest request);
//	ApiCustomResponse deactivateVehicleEquipment(VehicleEquipmentRequestDTO vehicleEquipmentRequest, HttpServletRequest request);
}
