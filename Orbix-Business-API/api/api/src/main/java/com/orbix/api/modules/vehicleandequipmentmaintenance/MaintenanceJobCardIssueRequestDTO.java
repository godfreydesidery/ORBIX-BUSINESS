package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceJobCardIssueRequestDTO {
	Long id;
	String no;
	String name;	
	String status;
	int noOfDays;
	double price;    
    String serviceSpecialistUserId;
    String serviceSpecialistUserNickname;
    
    Long maintenanceIssueTypeId;
    String maintenanceIssueTypeName;
    
    String description;
    String comments;
    
}
