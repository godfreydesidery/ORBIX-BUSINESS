/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author Godfrey
 *
 */
public interface PurchaseSummaryReport {
	LocalDate getDate();
	double getTotalPurchases();
	//double getTotalPurchasesVatExcl();
}
