/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author GODFREY
 *
 */
public interface DailyProductionReport {
	LocalDate getDate();
	String getCode();
	String getDescription();
	double getQty();
	double getAmount();
}
