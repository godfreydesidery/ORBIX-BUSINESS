package com.orbix.api.modules.adminunits;

import lombok.Data;

@Data
public class WorkshopResponseDTO {
	private String id;
	private String code;
	private String name;	
	private String locationName;
	private String active;
	private String branchId;
	private String created;
	
	private String otherInfo;
	
	private String workshopCategory;
}
