package com.orbix.api.modules.finance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface BillReceivableService {
	
	List<BillReceivableResponseDTO> confirmBillPayment(
			List<BillReceivableRequestDTO> billRequests, 
			double totalAmount,
			HttpServletRequest request);
}