package com.orbix.api.modules.bond;

import lombok.Data;

@Data
public class BondZoneResponseDTO {
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
