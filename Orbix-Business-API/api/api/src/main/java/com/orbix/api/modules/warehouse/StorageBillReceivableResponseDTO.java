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
	String noOfDays;
	String price;
	
	String discount;
	String discountStatus;
	
	String amount;
	
	String storageId;
	
	String payStatus;
	
	String discountApprovedBy;
	String discountApprovedDateTime;
}
