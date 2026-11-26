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
	/** How many tracking devices are attached; empty when none or not recorded */
	String deviceCount;
	String keyStatus;
	Byte[] image;
	
	String comments;
	
	
	
	String vehicleEquipmentCategory;

	String status;
	/** INSIDE the yard, or OUTSIDE on a temporary trip */
	String custody;

    String parkingZoneId;
    String parkingZoneName;
    /** The slot within the zone; empty when the zone has no slots or none was recorded */
    String parkingSlotNo;
    /** The registered vehicle, so its inspection can be opened again */
    String vehicleEquipmentId;
    
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
    
    String payStatus;
    String paidAmount;
    
    List<ServiceBillItem> serviceBillItems;

    /**Inspection and driver; the signatures come from /parkings/get_inspection*/
    String interiorDetails;
    String bodyCondition;
    String driverName;
    String driverPhoneNo;
    String driverIdType;
    String driverIdNo;
}
