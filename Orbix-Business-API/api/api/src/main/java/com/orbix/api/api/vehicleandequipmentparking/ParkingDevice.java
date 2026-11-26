package com.orbix.api.api.vehicleandequipmentparking;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A device found attached to a vehicle at inspection (radio, jack, spare tyre...) and how many of it.
 * Every column is nullable: required values are checked by the inspection, not by the database.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parking_devices", indexes = {
		@Index(name = "ix_parking_devices_parking", columnList = "parking_id")
})
public class ParkingDevice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(targetEntity = Parking.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "parking_id", nullable = true)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Parking parking;

	private String name;

	private Integer qty;
}
