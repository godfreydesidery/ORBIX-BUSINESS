package com.orbix.api.modules.weighbridge;

import lombok.Data;

@Data
public class WeighRequestDTO {
	Long id;

	String no;
	/** Owner information */

	String regNo;
	String refNo;

	String ownerFirstName;

	String ownerMiddleName;

	String ownerLastName;
	String ownerName;
	String ownerCompanyName;
	String ownerIdNo;
	String ownerIdType;
	String ownerPhoneNo;
	String ownerEmail;
	String ownerAddress;
	
	String recheck;
	
	String weighStatus;

	String comments;

	String status = "PENDING";

	String description;
}
