package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class VehicleEquipmentTypeRequestDTO {
	private Long id;
	private String code;
	private String name;

	private Long companyId;
	private String companyName;
	
	double dailyPrice;
}
