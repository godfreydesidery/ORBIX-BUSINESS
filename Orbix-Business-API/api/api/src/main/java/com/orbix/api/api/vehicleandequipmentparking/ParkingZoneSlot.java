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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.adminunits.Currency;
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
@Table(name = "parking_zone_slots", uniqueConstraints = { @UniqueConstraint(columnNames = {"no", "parking_zone_id"})})

public class ParkingZoneSlot {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String no;
	private String status;
	private boolean active = false;
	
	@ManyToOne(targetEntity = ParkingZone.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "parking_zone_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private ParkingZone parkingZone;
		
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
		
	private LocalDateTime createdDateTime = LocalDateTime.now();
}
