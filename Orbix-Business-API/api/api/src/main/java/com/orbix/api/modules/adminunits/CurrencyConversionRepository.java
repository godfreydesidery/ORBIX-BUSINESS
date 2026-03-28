package com.orbix.api.modules.adminunits;

import java.util.Currency;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CurrencyConversionRepository extends JpaRepository<CurrencyConversion, Long> {

	CurrencyConversion findBySourceCurrencyCodeAndFinalCurrencyCode(Currency currency, Currency instance);
	
	@Modifying
	@Transactional
	@Query("UPDATE CurrencyConversion c SET c.finalCurrencyValue = 0, c.active = false")
	int resetAllRates();
}