package com.orbix.api.modules.inventoryandprocurement;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ShopProductResponseDTO {
	private String id;
	private String currentStock;
    private String minStock;
    private String maxStock;
    private String defaultReorderQty;
    private String defaultReorderLevel;   
    private String vatRate; // Fractional rate, e.g., 0.18 for 18%
    private String vatPercentage;
    private String costPriceVatIncl;
    private String costPriceVatExcl;
    private String sellingPriceVatIncl;
    private String sellingPriceVatExcl;
    private String active;
    // References
    private String productId;
    private String productCode;
    private String productName;
    private String shopId;
    private String shopName;
    
    private String created;
    private String createdDateTime;
}
