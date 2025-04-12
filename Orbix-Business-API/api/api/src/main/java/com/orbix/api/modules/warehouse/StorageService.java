package com.orbix.api.modules.warehouse;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;



public interface StorageService {
	List<StorageResponseDTO> getAllStorages(HttpServletRequest request);	
	List<StorageResponseDTO> getAllPendingOrCheckedInStorages(HttpServletRequest request);
	List<StorageResponseDTO> getAllPendingOrCheckedInStoragesByWarehouse(Long warehouseId, HttpServletRequest request);
	List<StorageResponseDTO> getAllRecentCheckedOutStoragesByWarehouse(Long warehouseId, HttpServletRequest request);
	List<StorageResponseDTO> getAllCleared(HttpServletRequest request);
	List<StorageResponseDTO> getTodayCheckedOut(HttpServletRequest request);
	List<StorageResponseDTO> getAllCheckedInStorages(HttpServletRequest request);	
	StorageResponseDTO get(Long id, HttpServletRequest request);
	List<StorageBillReceivableResponseDTO> getStorageBillReceivables(Long id, HttpServletRequest request);
	StorageResponseDTO createStorage(StorageRequestDTO storageRequest, HttpServletRequest request);
	StorageResponseDTO updateStorage(StorageRequestDTO storageRequest, HttpServletRequest request);
	
	StorageResponseDTO checkIn(StorageRequestDTO storageRequest, HttpServletRequest request);
	StorageResponseDTO checkOut(StorageRequestDTO storageRequest, HttpServletRequest request);
//	ApiCustomResponse activateStorage(StorageRequestDTO storageRequest, HttpServletRequest request);
//	ApiCustomResponse deactivateStorage(StorageRequestDTO storageRequest, HttpServletRequest request);
	
	StorageBillReceivableResponseDTO createStorageBillReceivable(Long storageId, LocalDateTime startedAt, LocalDateTime endedAt, String billingType, double qty, double price, double discount, int autoBilling, HttpServletRequest request);

	StorageCustomBillDetail showStorageCustomBillDetail(Long storageId, HttpServletRequest request);
}
