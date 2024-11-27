package com.orbix.api.modules.adminunits;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
	
	boolean existsBy();

	List<Currency> findAllByDefaultCurrency(boolean b);

}
