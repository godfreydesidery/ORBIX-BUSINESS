package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingZoneResponseDTO {
	String id;
	String code;
	String name;
	String noOfSlots;
	String active;
	
	String companyId;
	String companyName;
	String branchId;
	String branchName;

	String createdByUser;
	String createdDateTime;
}
