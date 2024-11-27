/**
 * 
 */
package com.orbix.api.reports.models;

/**
 * @author GODFREY
 *
 */
public interface SupplySalesReport {
	String getCode();
	String getDescription();
	String getStock();
	String getQty();
	double getAmount();
	double getDiscount();	
    double getTax();
    double getProfit();
}
