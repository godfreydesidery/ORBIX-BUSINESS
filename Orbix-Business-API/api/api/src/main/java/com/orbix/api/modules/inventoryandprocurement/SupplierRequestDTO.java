package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class SupplierRequestDTO {
	private Long id;
	private String code;
	private String name; 	
	private String contactName;
	private String address;
	private String phoneNo;
	private boolean active = false;
	private Long companyId;
}
