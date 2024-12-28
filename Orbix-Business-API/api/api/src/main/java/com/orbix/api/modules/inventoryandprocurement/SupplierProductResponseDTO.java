package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class SupplierProductResponseDTO {
	private String id;
	private String vatRate;
	private String costPriceVatIncl;
	private String maxSupplyQty;
	private String active;
	private String termsOfSupply;
	
	private String supplierId;
	private String supplierCode;
	private String supplierName;
	private String productId;
	private String productCode;
	private String productName;
	private String productDescription;
	
	private String branchId;
	
	private String created;
    private String createdDateTime;
}
