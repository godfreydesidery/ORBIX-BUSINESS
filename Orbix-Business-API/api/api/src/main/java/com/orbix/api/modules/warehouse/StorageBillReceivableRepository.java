package com.orbix.api.modules.warehouse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.finance.BillReceivable;

public interface StorageBillReceivableRepository extends JpaRepository<StorageBillReceivable, Long> {

	List<StorageBillReceivable> findAllByStorage(Storage storage);

	List<StorageBillReceivable> findByStorage(Storage storage);

	Optional<StorageBillReceivable> findByBillReceivable(BillReceivable billReceivable);

}
