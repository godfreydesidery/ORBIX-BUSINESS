package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceRequestDTO {
	Long id;
	
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
	
	String comments;
	
	String vehicleEquipmentCategory;
	
	String status = "PENDING";
	
    Long vehicleEquipmentTypeId;
    Long branchId;
    Long companyId;
    
    String vehicleEquipmentTypeName;
    String vehicleEquipmentName;
	String vehicleEquipmentColor;
    
    Long vehicleEquipmentId;
    
    String billingType;
    double billingAmount;
}
