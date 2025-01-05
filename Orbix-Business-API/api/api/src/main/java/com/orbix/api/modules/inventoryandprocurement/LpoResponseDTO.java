package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import com.orbix.api.modules.salesandmarketing.ShopSalesOrderDetailResponseDTO;

import lombok.Data;

@Data
public class LpoResponseDTO {
	private String id;
	private String no;
	private String status;
	
	private String branchId;
	private String branchCode;
	private String shopId;
	private String shopCode;
	private String shopName;
	private String supplierId;
	private String supplierCode;
	private String supplierName;
	
	List<LpoDetailResponseDTO> lpoDetails;
}
