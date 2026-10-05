package com.orbix.api.modules.audit;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One entry of the audit log: a sign-in event or a critical action.
 *
 * The user, company, branch and record are kept as plain ids (no foreign keys), and the readable values
 * (username, reference, summary) are copied when the entry is written, so an entry stays readable after
 * the records it refers to change or are deleted. Entries are only ever inserted.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_logs", indexes = {
		@Index(name = "ix_audit_logs_occurred", columnList = "occurredAt"),
		@Index(name = "ix_audit_logs_user_occurred", columnList = "userId, occurredAt"),
		@Index(name = "ix_audit_logs_action_occurred", columnList = "action, occurredAt"),
		@Index(name = "ix_audit_logs_entity", columnList = "entityType, entityId"),
		@Index(name = "ix_audit_logs_branch_occurred", columnList = "branchId, occurredAt")
})
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** When it happened, in UTC */
	@Column(nullable = false, columnDefinition = "DATETIME(3)")
	private LocalDateTime occurredAt;

	/** AUTH, SECURITY, FINANCE, OPERATIONS, INVENTORY, PROCUREMENT, SALES or SETTINGS */
	@Column(nullable = false, length = 20)
	private String category;

	/** e.g. LOGIN_FAILED, PAYMENT_CONFIRMED */
	@Column(nullable = false, length = 60)
	private String action;

	/** SUCCESS or FAILURE */
	@Column(nullable = false, length = 10)
	private String outcome;

	private Long userId;

	/** The username at the time; for a failed sign-in, the name that was typed */
	@Column(length = 100)
	private String username;

	private Long companyId;

	private Long branchId;

	@Column(length = 60)
	private String entityType;

	@Column(length = 40)
	private String entityId;

	/** A readable reference to the record, e.g. a bill or parking number, or a username */
	@Column(length = 100)
	private String entityRef;

	@Column(nullable = false, length = 255)
	private String summary;

	/** Key values as JSON; never passwords or tokens */
	@Column(columnDefinition = "TEXT")
	private String details;

	@Column(length = 45)
	private String ipAddress;

	@Column(length = 255)
	private String userAgent;
}
