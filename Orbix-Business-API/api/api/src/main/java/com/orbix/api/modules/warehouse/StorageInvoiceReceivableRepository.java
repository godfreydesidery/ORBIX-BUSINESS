package com.orbix.api.modules.warehouse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageInvoiceReceivableRepository extends JpaRepository<StorageInvoiceReceivable, Long> {

	List<StorageInvoiceReceivable> findAllByStorage(Storage storage);

}
