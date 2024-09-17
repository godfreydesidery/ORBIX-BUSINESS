/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author Godfrey
 *
 */
public interface ProductStockSummaryReport {
	LocalDate getDate();
	double getOpeningStockValue();
	double getClosingStockValue();
}
