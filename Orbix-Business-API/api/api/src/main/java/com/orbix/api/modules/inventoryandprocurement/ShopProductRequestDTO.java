package com.orbix.api.modules.inventoryandprocurement;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ShopProductRequestDTO {
	private Long id;
    private double currentStock;
    private double minStock;
    private double maxStock;
    private double defaultReorderQty;
    private double defaultReorderLevel;   
    private double vatRate; // Fractional rate, e.g., 0.18 for 18%
    private double costPriceVatIncl;
    private double costPriceVatExcl;
    private double sellingPriceVatIncl;
    private double sellingPriceVatExcl;
    private boolean active;
    // References
    @NotNull
    private Long productId;

    @NotNull
    private Long shopId;
}
