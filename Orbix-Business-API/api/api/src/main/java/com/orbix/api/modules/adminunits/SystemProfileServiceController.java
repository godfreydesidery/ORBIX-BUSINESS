package com.orbix.api.modules.adminunits;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidEntryException;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SystemProfileServiceController implements SystemProfileService {
	private final SystemProfileRepository systemProfileRepository;
	
	private final TimeZoneRepository timeZoneRepository;
	private final CurrencyRepository currencyRepository;

	@Override
	public SystemProfile saveSystemProfile(SystemProfile systemProfile) {
		List<SystemProfile> profiles = systemProfileRepository.findAll();
		int i = 0;
		SystemProfile profile = new SystemProfile();
		for(SystemProfile p : profiles) {
			i = i + 1;
			profile = p;
		}
		if(validateSystem(systemProfile)) {
			if(i > 1) {
				systemProfileRepository.deleteAll();
			}else if(systemProfile.getId() == null) {
				systemProfileRepository.deleteAll();
			}
			if(i == 1) {
				profile.setName(systemProfile.getName());
				profile.setContactName(systemProfile.getContactName());
				profile.setTin(systemProfile.getTin());
				profile.setVrn(systemProfile.getVrn());
				profile.setPhysicalAddress(systemProfile.getPhysicalAddress());
				profile.setPostCode(systemProfile.getPostCode());
				profile.setPostAddress(systemProfile.getPostAddress());
				profile.setTelephone(systemProfile.getTelephone());
				profile.setMobile(systemProfile.getMobile());
				profile.setEmail(systemProfile.getEmail());
				profile.setWebsite(systemProfile.getWebsite());
				profile.setFax(systemProfile.getFax());
				
			}else {
				profile = systemProfile;
			}
			return systemProfileRepository.saveAndFlush(profile);
		}else {
			throw new InvalidEntryException("Invalid system information");
		}
	}
	
	private boolean validateSystem(SystemProfile profile) {
		/**
		 * Add validation logic, return true if valid, else false
		 */
		
		return true;
	}

	@Override
	public SystemProfile getSystemProfile(HttpServletRequest request) {
		List<SystemProfile> profiles = systemProfileRepository.findAll();
		SystemProfile profile = new SystemProfile();
		for(SystemProfile p : profiles) {
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
		return systemProfileRepository.hasData();
	}

	@Override
	public TimeZone getDefaultTimeZone() {
		List<TimeZone> timeZones = timeZoneRepository.findAllByIsDefault(true);
		if (!timeZones.isEmpty()) {
		    return timeZones.get(0);
		    // Use firstTimeZone as needed
		} else {
		    // Handle the case where no time zones are found
		}
		return null;
	}
	
	@Override
	public Currency getDefaultCurrency() {
		List<Currency> currencies = currencyRepository.findAllByDefaultCurrency(true);
		if (!currencies.isEmpty()) {
		    return currencies.get(0);
		    // Use firstTimeZone as needed
		} else {
		    // Handle the case where no time zones are found
		}
		return null;
	}

	@Override
	public void createDefaultTimeZone() {
		// To create a default time zone
		
		if(timeZoneRepository.existsBy()) return;
		
		TimeZone timeZone = new TimeZone();
		timeZone.setName("Nairobi Time");
		timeZone.setAbbreviation("EAT");
		timeZone.setUtcOffset("+03:00");
		timeZone.setCountryRegion("East Africa");
		timeZone.setIsStandardTime(true);
		timeZone.setIanaCode("Africa/Nairobi");
		timeZone.setDstOffset(null); // Nairobi does not observe Daylight Saving Time (DST)
		timeZone.setDstStart(null);   // DST start date (null because DST is not observed)
		timeZone.setDstEnd(null);     // DST end date (null because DST is not observed)
		timeZone.setIsDefault(true);
		
		timeZoneRepository.save(timeZone);
		
	}

	@Override
	public void createDefaultCurrency() {
		// To create a default currency
		
		if(currencyRepository.existsBy()) return;
		
		Currency currency = new Currency();
		currency.setCode("TZS");        // ISO code for Kenyan Shilling
		currency.setName("Tanzanian Shilling");    // Full name of the currency
		currency.setSymbol("TSh");              // Symbol for the currency
		currency.setCountry("Tanzania");           // Country where the currency is used
		currency.setDecimalPlaces(2);           // Number of decimal places (usually 2 for most currencies)
		currency.setExchangeRateToUsd(0.0073);  // Example exchange rate to USD, adjust based on latest data
		currency.setDefaultCurrency(true);
		
		currencyRepository.save(currency);
	}
}
