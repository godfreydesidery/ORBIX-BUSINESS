package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceIssueTypeResponseDTO {
	String id;
	String code;
	String name;	
	String active;
    String companyId;
    String companyName;
    String createdBy;	
	String createdDateTime;
}
