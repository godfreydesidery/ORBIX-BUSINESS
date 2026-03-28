package com.orbix.api.modules.bond;

import lombok.Data;

@Data
public class BondItemTypeResponseDTO {
	private String id;
	private String code;
	private String name;
	
	private String active;
	
	private String companyId;
	private String companyName;
	
	private String dailyPrice;
	private String hourlyPrice;
}
