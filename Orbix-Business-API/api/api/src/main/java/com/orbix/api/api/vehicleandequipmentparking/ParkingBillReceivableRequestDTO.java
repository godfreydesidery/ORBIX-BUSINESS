package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ParkingBillReceivableRequestDTO {

	Long id;
	String startedAt;
	String endedAt;
	String billingType;
	double qty;
	double price;
	
	double discount;
	
	double amount;
	
	Long parkingId;
	
	int autoBilling;
	
}
