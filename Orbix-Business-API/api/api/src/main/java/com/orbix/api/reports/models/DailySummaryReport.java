/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author Godfrey
 *
 */
public interface DailySummaryReport {
	LocalDate getDate();
	double getOpeningStockValue();
	double getClosingStockValue();
	//double getPurchaseOnCash();
	double getPurchaseOnCredit();
	//LocalDate getPaymentDate();
	double getAmountPaid();
	double getCashSales();
	double getGrossMargin();
	double getCreditSales();
	//double getStockTarget();
	//double GetVariationInStockTarget();
	
	
	double getTotalSalesVatExcl();
	double getTotalSalesVatIncl();
	double getTotalVat();
	double getTotalPurchases();
}
