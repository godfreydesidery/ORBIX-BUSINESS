package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class UomRequestDTO {
	private Long id;
	private String code;
	private String name;
	private String type;
	private boolean active;
	private Long companyId;
}
