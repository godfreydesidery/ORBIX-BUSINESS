package com.orbix.api.modules.inventoryandprocurement;

import lombok.Data;

@Data
public class RestaurantDineableProductResponseDTO {
	private String id;
	
    private String restaurantId;
    private String restaurantName;
    
    private String dineableId;
    private String dineableCode;
    private String dineableName;
    private String dineableDescription;
    
    private String productId;
    private String productCode;
    private String productName;
    private String productDescription;
    
    private String productQty;
	
    private String created;
    private String createdDateTime;
}
