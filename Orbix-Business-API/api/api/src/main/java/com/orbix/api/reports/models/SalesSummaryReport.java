/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author Godfrey
 *
 */
public interface SalesSummaryReport {
	LocalDate getDate();
	double getTotalSalesVatExcl();
	double getTotalSalesVatIncl();
	double getGrossMargin();
	double getTotalVat();
}
