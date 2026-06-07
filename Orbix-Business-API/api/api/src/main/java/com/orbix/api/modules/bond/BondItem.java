package com.orbix.api.modules.bond;

import java.time.LocalDateTime;
import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.ParkingZone;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipment;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipmentType;
import com.orbix.api.modules.adminunits.Branch;
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
@Table(name = "bond_items")
public class BondItem {
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
	
	private String comments;
	
	@Column(nullable = false)
	private String bondItemName;
	private String bondItemDescription;
	
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
	private boolean deviceStatus = true;
	private Byte[] image;
	private String cardNo;
	

	private String bondItemCategory;
	private String bondItemColor;
	
	private double weight; // In kg
	private double length; // In cm
	private double width; // In cm
	private double height; // In cm
		
	private LocalDateTime startBillingAt = LocalDateTime.now();
	
//	@Enumerated(EnumType.STRING)
//    @Column(nullable = false)
	String status = "PENDING";
	
	/**Billing*/
	private String billingType;
	private double billingAmount;
	
	@Column(name = "currency")
    private java.util.Currency currency;
	
	private double initialQty = 1;
	private double currentQty = 1;
	
	@ManyToOne(targetEntity = BondZone.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "bond_zone_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private BondZone bondZone;
	
	@ManyToOne(targetEntity = BondItemType.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "bond_item_type_id", nullable = false , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private BondItemType bondItemType;
		
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
