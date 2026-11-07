package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class ParkingTripRequestDTO {
	Long parkingId;

	/** Send out */
	String reason;
	String reasonDetails;
	String driverName;
	String driverPhoneNo;
	String driverIdType;
	String driverIdNo;
	boolean documentsLeft;
	/** yyyy-MM-ddTHH:mm, as given by a datetime-local input */
	String expectedReturnAt;

	/** Send in */
	boolean documentsReturned;
	String returnNotes;
}
