package com.orbix.api.modules.audit;

import lombok.Data;

@Data
public class AuditLogResponseDTO {
	private Long id;
	/** UTC, ISO-8601 with a trailing Z, so the screen can show it in local time */
	private String occurredAt;
	private String category;
	private String action;
	private String outcome;
	private Long userId;
	private String username;
	private Long companyId;
	private Long branchId;
	private String entityType;
	private String entityId;
	private String entityRef;
	private String summary;
	private String details;
	private String ipAddress;
	private String userAgent;
}
