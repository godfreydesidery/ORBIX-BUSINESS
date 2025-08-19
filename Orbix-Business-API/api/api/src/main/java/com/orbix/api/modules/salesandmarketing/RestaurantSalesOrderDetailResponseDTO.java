package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class RestaurantSalesOrderDetailResponseDTO {
	String id;
	String dineableId;
	String dineableCode;
	String dineableName;
	String dineableDescription;
	String costPriceVatIncl;
	String sellingPriceVatIncl;
	String vatRate;
	String qty;
	String discount;
	String baseUom;
	String amount;
	//
	String billId;
	String billStatus;
	String billDescription;
}
