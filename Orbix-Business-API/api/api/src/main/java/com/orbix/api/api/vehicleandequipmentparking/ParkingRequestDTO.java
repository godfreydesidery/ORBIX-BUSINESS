package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.util.List;

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
	boolean hasKeys;
	boolean deviceStatus;
	Byte[] image;
	
	String cardNo;
	
	String comments;
	
	
	
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

    /**Inspection*/
    String interiorDetails;
    String bodyCondition;
    /** Devices attached; when given, replaces the vehicle's list */
    List<ParkingDeviceDTO> devices;

    /**Driver*/
    String driverName;
    String driverPhoneNo;
    String driverIdType;
    String driverIdNo;

    /**Signatures: base64 PNGs, optionally with their data: prefix*/
    String inspectorSignature;
    String driverSignature;
    /** The name the driver typed beside the signature */
    String driverSignerName;
    String registrarSignature;
    String modifierSignature;
}
