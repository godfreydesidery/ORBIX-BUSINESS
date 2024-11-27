/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author Godfrey
 *
 */
public interface LpoReport {
	LocalDate getDate();
	String getBarcode();
	String getCode();
	String getDescription();
	double getQty();
	double getAmount();
	String getSupplierName();
	String getLpoNo();
}
