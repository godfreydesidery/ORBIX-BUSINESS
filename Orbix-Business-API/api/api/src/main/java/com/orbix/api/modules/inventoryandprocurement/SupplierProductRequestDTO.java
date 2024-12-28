package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class SupplierProductRequestDTO {
	
	private Long id;
	private double vatRate;
	private double costPriceVatIncl;
	private double maxSupplyQty;
	private boolean active;
	private String termsOfSupply;
	
	private Long supplierId;
	private Long productId;
	private Long branchId;
}
