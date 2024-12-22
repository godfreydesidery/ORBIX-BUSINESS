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
	
	private String tformNumber;
	
	/**Agent Information*/
	String agentName;
	String agentAddress;
	String agentPhoneNo;
	String agentEmail;
	
	private String registrationNo;
	private String chasisNo;
	private String cardNo;
	private Byte[] image;
	
	private String comments;
	
	private boolean deviceStatus;
		
	private String vehicleEquipmentTypeName;
	
	String vehicleEquipmentName;
	String vehicleEquipmentColor;
	
	private String companyId; //optional
	private String companyName; // optional, to use in validation
	private String branchId;
	private String branchName;
	
	private String parkingZoneName;
}
