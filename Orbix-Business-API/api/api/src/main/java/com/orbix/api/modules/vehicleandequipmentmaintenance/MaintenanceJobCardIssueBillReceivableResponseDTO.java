package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceJobCardIssueBillReceivableResponseDTO {
	String id;
	String description;
	String startedAt;
	String endedAt;
	String billingType;
	String qty;
	String price;
	
	String discount;
	
	String amount;
	
	String maintenanceId;
	
	String payStatus;
}
