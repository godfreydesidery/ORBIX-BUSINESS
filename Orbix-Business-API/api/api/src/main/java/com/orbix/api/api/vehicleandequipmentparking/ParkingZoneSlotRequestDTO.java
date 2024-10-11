package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingZoneSlotRequestDTO {
	Long id;
	String no;
	String status;
	boolean active;
	
	Long parkingZoneId;	
}
