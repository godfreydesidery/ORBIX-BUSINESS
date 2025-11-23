package com.orbix.api.modules.weighbridge;

import java.util.List;

import javax.servlet.http.HttpServletRequest;


public interface WeighBillReceivableService {
	public List<WeighBillReceivableResponseDTO> getAllByWeigh(Long weighId, HttpServletRequest request);
//	public WeighBillReceivableResponseDTO createWeighBillReceivable(WeighBillReceivableRequestDTO weighBillReceivableRequestDTO, HttpServletRequest request);
//	public WeighBillReceivableResponseDTO updateWeighBillReceivable(WeighBillReceivableRequestDTO weighBillReceivableRequestDTO, HttpServletRequest request);
//	public WeighBillReceivableResponseDTO createWeighCustomBillReceivable(WeighBillReceivableRequestDTO weighBillReceivableRequestDTO, HttpServletRequest request);
	public WeighBillReceivableResponseDTO getWeighBillReceivable(Long id, HttpServletRequest request);
	
//	public BillViewResponseDTO getBillView(Long weighId, HttpServletRequest request);
}
