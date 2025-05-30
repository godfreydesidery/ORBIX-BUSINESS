package com.orbix.api.modules.finance;

import lombok.Data;

@Data
public class DiscountRequestRequestDTO {
	Long serviceBillId;
	String serviceBillName;
	double billAmount;
	double discountAmount;
}
