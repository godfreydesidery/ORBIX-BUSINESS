/**
 * 
 */
package com.orbix.api.reports.models;

/**
 * @author Godfrey
 *
 */
public interface SupplierStockStatusReport {
	String getCode();
	String getDescription();
	double getStock();
	double getCostPriceVatIncl();
	double getSellingPriceVatIncl();
}
