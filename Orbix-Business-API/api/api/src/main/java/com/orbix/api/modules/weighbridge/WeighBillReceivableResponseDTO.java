package com.orbix.api.modules.weighbridge;

import lombok.Data;

@Data
public class WeighBillReceivableResponseDTO {
	String id;
	String description;
	String qty;
	String price;
	
	String discount;
	
	String createdBy;
	
	String amount;
	
	String weighId;

	String payStatus;
	
	String weightOne;
	String weightTwo;
	String weightThree;
	String weightFour;
	String weighStatus;
	
	String recheck;
}
