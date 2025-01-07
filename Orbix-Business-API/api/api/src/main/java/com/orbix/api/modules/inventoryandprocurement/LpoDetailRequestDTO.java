package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class LpoDetailRequestDTO {
	private Long id;
	private double costPriceVatIncl;
	private double vatRate;
	private double qty;
	
	private Long lpoId;
	private Long productId;	
}
