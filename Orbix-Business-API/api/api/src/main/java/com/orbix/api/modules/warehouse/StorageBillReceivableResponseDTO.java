package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class StorageBillReceivableResponseDTO {
	String id;
	String description;
	String startedAt;
	String endedAt;
	String billingType;
	String qty;
	String price;
	
	String discount;
	
	String amount;
	
	String storageId;
	
	String payStatus;
}
