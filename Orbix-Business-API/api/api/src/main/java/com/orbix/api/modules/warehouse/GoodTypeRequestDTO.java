package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class GoodTypeRequestDTO {
	private Long id;
	private String code;
	private String name;

	private Long companyId;
	private String companyName;
	
	double dailyPrice;
	double hourlyPrice;
}
