package com.orbix.api.modules.finance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface BillReceivableService {
	
	List<BillReceivableResponseDTO> confirmBillPayment(
			BillReceivableSummaryDTO billReceivableSummary, 
			double totalAmount,
			HttpServletRequest request);
	
	List<BillReceivableResponseDTO> getAllByParking( 
			Long parkingId,
			HttpServletRequest request);
}