package com.orbix.api.modules.salesandmarketing;

import com.orbix.api.modules.inventoryandprocurement.Dineable;

import lombok.Data;

@Data
public class RestaurantSaleDetailRequestDTO {
	Long id;	
	double costPriceVatIncl;
	double sellingPriceVatIncl;
	double vatRate;
	double qty;
	double discount;
    Long restaurantSaleId;
    Long dineableId;
    Dineable dineable;
}
