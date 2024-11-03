package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data  
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "currencies")
public class Currency {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code; // ISO 4217 code, e.g., "USD", "EUR"

    @Column(name = "name", nullable = false)
    private String name; // Full name, e.g., "US Dollar", "Euro"

    @Column(name = "symbol")
    private String symbol; // Symbol, e.g., "$", "€"

    @Column(name = "country")
    private String country; // Country associated with the currency

    @Column(name = "decimal_places", nullable = false)
    private Integer decimalPlaces; // Decimal precision, e.g., 2 for USD

    @Column(name = "exchange_rate_to_usd")
    private Double exchangeRateToUsd; // Exchange rate relative to USD (optional)
    
    private boolean defaultCurrency = false;
    
    private LocalDateTime createdDateTime = LocalDateTime.now();
}
