/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

import lombok.Data;

/**
 * @author Godfrey
 *
 */
@Data
public class PurchasesAndSalesSummaryReport {
	LocalDate date;
	double openingStockValue;
	double totalPurchases;
	double totalSalesVatExcl;
	double totalSalesVatIncl;
	double grossMargin;
	double totalVat;
	double closingStockValue;	
}
