package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ShopResponseDTO {
	private String id;
	private String code;
	private String name;	
	private String locationName;
	private String active;
	private String branchId;
	private String created;
	
	private String otherInfo;
	
}
