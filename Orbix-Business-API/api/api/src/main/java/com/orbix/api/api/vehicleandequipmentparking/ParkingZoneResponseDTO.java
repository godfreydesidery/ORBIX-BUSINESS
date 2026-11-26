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

	/** Free spaces, in /parking_zones/get_all_branch_available only; empty for a zone without a limit (no of slots 0) */
	String freeSlots;
}
