package com.orbix.api.modules.bond;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.finance.BillReceivable;

public interface BondItemBillReceivableRepository extends JpaRepository<BondItemBillReceivable, Long> {

	List<BondItemBillReceivable> findAllByBondItem(BondItem bondItem);

	List<BondItemBillReceivable> findByBondItem(BondItem bondItem);

	Optional<BondItemBillReceivable> findByBillReceivable(BillReceivable billReceivable);

	List<BondItemBillReceivable> findAllByBillReceivableIn(List<BillReceivable> billReceivables);

	@Query("SELECT b FROM BondItemBillReceivable b JOIN b.bondItem i WHERE b.discountStatus = :discountStatus AND i.status IN :statuses ORDER BY i.id, b.id")
	List<BondItemBillReceivable> findAllByDiscountStatusAndBondItem_StatusIn(@Param("discountStatus") String discountStatus, @Param("statuses") List<String> statuses);

	@Query("SELECT b.id FROM BondItemBillReceivable b WHERE b.bondItem = :bondItem ORDER BY b.id")
	List<Long> getIdsByBondItem(@Param("bondItem") BondItem bondItem);

	@Query("SELECT b FROM BondItemBillReceivable b LEFT JOIN FETCH b.billReceivable WHERE b.bondItem IN :bondItems ORDER BY b.id")
	List<BondItemBillReceivable> findAllByBondItemIn(@Param("bondItems") List<BondItem> bondItems);

}
