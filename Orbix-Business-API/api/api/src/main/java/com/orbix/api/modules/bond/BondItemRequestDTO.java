package com.orbix.api.modules.bond;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class BondItemRequestDTO {
	Long id;
	
	String no;
	/** Owner information */

	@NotBlank(message = "First name cannot be empty")
	String ownerFirstName;
	
	String ownerMiddleName;

	@NotBlank(message = "Last name cannot be empty")
	String ownerLastName;
	String ownerCompanyName;
	String ownerIdNo;
	String ownerIdType;
	String ownerPhoneNo;
	String ownerEmail;
	String ownerAddress;
	
	String comments;
		
	String status = "PENDING";

    Long bondZoneId;
    String bondZoneName;
	
    Long bondItemTypeId;	
    Long branchId;
    Long companyId;
    
    String bondItemTypeName;
    @NotBlank(message = "Good name cannot be empty")
    String bondItemName;
    String bondItemDescription;
	String bondItemColor;
        
    String billingType;
    double billingAmount;
    
    String startBillingAt;
    
    double width;
    double length;
    double height;
    double weight;
    double initialQty;
    ////////////////

	/**Agent Information*/
	String agentName;
	String agentAddress;
	String agentPhoneNo;
	String agentEmail;
	String tformNumber;

	/**Vehicle or Equipment Information*/
	String registrationNo;
	String chasisNo;
	boolean leftFrontLamp;
	boolean rightFrontLamp;
	boolean leftRearLamp;
	boolean rightRearLamp;
	boolean leftSideMirror;
	boolean rightSideMirror;
	boolean leftWiper;
	boolean rightWiper;
	boolean backWiper;
	boolean fuelCap;
	boolean spareTire;
	boolean battery;
	boolean starter;
	boolean aerial;
	boolean wheelCap;
	boolean roundMirror;
	boolean tireIndicator;
	boolean hasKeys;
	boolean deviceStatus;
	Byte[] image;
	
	String cardNo;
	
	String bondItemCategory;
	
    Long parkingZoneId;
    String parkingZoneName;
	

    
    Long bondItemId;
    

   
    
    ///////////////
}
