package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "parkings")

public class Parking {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true, nullable = false)
	private String no;
	/** Owner information*/
	@Column(nullable = false)
	private String ownerFirstName;
	private String ownerMiddleName;
	@Column(nullable = false)
	private String ownerLastName;
	private String ownerCompanyName;
	private String ownerIdNo;
	private String ownerIdType;
	private String ownerPhoneNo;
	private String ownerEmail;
	private String ownerAddress;
	/**Agent Information*/
	private String agentName;
	private String agentAddress;
	private String agentPhoneNo;
	private String agentEmail;
	private String tformNumber;
	
	/**Vehicle or Equipment Information*/  ///Attention, change the boolean values to boolean instead of string, for database performance issues
	private String registrationNo;
	private String chasisNo;
	private boolean leftFrontLamp = true;
	private boolean rightFrontLamp = true;
	private boolean leftRearLamp = true;
	private boolean rightRearLamp = true;
	private boolean leftSideMirror = true;
	private boolean rightSideMirror = true;
	private boolean leftWiper = true;
	private boolean rightWiper = true;
	private boolean backWiper = true;
	private boolean fuelCap = true;
	private boolean spareTire = true;
	private boolean battery = true;
	private boolean starter = true;
	private boolean aerial = true;
	private boolean wheelCap = true;
	private boolean roundMirror = true;
	private boolean tireIndicator = true;
	private boolean hasKeys = true;
	private Byte[] image;
	private String cardNo;
	
	private String vehicleEquipmentCategory;
	private String vehicleEquipmentName;
	private String vehicleEquipmentColor;
	
	private LocalDateTime startBillingAt = LocalDateTime.now();
	
	@NotBlank
	private String status = "PENDING";
	
	/**Billing*/
	private String billingType;
	private double billingAmount;
	
	@ManyToOne(targetEntity = ParkingZone.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "parking_zone_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private ParkingZone parkingZone;
	
	@ManyToOne(targetEntity = VehicleEquipmentType.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "vehicle_and_equipment_type_id", nullable = false , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private VehicleEquipmentType vehicleEquipmentType;
	
	@ManyToOne(targetEntity = VehicleEquipment.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "vehicle_equipment_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private VehicleEquipment vehicleEquipment;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
		
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "checked_in_by_user_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User checkedInByUser;
		
	private LocalDateTime checkedInDateTime;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "checked_out_by_user_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User checkedOutByUser;
		
	private LocalDateTime checkedOutDateTime;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "canceled_by_user_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User canceledByUser;
		
	private LocalDateTime canceledDateTime;
	
	@ManyToOne(targetEntity = Branch.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "branch_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Branch branch;
}
