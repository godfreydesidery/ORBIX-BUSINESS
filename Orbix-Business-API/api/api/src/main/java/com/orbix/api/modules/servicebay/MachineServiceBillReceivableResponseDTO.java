package com.orbix.api.modules.servicebay;

import lombok.Data;

@Data
public class MachineServiceBillReceivableResponseDTO {
	String id;
	String description;
	String qty;
	String price;
	
	String createdBy;
	
	String amount;
	
	String machineServiceId;

	String payStatus;
	
	String recheck;
}
