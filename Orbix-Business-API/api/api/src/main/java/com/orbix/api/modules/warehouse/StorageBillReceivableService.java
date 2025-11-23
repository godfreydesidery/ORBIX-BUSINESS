package com.orbix.api.modules.warehouse;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface StorageBillReceivableService {
	
	public List<StorageBillReceivableResponseDTO> getAllByStorage(Long storageId, HttpServletRequest request);	
	public StorageBillReceivableResponseDTO createStorageBillReceivable(StorageBillReceivableRequestDTO storageBillReceivableRequestDTO, HttpServletRequest request);
	public StorageBillReceivableResponseDTO updateStorageBillReceivable(StorageBillReceivableRequestDTO storageBillReceivableRequestDTO, HttpServletRequest request);
	public StorageBillReceivableResponseDTO createStorageCustomBillReceivable(StorageBillReceivableRequestDTO storageBillReceivableRequestDTO, HttpServletRequest request);
	public StorageBillReceivableResponseDTO getStorageBillReceivable(Long id, HttpServletRequest request);
	
	public BillViewResponseDTO getBillView(Long storageId, HttpServletRequest request);
}
