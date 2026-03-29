package com.orbix.api.modules.adminunits;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

@Service
public class CurrencyConversionServiceController implements CurrencyConversionService {
    private final CurrencyConversionRepository currencyConversionRepository;
    
    // Constructor
    public CurrencyConversionServiceController(CurrencyConversionRepository currencyConversionRepository) {
        this.currencyConversionRepository = currencyConversionRepository;
    }

    @Override
    public List<CurrencyConversionResponseDTO> getAllCurrencyConversions(HttpServletRequest request) {
        List<CurrencyConversion> conversions = currencyConversionRepository.findAll();
        List<CurrencyConversionResponseDTO> conversionResponses = new ArrayList<>();
        
        for(CurrencyConversion conversion : conversions) {
            conversionResponses.add(currencyConversionResponseDTOMapper(conversion));
        }
        return conversionResponses;
    }

    @Override
    public CurrencyConversionResponseDTO get(Long id, HttpServletRequest request) {
        Optional<CurrencyConversion> conversion = currencyConversionRepository.findById(id);
        if(conversion.isEmpty()) {
            throw new NotFoundException("Currency Conversion not found");
        }
        return currencyConversionResponseDTOMapper(conversion.get());
    }

    @Override
    public CurrencyConversionResponseDTO createCurrencyConversion(CurrencyConversionRequestDTO conversionRequest, HttpServletRequest request) {
        CurrencyConversion conversion = new CurrencyConversion();
        
        // Set fields from the request
        conversion.setSourceCurrencyCode(java.util.Currency.getInstance(conversionRequest.getSourceCurrencyCode().getCurrencyCode()));
        conversion.setFinalCurrencyCode(java.util.Currency.getInstance(conversionRequest.getFinalCurrencyCode().getCurrencyCode()));
        conversion.setSourceCurrencyValue(conversionRequest.getSourceCurrencyValue());
        conversion.setFinalCurrencyValue(conversionRequest.getFinalCurrencyValue());
        conversion.setActive(conversionRequest.isActive());
        
        // Save conversion
        conversion = currencyConversionRepository.save(conversion);
        
        return currencyConversionResponseDTOMapper(conversion);
    }

	@Override
	public CurrencyConversionResponseDTO updateCurrencyConversion(CurrencyConversionRequestDTO conversionRequest,
			HttpServletRequest request) {
		Optional<CurrencyConversion> conversion = currencyConversionRepository.findById(conversionRequest.getId());
		if (conversion.isEmpty()) {
			throw new NotFoundException("Currency Conversion not found");
		}

		if (!conversion.get().getSourceCurrencyCode()
				.equals(java.util.Currency.getInstance(conversionRequest.getSourceCurrencyCode().getCurrencyCode())))
			throw new InvalidOperationException("Changing source currency code is not allowed");
		
		if (!conversion.get().getFinalCurrencyCode()
				.equals(java.util.Currency.getInstance(conversionRequest.getFinalCurrencyCode().getCurrencyCode())))
			throw new InvalidOperationException("Changing final currency code is not allowed");


		CurrencyConversion existingConversion = conversion.get();
		existingConversion.setSourceCurrencyValue(conversionRequest.getSourceCurrencyValue());
		existingConversion.setFinalCurrencyValue(conversionRequest.getFinalCurrencyValue());
		existingConversion.setActive(conversionRequest.isActive());

		existingConversion = currencyConversionRepository.save(existingConversion);

		return currencyConversionResponseDTOMapper(existingConversion);
	}
    
    private CurrencyConversionResponseDTO currencyConversionResponseDTOMapper(CurrencyConversion conversion) {
        CurrencyConversionResponseDTO response = new CurrencyConversionResponseDTO();
        response.setId(conversion.getId().toString());
        response.setSourceCurrencyCode(conversion.getSourceCurrencyCode().toString());
        response.setSourceCurrencyValue(String.valueOf(conversion.getSourceCurrencyValue()));
        response.setFinalCurrencyCode(conversion.getFinalCurrencyCode().toString());
        response.setFinalCurrencyValue(String.valueOf(conversion.getFinalCurrencyValue()));
        response.setActive(conversion.isActive() ? "Active" : "Inactive");
        
        return response;
    }
}