package com.orbix.api.modules.adminunits;

import java.time.LocalDateTime;
import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "currency_conversions", uniqueConstraints = { 
    @UniqueConstraint(columnNames = {"source_currency_code", "final_currency_code"})
})
public class CurrencyConversion {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean active = true;
    
    @Column(name = "source_currency_code")
    private Currency sourceCurrencyCode;

    private Double sourceCurrencyValue;

    @Column(name = "final_currency_code")
    private Currency finalCurrencyCode;

    private Double finalCurrencyValue;

    private LocalDateTime createdDateTime = LocalDateTime.now();

    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @JsonIgnoreProperties("currencyConversions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
}
