package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class StorageBillReceivableRequestDTO {
	Long id;
	String startedAt;
	String endedAt;
	String billingType;
	double qty;
	double price;
	
	double discount;
	
	double amount;
	
	Long storageId;
	
	int autoBilling;
}
