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
@Table(name = "vehicle_equipments")
public class VehicleEquipment {
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
	
	/**Vehicle or Equipment Information*/  ///Attention, change the boolean values to boolean instead of string, for database performance issues
	private String registrationNo;
	private String chasisNo;
	private String cardNo;
	private Byte[] image;
	
	private String vehicleEquipmentName;
	private String vehicleEquipmentColor;
	
	/**Agent Information*/
	private String agentName;
	private String agentAddress;
	private String agentPhoneNo;
	private String agentEmail;
	private String tformNumber;
	
	private boolean deviceStatus = true;
	
	
	private boolean active = true;

	@ManyToOne(targetEntity = VehicleEquipmentType.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "vehicle_equipment_type_id", nullable = false , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private VehicleEquipmentType vehicleEquipmentType;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
		
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = Branch.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "branch_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Branch branch;

}
