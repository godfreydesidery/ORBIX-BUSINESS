/**
 * 
 */
package com.orbix.api.service;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.domain.CompanyProfile;

/**
 * @author GODFREY
 *
 */
public interface CompanyProfileService {
	CompanyProfile saveCompanyProfile(CompanyProfile companyProfile);
	CompanyProfile getCompanyProfile(HttpServletRequest request);
	boolean hasData();
}
