package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceJobCardIssueBillReceivableRequestDTO {
	Long id;
	String billingType;
	double qty;
	double price;
	
	double discount;
	
	double amount;
	
	Long maintenanceId;
	
	int autoBilling;
}
