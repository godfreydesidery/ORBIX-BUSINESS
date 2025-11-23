package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class ServiceResponseDTO {
	private String id;
	private String code;
	private String name;
	private String description;
	private String baseUom;
	private String price;
	private String active;
	private String companyId;
}
