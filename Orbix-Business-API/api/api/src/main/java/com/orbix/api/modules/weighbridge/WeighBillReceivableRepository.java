package com.orbix.api.modules.weighbridge;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.finance.BillReceivable;

public interface WeighBillReceivableRepository extends JpaRepository<WeighBillReceivable, Long> {

	List<WeighBillReceivable> findByWeigh(Weigh weigh);

	List<WeighBillReceivable> findAllByWeigh(Weigh weigh);

	Optional<WeighBillReceivable> findByBillReceivable(BillReceivable billReceivable);

}
