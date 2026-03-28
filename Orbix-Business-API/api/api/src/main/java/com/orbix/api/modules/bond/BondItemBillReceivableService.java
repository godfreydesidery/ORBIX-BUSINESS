package com.orbix.api.modules.bond;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface BondItemBillReceivableService {
	
	public List<BondItemBillReceivableResponseDTO> getAllByBondItem(Long bondItemId, HttpServletRequest request);	
	public BondItemBillReceivableResponseDTO createBondItemBillReceivable(BondItemBillReceivableRequestDTO bondItemBillReceivableRequestDTO, HttpServletRequest request);
	public BondItemBillReceivableResponseDTO updateBondItemBillReceivable(BondItemBillReceivableRequestDTO bondItemBillReceivableRequestDTO, HttpServletRequest request);
	public BondItemBillReceivableResponseDTO createBondItemCustomBillReceivable(BondItemBillReceivableRequestDTO bondItemBillReceivableRequestDTO, HttpServletRequest request);
	public BondItemBillReceivableResponseDTO getBondItemBillReceivable(Long id, HttpServletRequest request);
	
	public BillViewResponseDTO getBillView(Long bondItemId, HttpServletRequest request);
}
