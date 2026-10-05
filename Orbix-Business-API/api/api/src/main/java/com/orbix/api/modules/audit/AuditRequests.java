package com.orbix.api.modules.audit;

import javax.servlet.http.HttpServletRequest;

/**
 * Where a request came from, as recorded in the audit log
 */
public final class AuditRequests {

	private AuditRequests() {
	}

	/** The address the request actually came from (the connection's remote address, which the client cannot fake) */
	public static String ipAddress(HttpServletRequest request) {
		return request == null ? null : truncate(request.getRemoteAddr(), 45);
	}

	/**
	 * The X-Forwarded-For header, if any. It names the original client when a proxy sits in front of the server,
	 * but any client can send it, so it is kept separately in the details rather than trusted as the address.
	 */
	public static String forwardedFor(HttpServletRequest request) {
		if(request == null) {
			return null;
		}
		String forwardedFor = request.getHeader("X-Forwarded-For");
		return (forwardedFor == null || forwardedFor.isBlank()) ? null : truncate(forwardedFor.trim(), 200);
	}

	/** The client's browser, as sent in the User-Agent header */
	public static String userAgent(HttpServletRequest request) {
		return request == null ? null : truncate(request.getHeader("User-Agent"), 255);
	}

	static String truncate(String value, int length) {
		return (value == null || value.length() <= length) ? value : value.substring(0, length);
	}
}
