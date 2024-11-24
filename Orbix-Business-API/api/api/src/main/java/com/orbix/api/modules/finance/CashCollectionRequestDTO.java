package com.orbix.api.modules.finance;

import lombok.Data;

@Data
public class CashCollectionRequestDTO {	
	double amount = 0;
	String payCode;	
}
