package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

/** A device attached to a vehicle, in requests and responses */
@Data
public class ParkingDeviceDTO {
	String name;
	Integer qty;
}
