package com.orbix.api.modules.bond;

import lombok.Data;

@Data
public class BondItemBillReceivableResponseDTO {
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
	
	String bondItemId;
	
	String payStatus;
	
	String discountApprovedBy;
	String discountApprovedDateTime;
}
