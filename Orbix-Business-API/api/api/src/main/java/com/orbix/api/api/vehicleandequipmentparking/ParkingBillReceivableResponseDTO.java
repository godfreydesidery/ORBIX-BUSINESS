package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingBillReceivableResponseDTO {

	String id;
	String description;
	String startedAt;
	String endedAt;
	String billingType;
	String qty;
	String price;
	
	String discount;
	String discountStatus;
	
	String amount;
	
	String parkingId;
	
	String payStatus;
	
	String discountApprovedBy;
	String discountApprovedDateTime;
}
