package com.orbix.api.modules.bond;

import lombok.Data;

@Data
public class BondZoneRequestDTO {
	Long id;
	String code;
	String name;
	int noOfSections;
	
	Long branchId;
	Long companyId;
}
