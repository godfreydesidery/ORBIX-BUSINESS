package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class ShopSalesOrderDetailRequestDTO {
	Long id;
	double costPriceVatIncl;
	double sellingPriceVatIncl;
	double vatRate;
	double qty;
	double discount;
    Long productId;
    Long shopSalesOrderId;
}
