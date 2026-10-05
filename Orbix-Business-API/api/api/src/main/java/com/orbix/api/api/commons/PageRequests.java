package com.orbix.api.api.commons;

import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Helpers for lists that are returned a page at a time and searched on the server
 */
public final class PageRequests {

	public static final int MAX_PAGE_SIZE = 100;

	private PageRequests() {
	}

	/**
	 * A page request whose size stays between 1 and MAX_PAGE_SIZE, and whose page number cannot overflow
	 */
	public static PageRequest of(int page, int size, Sort sort) {
		int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
		return PageRequest.of(Math.min(Math.max(page, 0), Integer.MAX_VALUE / pageSize), pageSize, sort);
	}

	/**
	 * The search text as a lower-case LIKE pattern, "%text%". LIKE wildcards typed by the user are matched literally.
	 * An empty search gives "%%", which the queries treat as "no search".
	 */
	public static String searchPattern(String search) {
		String text = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
		text = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return "%" + text + "%";
	}
}
