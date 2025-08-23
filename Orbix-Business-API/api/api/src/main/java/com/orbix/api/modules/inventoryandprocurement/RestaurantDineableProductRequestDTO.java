package com.orbix.api.modules.inventoryandprocurement;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RestaurantDineableProductRequestDTO {
	private Long id;	
	@NotNull
    private Long restaurantId;
	@NotNull
    private Long dineableId;
	@NotNull
    private Long productId;
	double productQty;
}
