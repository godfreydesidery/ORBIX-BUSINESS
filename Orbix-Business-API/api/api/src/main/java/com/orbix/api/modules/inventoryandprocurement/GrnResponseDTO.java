package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import lombok.Data;

@Data
public class GrnResponseDTO {
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
	private String lpoId;
	private String lpoNo;
	
	List<GrnDetailResponseDTO> grnDetails;
}
