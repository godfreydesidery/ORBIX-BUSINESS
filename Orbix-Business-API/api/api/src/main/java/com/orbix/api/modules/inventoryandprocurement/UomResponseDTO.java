package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class UomResponseDTO {
	private String id;
	private String code;
	private String name;
	private String type;
	private String active;
	private String companyId;
}
