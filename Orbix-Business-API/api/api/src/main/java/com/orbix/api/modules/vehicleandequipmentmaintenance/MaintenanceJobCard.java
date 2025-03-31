package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipment;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipmentType;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.inventoryandprocurement.LpoDetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "maintenance_job_cards")
public class MaintenanceJobCard {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true, nullable = false)
	private String no;
	
	@NotBlank
	private String status = "PENDING";
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "opened_by_user_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User openedByUser;
	private LocalDateTime openedDateTime;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "closed_by_user_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User closedByUser;
	private LocalDateTime closedDateTime;
	
	@ManyToOne(targetEntity = Maintenance.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "maintenance_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Maintenance maintenance;
	
	
	@OneToMany(targetEntity = MaintenanceJobCardIssue.class, mappedBy = "maintenanceJobCard", fetch = FetchType.EAGER, orphanRemoval = true)
    @Valid
    @JsonIgnoreProperties("maintenanceJobCard")
	@Fetch(FetchMode.SUBSELECT)
    private List<MaintenanceJobCardIssue> maintenanceJobCardIssues;
}
