package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;

import lombok.Data;

@Data
public class MaintenanceJobCardResponseDTO {
	String id;
	String no;
	String status;
    String createdBy;
    String createdDateTime;
    String openedBy;
    String openedDateTime;
    String closedBy;
    String closedDateTime;
    String maintenanceId;
    String maintenanceNo;
    String ownerName;
    String vehicleEquipmentName;
    String vehicleEquipmentTypeName;
    
    List<MaintenanceJobCardIssueResponseDTO> maintenanceJobCardIssues;
}
