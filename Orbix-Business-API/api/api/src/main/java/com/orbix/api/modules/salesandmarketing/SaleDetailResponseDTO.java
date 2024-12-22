package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class SaleDetailResponseDTO {
	String id;	
	String costPriceVatIncl;
	String sellingPriceVatIncl;
	String vatRate;
	String qty;
	String discount;
	String saleId;
	String productId;
	String productCode;
	String productName;
	String productDescription;
}
