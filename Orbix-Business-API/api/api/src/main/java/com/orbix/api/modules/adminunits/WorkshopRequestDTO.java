package com.orbix.api.modules.adminunits;

import lombok.Data;

@Data
public class WorkshopRequestDTO {
	private Long id;
	private String code;
	private String name;
	private String locationName;
	private boolean active;
	private Long branchId;
	
	private String workshopCategory;
}
