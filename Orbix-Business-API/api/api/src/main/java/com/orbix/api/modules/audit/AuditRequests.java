package com.orbix.api.modules.audit;

import javax.servlet.http.HttpServletRequest;

/**
 * Where a request came from, as recorded in the audit log
 */
public final class AuditRequests {

	private AuditRequests() {
	}

	/** The client's address: the first X-Forwarded-For entry when behind a proxy, otherwise the remote address */
	public static String ipAddress(HttpServletRequest request) {
		if(request == null) {
			return null;
		}
		String forwardedFor = request.getHeader("X-Forwarded-For");
		String ipAddress = (forwardedFor != null && !forwardedFor.isBlank()) ? forwardedFor.split(",")[0].trim() : request.getRemoteAddr();
		return truncate(ipAddress, 45);
	}

	/** The client's browser, as sent in the User-Agent header */
	public static String userAgent(HttpServletRequest request) {
		return request == null ? null : truncate(request.getHeader("User-Agent"), 255);
	}

	static String truncate(String value, int length) {
		return (value == null || value.length() <= length) ? value : value.substring(0, length);
	}
}
