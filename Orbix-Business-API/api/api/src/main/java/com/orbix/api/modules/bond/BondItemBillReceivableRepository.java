package com.orbix.api.modules.bond;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.finance.BillReceivable;

public interface BondItemBillReceivableRepository extends JpaRepository<BondItemBillReceivable, Long> {

	List<BondItemBillReceivable> findAllByBondItem(BondItem bondItem);

	List<BondItemBillReceivable> findByBondItem(BondItem bondItem);

	Optional<BondItemBillReceivable> findByBillReceivable(BillReceivable billReceivable);

}
