package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class GrnDetailResponseDTO {
	private String id;
	private String costPriceVatIncl;
	private String vatRate;
	private String orderedQty;
	private String receivedQty;
	
	private String GRNId;
	private String productId;
	private String productCode;
	private String productName;
	private String productDescription;
	
	private String baseUom;
	
	private String amount;
	
	private String createdBy;
	private String createdAt;
}
