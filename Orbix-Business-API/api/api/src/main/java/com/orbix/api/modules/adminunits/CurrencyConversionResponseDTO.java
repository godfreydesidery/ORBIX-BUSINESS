package com.orbix.api.modules.adminunits;

import lombok.Data;

@Data
public class CurrencyConversionResponseDTO {
	private String id;
	String sourceCurrencyCode;
	String sourceCurrencyValue;
	String finalCurrencyCode;
	String finalCurrencyValue;
	String active;
}
