package com.orbix.api.modules.warehouse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.finance.BillReceivable;

public interface StorageBillReceivableRepository extends JpaRepository<StorageBillReceivable, Long> {

	List<StorageBillReceivable> findAllByStorage(Storage storage);

	List<StorageBillReceivable> findByStorage(Storage storage);

	Optional<StorageBillReceivable> findByBillReceivable(BillReceivable billReceivable);

	List<StorageBillReceivable> findAllByBillReceivableIn(List<BillReceivable> billReceivables);

	@Query("SELECT b FROM StorageBillReceivable b JOIN b.storage s WHERE b.discountStatus = :discountStatus AND s.status IN :statuses ORDER BY s.id, b.id")
	List<StorageBillReceivable> findAllByDiscountStatusAndStorage_StatusIn(@Param("discountStatus") String discountStatus, @Param("statuses") List<String> statuses);

	@Query("SELECT b.id FROM StorageBillReceivable b WHERE b.storage = :storage ORDER BY b.id")
	List<Long> getIdsByStorage(@Param("storage") Storage storage);

	@Query("SELECT b FROM StorageBillReceivable b LEFT JOIN FETCH b.billReceivable WHERE b.storage IN :storages ORDER BY b.id")
	List<StorageBillReceivable> findAllByStorageIn(@Param("storages") List<Storage> storages);

}
