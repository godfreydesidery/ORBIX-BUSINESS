package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class VehicleEquipmentTypeResponseDTO {
	private String id;
	private String code;
	private String name;
	
	private String active;
	
	private String companyId;
	private String companyName;
	
	private String dailyPrice;
		
}
