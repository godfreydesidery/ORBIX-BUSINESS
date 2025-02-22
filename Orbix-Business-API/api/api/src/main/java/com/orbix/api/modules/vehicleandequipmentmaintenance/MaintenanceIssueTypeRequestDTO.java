package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceIssueTypeRequestDTO {
	Long id;
	String code;
	String name;	
	String active;
}
