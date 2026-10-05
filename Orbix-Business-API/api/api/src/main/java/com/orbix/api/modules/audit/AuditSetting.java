package com.orbix.api.modules.audit;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The audit log's settings, for the whole system: a single row. Without a row, recording is off.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_settings")
public class AuditSetting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Whether sign-ins and critical actions are recorded */
	@Column(nullable = false)
	private boolean recordingEnabled = false;

	/** When recording was last turned on or off, in UTC */
	private LocalDateTime updatedAt;

	/** Who last turned recording on or off */
	@Column(length = 100)
	private String updatedBy;
}
