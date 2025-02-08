package com.orbix.api.modules.warehouse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageBillReceivableRepository extends JpaRepository<StorageBillReceivable, Long> {

	List<StorageBillReceivable> findAllByStorage(Storage storage);

	List<StorageBillReceivable> findByStorage(Storage storage);

}
