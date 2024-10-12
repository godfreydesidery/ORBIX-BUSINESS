package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class VehicleAndEquipmentTypeResponseDTO {
	private String id;
	private String code;
	private String name;
	
	private String active;
	
	private String companyId;
	private String companyName;
		
}
