package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class StorageGoodReleaseResponseDTO {
	String id;
	String no;
	String qty;
	String status;
	String storageId;
	String releaseDate;
	String checkedInDate;
	//
	String clientName;
	String goodName;
	String unitPrice;
	String total;
	String clientAddress;
	String clientPhoneNo;
}
