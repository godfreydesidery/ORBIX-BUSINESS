package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ParkingServiceBillReceivableRequestDTO {
	Long id;

	String description;
	String serviceDate;
	String billingType;
	double qty;
	double price;
	
	double discount;
	
	double amount;
	
	Long parkingId;
	
	int autoBilling;
}
