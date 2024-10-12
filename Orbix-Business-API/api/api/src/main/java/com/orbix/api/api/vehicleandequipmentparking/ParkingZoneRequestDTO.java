package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingZoneRequestDTO {
	Long id;
	String code;
	String name;
	int noOfSlots;
	
	Long branchId;
	Long companyId;
}
