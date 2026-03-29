package com.orbix.api.modules.bond;

import lombok.Data;

@Data
public class BondItemTypeRequestDTO {
	private Long id;
	private String code;
	private String name;

	private Long companyId;
	private String companyName;
	private java.util.Currency currency;
	
	double dailyPrice;
	double hourlyPrice;
}
