package com.orbix.api.modules.bond;

import java.util.List;


import lombok.Data;

@Data
public class BondItemResponseDTO {
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
	
	String comments;
	
	String status;

    String bondZoneId;
    String bondZoneName;
    
    String bondItemName;
    String bondItemDescription;
	
    String bondItemTypeId;	
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
    
    String bondItemTypeName;
    
    String billingType;
    String billingAmount;
    String billingStartAt;
    
    String width;
    String length;
    String height;
    String weight;
    String initialQty;
    String currentQty;
	
    List<ServiceBillItem> serviceBillItems;
    ///////////////////

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
	
	String bondItemCategory;


    
	String bondItemColor;

    

    
    String payStatus;
    String paidAmount;
    
    ///////////////////
}
