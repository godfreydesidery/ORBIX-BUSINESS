package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingZoneSlotResponseDTO {
	String id;
	String no;
	String status;
	String active;
	
	String branchId;
	
	String createdByUser;
	String createdDateTime;
}
