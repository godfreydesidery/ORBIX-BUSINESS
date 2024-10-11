package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingZoneRequestDTO {
	Long id;
	String code;
	String name;
	int noOfSlots;
	boolean active;
	
	Long branchId;
	Long companyId;
}
