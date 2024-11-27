package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingServiceBillReceivableResponseDTO {
	String id;
	String description;
	String serviceDate;
	String billingType;
	String qty;
	String price;
	
	String discount;
	
	String amount;
	
	String parkingId;
	
	String payStatus;
}
