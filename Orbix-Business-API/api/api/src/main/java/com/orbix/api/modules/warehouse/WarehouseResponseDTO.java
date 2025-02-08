package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class WarehouseResponseDTO {
	String id;
	String code;
	String name;
	String noOfSections;
	String active;
	
	String companyId;
	String companyName;
	String branchId;
	String branchName;

	String createdByUser;
	String createdDateTime;
}
