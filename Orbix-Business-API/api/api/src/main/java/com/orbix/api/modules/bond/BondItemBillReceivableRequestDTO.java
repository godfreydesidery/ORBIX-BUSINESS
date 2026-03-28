package com.orbix.api.modules.bond;

import lombok.Data;

@Data
public class BondItemBillReceivableRequestDTO {
	Long id;
	String startedAt;
	String endedAt;
	String billingType;
	double qty;
	double price;
	
	double discount;
	
	double amount;
	
	Long bondItemId;
	
	int autoBilling;
}
