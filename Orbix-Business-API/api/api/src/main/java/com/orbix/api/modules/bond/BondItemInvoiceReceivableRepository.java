package com.orbix.api.modules.bond;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BondItemInvoiceReceivableRepository extends JpaRepository<BondItemInvoiceReceivable, Long> {

	List<BondItemInvoiceReceivable> findAllByBondItem(BondItem storage);

}
