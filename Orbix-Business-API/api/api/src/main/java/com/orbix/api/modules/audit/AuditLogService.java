package com.orbix.api.modules.audit;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.PageResponseDTO;

public interface AuditLogService {

	/** Records a critical action of the current user once the current transaction commits */
	void recordAction(AuditLog auditLog);

	/** Records a sign-in event (sign-in, failed sign-in, token refresh, sign-out) in the background */
	void recordAuth(String action, String outcome, String username, String reason, String ipAddress, String userAgent);

	/** Records a refused request (the user lacks the privilege) in the background */
	void recordAccessDenied(String username, String path, String ipAddress, String userAgent);

	PageResponseDTO<AuditLogResponseDTO> getAuditLogPage(String from, String to, String category, String action, String outcome,
			Long userId, Long branchId, int page, int size, String search, HttpServletRequest request);

	AuditLogResponseDTO get(Long id, HttpServletRequest request);
}
