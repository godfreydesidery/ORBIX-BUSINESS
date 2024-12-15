package com.orbix.api.modules.salesandmarketing;

import lombok.Data;

@Data
public class ShopSalesOrderRequestDTO {
	Long id;
	String no;	
	String summary;
	String status;	
    Long shopId;
}
