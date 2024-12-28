package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class SupplierResponseDTO {
	private String id;
	private String code;
	private String name; 	
	private String contactName;
	private String address;
	private String phoneNo;
	private String active;
	private String companyId;
	private String companyName;
}
