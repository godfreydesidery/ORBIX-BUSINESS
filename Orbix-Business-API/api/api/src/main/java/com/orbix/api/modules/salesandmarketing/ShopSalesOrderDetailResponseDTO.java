package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class ShopSalesOrderDetailResponseDTO {
	String id;
	String productId;
	String productCode;
	String productName;
	String productDescription;
	String costPriceVatIncl;
	String sellingPriceVatIncl;
	String vatRate;
	String qty;
	String discount;
	String baseUom;
	String amount;
}
