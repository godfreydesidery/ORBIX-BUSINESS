package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class RestaurantProductResponseDTO {
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
    private String productDescription;
    private String baseUom;
    private String restaurantId;
    private String restaurantName;
    
    private String created;
    private String createdDateTime;
}
