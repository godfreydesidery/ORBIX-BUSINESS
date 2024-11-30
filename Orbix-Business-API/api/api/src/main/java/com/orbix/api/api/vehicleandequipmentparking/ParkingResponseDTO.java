package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import lombok.Data;

@Data
public class ParkingResponseDTO {
	String id;
	
	String sn;
	
	String no;
	/** Owner information */

	String ownerFirstName;
	String ownerMiddleName;

	String ownerLastName;
	String ownerCompanyName;
	String ownerIdNo;
	String ownerIdType;
	String ownerPhoneNo;
	String ownerEmail;
	String ownerAddress;
	/**Agent Information*/
	String agentName;
	String agentAddress;
	String agentPhoneNo;
	String agentEmail;
	String tformNumber;

	/**Vehicle or Equipment Information*/
	String registrationNo;
	String chasisNo;
	String cardNo;
	String leftFrontLamp;
	String rightFrontLamp;
	String leftRearLamp;
	String rightRearLamp;
	String leftSideMirror;
	String rightSideMirror;
	String leftWiper;
	String rightWiper;
	String backWiper;
	String fuelCap;
	String spareTire;
	String battery;
	String starter;
	String aerial;
	String wheelCap;
	String roundMirror;
	String tireIndicator;
	String hasKeys;
	String deviceStatus;
	String keyStatus;
	Byte[] image;
	
	String comments;
	
	
	
	String vehicleEquipmentCategory;

	String status;

    String parkingZoneId;
    String parkingZoneName;
    
    String vehicleEquipmentName;
	String vehicleEquipmentColor;
	
    String vehicleAndEquipmentTypeId;	
    String branchId;
    String companyId;
    
    String createdAt;
    String createdBy;
    String checkedInAt;
    String checkedInBy;
    String checkedOutAt;
    String checkedOutBy;
    String canceledAt;
    String canceledBy;
    
    String vehicleEquipmentTypeName;
    
    String billingType;
    String billingAmount;
    String billingStartAt;
    
    List<ServiceBillItem> serviceBillItems;
}
