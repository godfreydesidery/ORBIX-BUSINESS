package com.orbix.api.modules.weighbridge;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.finance.BillReceivable;

public interface WeighBillReceivableRepository extends JpaRepository<WeighBillReceivable, Long> {

	List<WeighBillReceivable> findByWeigh(Weigh weigh);

	List<WeighBillReceivable> findAllByWeigh(Weigh weigh);

	Optional<WeighBillReceivable> findByBillReceivable(BillReceivable billReceivable);

	List<WeighBillReceivable> findAllByBillReceivableIn(List<BillReceivable> billReceivables);

	@Query("SELECT b FROM WeighBillReceivable b LEFT JOIN FETCH b.billReceivable WHERE b.weigh IN :weighs ORDER BY b.id")
	List<WeighBillReceivable> findAllByWeighIn(@Param("weighs") List<Weigh> weighs);

}
