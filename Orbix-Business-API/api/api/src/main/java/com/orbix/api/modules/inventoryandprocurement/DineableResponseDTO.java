package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class DineableResponseDTO {
	private String id;
	private String code;
	private String name;
	private String description;
	private String baseUom;
	private String active;
	private String companyId;
	
	private String imported;
}
