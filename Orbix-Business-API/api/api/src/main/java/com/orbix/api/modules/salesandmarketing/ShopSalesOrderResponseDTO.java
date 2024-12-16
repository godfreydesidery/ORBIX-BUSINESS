package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import lombok.Data;

@Data
public class ShopSalesOrderResponseDTO {
	String id;
	String no;	
	String customerName;
	String summary;
	String status;	
    String shopId;
    String createdBy;
    String  createdAt;
    
    List<ShopSalesOrderDetailResponseDTO> shopSalesOrderDetails;
}
