package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceJobCardIssueResponseDTO {
	String id;
	String no;
	String name;	
	String status;
	String noOfDays;
	String price;
    String createdBy;
    String createdDateTime;
    String openedBy;
    String openedDateTime;
    String closedBy;
    String closedDateTime;
    String serviceSpecialist;
    
    String maintenanceJobCardId;
    
    String maintenanceIssueTypeId;
    String maintenanceIssueTypeName;
    
    String description;
    String comments;
}
