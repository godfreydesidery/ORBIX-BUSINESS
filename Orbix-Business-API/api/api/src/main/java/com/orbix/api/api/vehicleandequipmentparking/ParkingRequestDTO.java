package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.orbix.api.modules.identityandaccess.User;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class ParkingRequestDTO {

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
	Byte[] image;
	
	String cardNo;
	
	String hasKeys;
	
	String vehicleEquipmentCategory;
	
	String status = "PENDING";

    Long parkingZoneId;
    String parkingZoneName;
	
    Long vehicleEquipmentTypeId;	
    Long branchId;
    Long companyId;
    
    String vehicleEquipmentTypeName;
    String vehicleEquipmentName;
	String vehicleEquipmentColor;
    
    Long vehicleEquipmentId;
    
    String billingType;
    double billingAmount;
    
    String startBillingAt;
}
