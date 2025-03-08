package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceJobCardIssueResponseDTO {
	String id;
	String no;
	String name;
	String description;
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
    
    String maintenanceNo;
    String regNo;
    
    String maintenanceIssueTypeId;
    String maintenanceIssueTypeName;
    
    String comments;
    
    String equipmentReference;
    
    String ownerName;
    String chasisNo;
    String phoneNo;
    String keys;
    String payment;
    String payStatus;
}
