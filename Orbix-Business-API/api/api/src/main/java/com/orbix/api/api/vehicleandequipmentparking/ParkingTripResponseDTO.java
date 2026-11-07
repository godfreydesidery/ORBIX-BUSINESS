package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingTripResponseDTO {
	String id;
	int sn;
	String no;

	String parkingId;
	String parkingNo;
	String chasisNo;
	String registrationNo;
	String vehicleEquipmentTypeName;
	String vehicleEquipmentColor;
	String cardNo;
	String ownerName;
	String ownerPhoneNo;

	String reason;
	String reasonDetails;
	String driverName;
	String driverPhoneNo;
	String driverIdType;
	String driverIdNo;
	boolean documentsLeft;
	String expectedReturnAt;

	String status;
	/** Still out after the expected return time */
	boolean overdue;

	String sentOutAt;
	String sentOutBy;
	String receivedAt;
	String receivedBy;
	boolean documentsReturned;
	String returnNotes;

	/** Minutes away: until received back, or until now while still out */
	long minutesOut;
}
