package com.orbix.api.modules.salesandmarketing;

import com.orbix.api.modules.inventoryandprocurement.Product;

import lombok.Data;

@Data
public class SaleDetailRequestDTO {
	Long id;	
	double costPriceVatIncl;
	double sellingPriceVatIncl;
	double vatRate;
	double qty;
    Long saleId;
    Long productId;
    Product product;
}
