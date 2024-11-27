/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author Godfrey
 *
 */
public interface DailyPurchaseReport {
	LocalDate getDate();
	double getAmount();
}
