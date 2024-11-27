package com.orbix.api.modules.finance;

import lombok.Data;

@Data
public class CollectionRequestDTO {	
	double amount = 0;
	String payCode;	
}
