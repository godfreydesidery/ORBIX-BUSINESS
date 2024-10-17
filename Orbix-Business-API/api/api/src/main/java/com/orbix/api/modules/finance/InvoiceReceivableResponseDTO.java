package com.orbix.api.modules.finance;

import lombok.Data;

@Data
public class InvoiceReceivableResponseDTO {

	String id;
	private String no;
	String status;
	
	String summary;
		
	String createdBy;
	String createdDateTime;
	
	String branchId;
	String branchName;
	String companyId;
	String companyName;
	
	//For vehicle parking
	String ownerName;
	String ownerPhoneNo;
	String chasisNo;
	String cardNo;
	String model;
	
}
