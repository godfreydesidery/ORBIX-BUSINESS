package com.orbix.api.modules.finance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.PayCode;

public interface BillReceivableService {
	
	List<BillReceivableResponseDTO> confirmBillPayment(
			List<BillReceivableRequestDTO> billReceivableRequests, 
			PayCode payCode,
			String payRefNo,
			double totalAmount,
			HttpServletRequest request);
	
	List<BillReceivableResponseDTO> getAllByParking( 
			Long parkingId,
			HttpServletRequest request);
	
	List<BillReceivableResponseDTO> getAllByStorage( 
			Long storageId,
			HttpServletRequest request);
	
	List<BillReceivableResponseDTO> getAllByMaintenance( 
			Long maintenanceId,
			HttpServletRequest request);
	
	List<BillReceivableResponseDTO> getAllByWeigh( 
			Long weighId,
			HttpServletRequest request);
}