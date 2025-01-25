package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class GrnDetailRequestDTO {
	private Long id;
	private double costPriceVatIncl;
	private double vatRate;
	private double orderedQty;
	private double receivedQty;
	
	private Long grnId;
	private Long productId;
}
