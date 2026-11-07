package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;

import javax.persistence.Column;
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

import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * One temporary trip of a checked-in vehicle out of the yard and back (service, testing, loading).
 * Sent out by the cashier with a temporary gate pass, received back by reception.
 * Status: OUT while away, then RETURNED (sent in) or NOT_RETURNED (checked out while away).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parking_trips", indexes = {
		@Index(name = "ix_parking_trips_parking", columnList = "parking_id"),
		@Index(name = "ix_parking_trips_status_expected", columnList = "status, expectedReturnAt"),
		@Index(name = "ix_parking_trips_sent_out_at", columnList = "sentOutAt")
})
public class ParkingTrip {

	public static final String OUT = "OUT";
	public static final String RETURNED = "RETURNED";
	public static final String NOT_RETURNED = "NOT_RETURNED";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Gate pass number */
	@Column(unique = true, nullable = false)
	private String no;

	@ManyToOne(targetEntity = Parking.class, fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "parking_id", nullable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Parking parking;

	@ManyToOne(targetEntity = Branch.class, fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "branch_id", nullable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	private Branch branch;

	/** SERVICE_REPAIR, TESTING, LOADING or OTHER */
	@Column(nullable = false)
	private String reason;
	private String reasonDetails;

	@Column(nullable = false)
	private String driverName;
	@Column(nullable = false)
	private String driverPhoneNo;
	@Column(nullable = false)
	private String driverIdType;
	@Column(nullable = false)
	private String driverIdNo;

	/** The driver left the required documents at the yard; recorded, not required */
	private boolean documentsLeft;

	@Column(nullable = false)
	private LocalDateTime expectedReturnAt;

	@Column(nullable = false)
	private String status = OUT;

	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "sent_out_by_user_id", nullable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User sentOutByUser;

	@Column(nullable = false)
	private LocalDateTime sentOutAt;

	/** Who received it back (RETURNED), or who checked it out while away (NOT_RETURNED) */
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "received_by_user_id", nullable = true)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User receivedByUser;

	private LocalDateTime receivedAt;

	private boolean documentsReturned;

	private String returnNotes;
}
