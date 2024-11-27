package com.orbix.api.modules.finance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface InvoiceReceivableService {
	List<InvoiceReceivableResponseDTO> getAllInvoiceReceivables(HttpServletRequest request);
	//Get receivable invoices for parking(pending)
	List<InvoiceReceivableResponseDTO> getPendingParkingInvoiceReceivables(HttpServletRequest request);
	InvoiceReceivableResponseDTO get(Long id, HttpServletRequest request);
}
