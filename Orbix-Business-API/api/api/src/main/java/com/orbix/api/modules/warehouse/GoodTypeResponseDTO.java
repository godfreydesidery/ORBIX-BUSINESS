package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class GoodTypeResponseDTO {
	private String id;
	private String code;
	private String name;
	
	private String active;
	
	private String companyId;
	private String companyName;
	
	private String dailyPrice;
	private String hourlyPrice;
}
