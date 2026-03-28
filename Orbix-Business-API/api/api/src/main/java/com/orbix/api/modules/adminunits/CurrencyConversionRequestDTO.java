package com.orbix.api.modules.adminunits;

import lombok.Data;

@Data
public class CurrencyConversionRequestDTO {
	private Long id;
	java.util.Currency sourceCurrencyCode;
	double sourceCurrencyValue;
	java.util.Currency finalCurrencyCode;
	double finalCurrencyValue;
	private boolean active;
}
