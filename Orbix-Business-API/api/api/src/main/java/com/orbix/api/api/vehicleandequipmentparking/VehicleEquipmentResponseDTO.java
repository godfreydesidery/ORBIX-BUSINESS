package com.orbix.api.api.vehicleandequipmentparking;

import lombok.Data;

@Data
public class VehicleEquipmentResponseDTO {
	private String id;	
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
	
	/**Agent Information*/
	String agentName;
	String agentAddress;
	String agentPhoneNo;
	String agentEmail;
	
	String deviceStatus;
	
	private String registrationNo;
	private String chasisNo;
	private String cardNo;
	private Byte[] image;
	
	private String tformNumber;
	
	private String active;
	
	private String vehicleEquipmentTypeName;
	
	String vehicleEquipmentName;
	String vehicleEquipmentColor;

	private String companyId;
	private String companyName;
	private String branchId;
	private String branchName;
	
	private String parkingId;
}
