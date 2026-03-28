package com.orbix.api.modules.adminunits;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CurrencyConversionService {

    List<CurrencyConversionResponseDTO> getAllCurrencyConversions(HttpServletRequest request);

    CurrencyConversionResponseDTO get(Long id, HttpServletRequest request);

    CurrencyConversionResponseDTO createCurrencyConversion(CurrencyConversionRequestDTO conversionRequest, HttpServletRequest request);

    CurrencyConversionResponseDTO updateCurrencyConversion(CurrencyConversionRequestDTO conversionRequest, HttpServletRequest request);

}