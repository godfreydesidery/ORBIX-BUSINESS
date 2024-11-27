package com.orbix.api.modules.finance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceReceivableDetailRepository extends JpaRepository<InvoiceReceivableDetail, Long> {

	InvoiceReceivableDetail findByBillReceivable(BillReceivable billReceivable);

}
