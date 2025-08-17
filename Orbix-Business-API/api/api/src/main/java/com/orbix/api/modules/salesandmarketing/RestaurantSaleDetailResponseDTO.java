package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class RestaurantSaleDetailResponseDTO {
	String id;	
	String costPriceVatIncl;
	String sellingPriceVatIncl;
	String vatRate;
	String qty;
	String discount;
	String restaurantSaleId;
	String dineableId;
	String dineableCode;
	String dineableName;
	String dineableDescription;
}
