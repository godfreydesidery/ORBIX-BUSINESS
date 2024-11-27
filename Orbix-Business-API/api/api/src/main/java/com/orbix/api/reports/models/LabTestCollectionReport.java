package com.orbix.api.reports.models;

import java.time.LocalDateTime;

public interface LabTestCollectionReport {
	String getFirstName();
	String getMiddleName();
	String getLastName();
	String getFileNo();
	String getPhoneNo();
	double getAmount();
	String getPaymentType();
	String getCashier();
	LocalDateTime getDateTime();
}
