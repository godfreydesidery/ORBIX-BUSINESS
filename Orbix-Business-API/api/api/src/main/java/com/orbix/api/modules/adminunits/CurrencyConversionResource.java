package com.orbix.api.modules.adminunits;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orbix-business-api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CurrencyConversionResource {

    private final CurrencyConversionService currencyConversionService;

    public CurrencyConversionResource(CurrencyConversionService currencyConversionService) {
        this.currencyConversionService = currencyConversionService;
    }

    @GetMapping("/currency_conversions")
    public ResponseEntity<List<CurrencyConversionResponseDTO>> getAllCurrencyConversions(HttpServletRequest request) {
        return ResponseEntity.ok().body(currencyConversionService.getAllCurrencyConversions(request));
    }

    @GetMapping("/currency_conversions/get")
    public ResponseEntity<CurrencyConversionResponseDTO> getCurrencyConversion(@RequestParam Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(currencyConversionService.get(id, request));
    }

    @PostMapping("/currency_conversions/create")
    public ResponseEntity<CurrencyConversionResponseDTO> createCurrencyConversion(@RequestBody CurrencyConversionRequestDTO conversionRequest, HttpServletRequest request) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/currency-conversions/create").toUriString());
        return ResponseEntity.created(uri).body(currencyConversionService.createCurrencyConversion(conversionRequest, request));
    }

    @PostMapping("/currency_conversions/update")
    public ResponseEntity<CurrencyConversionResponseDTO> updateCurrencyConversion(@RequestBody CurrencyConversionRequestDTO conversionRequest, HttpServletRequest request) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/currency-conversions/update").toUriString());
        return ResponseEntity.created(uri).body(currencyConversionService.updateCurrencyConversion(conversionRequest, request));
    }

//    @PostMapping("/currency-conversions/reset-rates")
//    public ResponseEntity<ApiCustomResponse> resetAllRates(HttpServletRequest request) {
//        return ResponseEntity.ok().body(currencyConversionService.resetAllRates(request));
//    }
}