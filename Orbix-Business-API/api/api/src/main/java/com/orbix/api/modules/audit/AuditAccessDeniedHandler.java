package com.orbix.api.modules.audit;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

/**
 * Records a refused request in the audit log, then answers exactly as Spring Security's default handler does (403)
 */
public class AuditAccessDeniedHandler implements AccessDeniedHandler {

	private final AuditLogService auditLogService;
	private final AccessDeniedHandler defaultHandler = new AccessDeniedHandlerImpl();

	public AuditAccessDeniedHandler(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
			throws IOException, ServletException {
		try {
			String username = request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();
			auditLogService.recordAccessDenied(username, request.getMethod() + " " + request.getRequestURI(),
					AuditRequests.ipAddress(request), AuditRequests.forwardedFor(request), AuditRequests.userAgent(request));
		}catch(Exception e) {
			// Recording must never change the response
		}
		defaultHandler.handle(request, response, accessDeniedException);
	}
}
