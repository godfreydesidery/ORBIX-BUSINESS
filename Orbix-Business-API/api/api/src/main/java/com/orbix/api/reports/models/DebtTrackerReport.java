/**
 * 
 */
package com.orbix.api.reports.models;

import java.time.LocalDate;

/**
 * @author Godfrey
 *
 */
public interface DebtTrackerReport {
	Long getId();
	String getNo();
	String getCustomerName();
	String getOfficerIncharge();
	LocalDate getInceptionDate();
	double getTotalAmount();
	double getAmountPaid();
	double getBalance();
	String getStatus();	
}
