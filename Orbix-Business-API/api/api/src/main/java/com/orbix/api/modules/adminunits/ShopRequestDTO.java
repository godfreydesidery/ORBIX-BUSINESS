package com.orbix.api.modules.adminunits;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ShopRequestDTO {
	private Long id;
	private String code;
	private String name;
	private String locationName;
	private boolean active;
	private Long branchId;
}
