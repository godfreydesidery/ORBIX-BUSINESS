package com.orbix.api.modules.audit;

import lombok.Data;

@Data
public class AuditSettingResponseDTO {
	private boolean recordingEnabled;
	private String updatedAt;
	private String updatedBy;
}
