package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;
@Data
public class VehicleEquipmentRequestDTO {
	private Long id;	
	private String no;
	private String ownerFirstName;
	private String ownerMiddleName;
	private String ownerLastName;
	private String ownerCompanyName;
	private String ownerIdNo;
	private String ownerIdType;
	private String ownerPhoneNo;
	private String ownerEmail;
	private String ownerAddress;
	
	private String registrationNo;
	private String chasisNo;
	private String cardNo;
	private Byte[] image;
		
	private String vehicleEquipmentTypeName;
	
	private String companyId;
	private String companyName;
	private String branchId;
	private String branchName;
	
	private String parkingZoneName;
}
