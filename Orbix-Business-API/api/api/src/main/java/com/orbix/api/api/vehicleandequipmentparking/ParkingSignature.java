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

import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * An electronic signature given at one step of a parking: the inspector and the driver at INSPECTION, the registrar
 * at REGISTRATION (which checks the vehicle in), and whoever modifies the record afterwards at MODIFICATION.
 *
 * Rows are only ever inserted, never updated or deleted, so earlier signatures always survive. The image is a tiny
 * black and white PNG (a few hundred bytes): enough to show that someone signed, not a faithful copy.
 *
 * A MODIFICATION row also keeps what changed, because audit log entries are cleared after 90 days while this
 * history must last as long as the record.
 *
 * Every column is nullable: required signatures are checked by the steps, not by the database.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parking_signatures", indexes = {
		@Index(name = "ix_parking_signatures_parking", columnList = "parking_id, signedAt")
})
public class ParkingSignature {

	public static final String INSPECTION = "INSPECTION";
	public static final String REGISTRATION = "REGISTRATION";
	public static final String MODIFICATION = "MODIFICATION";

	public static final String INSPECTOR = "INSPECTOR";
	public static final String DRIVER = "DRIVER";
	public static final String REGISTRAR = "REGISTRAR";
	public static final String MODIFIER = "MODIFIER";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(targetEntity = Parking.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "parking_id", nullable = true, updatable = false)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Parking parking;

	@Column(length = 20)
	private String stage;

	@Column(length = 20)
	private String role;

	/** The signing user; empty for the driver, who is not a system user */
	@ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "user_id", nullable = true, updatable = false)
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User user;

	/** The user's name, or the name the driver typed */
	private String signerName;

	/** PNG, at most SignatureImages.MAX_BYTES. Signatures are read only for one vehicle's details, never in lists. */
	@Column(columnDefinition = "BLOB")
	@ToString.Exclude
	private byte[] image;

	private LocalDateTime signedAt;

	/** MODIFICATION only: what changed, one "Field: old → new" per line */
	@Column(columnDefinition = "TEXT")
	private String changes;
}
