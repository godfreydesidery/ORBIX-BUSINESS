/**
 * 
 */
package com.orbix.api.service;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import com.orbix.api.domain.CompanyProfile;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.repositories.CompanyProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author GODFREY
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompanyProfileServiceImpl implements CompanyProfileService {
	
	private final CompanyProfileRepository companyProfileRepository;

	@Override
	public CompanyProfile saveCompanyProfile(CompanyProfile companyProfile) {
		List<CompanyProfile> profiles = companyProfileRepository.findAll();
		int i = 0;
		CompanyProfile profile = new CompanyProfile();
		for(CompanyProfile p : profiles) {
			i = i + 1;
			profile = p;
		}
		if(validateCompany(companyProfile)) {
			if(i > 1) {
				companyProfileRepository.deleteAll();
			}else if(companyProfile.getId() == null) {
				companyProfileRepository.deleteAll();
			}
			if(i == 1) {
				profile.setCompanyName(companyProfile.getCompanyName());
				profile.setContactName(companyProfile.getContactName());
				profile.setTin(companyProfile.getTin());
				profile.setVrn(companyProfile.getVrn());
				profile.setPhysicalAddress(companyProfile.getPhysicalAddress());
				profile.setPostCode(companyProfile.getPostCode());
				profile.setPostAddress(companyProfile.getPostAddress());
				profile.setTelephone(companyProfile.getTelephone());
				profile.setMobile(companyProfile.getMobile());
				profile.setEmail(companyProfile.getEmail());
				profile.setWebsite(companyProfile.getWebsite());
				profile.setFax(companyProfile.getFax());
				
				profile.setBankAccountName(companyProfile.getBankAccountName());
				profile.setBankPhysicalAddress(companyProfile.getBankPhysicalAddress());
				profile.setBankPostAddress(companyProfile.getBankPostAddress());
				profile.setBankPostCode(companyProfile.getBankPostCode());
				profile.setBankName(companyProfile.getBankName());
				profile.setBankAccountNo(companyProfile.getBankAccountNo());
				
				profile.setBankAccountName2(companyProfile.getBankAccountName2());
				profile.setBankPhysicalAddress2(companyProfile.getBankPhysicalAddress2());
				profile.setBankPostAddress2(companyProfile.getBankPostAddress2());
				profile.setBankPostCode2(companyProfile.getBankPostCode2());
				profile.setBankName2(companyProfile.getBankName2());
				profile.setBankAccountNo2(companyProfile.getBankAccountNo2());
				
				profile.setBankAccountName3(companyProfile.getBankAccountName3());
				profile.setBankPhysicalAddress3(companyProfile.getBankPhysicalAddress3());
				profile.setBankPostAddress3(companyProfile.getBankPostAddress3());
				profile.setBankPostCode3(companyProfile.getBankPostCode3());
				profile.setBankName3(companyProfile.getBankName3());
				profile.setBankAccountNo3(companyProfile.getBankAccountNo3());
				
				profile.setQuotationNotes(companyProfile.getQuotationNotes());
				profile.setSalesInvoiceNotes(companyProfile.getSalesInvoiceNotes());
				
				profile.setRegistrationFee(companyProfile.getRegistrationFee());
				profile.setPublicPath(companyProfile.getPublicPath());
				
				profile.setEmployeePrefix(companyProfile.getEmployeePrefix());
			}else {
				profile = companyProfile;
			}
			return companyProfileRepository.saveAndFlush(profile);
		}else {
			throw new InvalidEntryException("Invalid company information");
		}
	}
	
	private boolean validateCompany(CompanyProfile profile) {
		/**
		 * Add validation logic, return true if valid, else false
		 */
		
		return true;
	}

	@Override
	public CompanyProfile getCompanyProfile(HttpServletRequest request) {
		List<CompanyProfile> profiles = companyProfileRepository.findAll();
		CompanyProfile profile = new CompanyProfile();
		for(CompanyProfile p : profiles) {
			profile = p;
			//profile.setLogo(decompressBytes(p.getLogo()));
			break;
		}	
		return profile;
 	}
	
	public static byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException ioe) {
        } catch (DataFormatException e) {
        }
        return outputStream.toByteArray();
    }
	
	@Override
	public boolean hasData() {
		return companyProfileRepository.hasData();
	}

}
