/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author Godfrey
 *
 */
public interface MaterialUsageReport {
	LocalDate getDate();
	String getCode();
	String getDescription();
	double getQty();
	double getAmount();
}
