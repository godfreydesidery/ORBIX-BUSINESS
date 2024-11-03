package com.orbix.api.modules.adminunits;

import javax.servlet.http.HttpServletRequest;


public interface SystemProfileService {
	SystemProfile saveSystemProfile(SystemProfile systemProfile);
	SystemProfile getSystemProfile(HttpServletRequest request);
	boolean hasData();
	
	TimeZone getDefaultTimeZone();
	Currency getDefaultCurrency();
	
	void createDefaultTimeZone();
	void createDefaultCurrency();
}
