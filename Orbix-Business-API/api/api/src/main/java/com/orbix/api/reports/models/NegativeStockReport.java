/**
 * 
 */
package com.orbix.api.reports.models;

/**
 * @author Godfrey
 *
 */
public interface NegativeStockReport {
	String getBarcode();
	String getCode();
	String getDescription();
	double getStock();
}
