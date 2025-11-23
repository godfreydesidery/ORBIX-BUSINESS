package com.orbix.api.modules.warehouse;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface StorageGoodReleaseService {
	StorageGoodReleaseDetail showStorageGoodReleaseDetail(Long storageId, HttpServletRequest request);
	StorageGoodReleaseResponseDTO createStorageGoodRelease(StorageGoodReleaseRequestDTO storageGoodreleaseRequest, HttpServletRequest request);
	List<StorageGoodReleaseResponseDTO> getStorageGoodReleases(Long storageId, HttpServletRequest request);
	StorageGoodReleaseResponseDTO get(Long id, HttpServletRequest request);
}
