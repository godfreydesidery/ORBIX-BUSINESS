package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class WarehouseRequestDTO {
	Long id;
	String code;
	String name;
	int noOfSections;
	
	Long branchId;
	Long companyId;
}
