package com.orbix.api.api.commons;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One page of a list, with the number of rows in the whole list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {
	private List<T> content;
	private long totalElements;
}
