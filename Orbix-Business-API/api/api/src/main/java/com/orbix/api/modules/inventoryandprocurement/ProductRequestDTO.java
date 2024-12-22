package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class ProductRequestDTO {
	private Long id;
	private String code;
	private String name;
	private String description;
	private String baseUom;
	private boolean active;
	private Long companyId;
}
