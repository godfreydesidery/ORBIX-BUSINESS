package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface GrnService {
	List<GrnResponseDTO> getAllGrns(HttpServletRequest request);
	List<GrnResponseDTO> getAllPendingGrns(HttpServletRequest request);
	List<GrnResponseDTO> getAllVisibleGrnsByBranch(HttpServletRequest request);
	GrnResponseDTO get(Long id, HttpServletRequest request);
	GrnResponseDTO createGrn(GrnRequestDTO grnRequest, HttpServletRequest request);
	GrnResponseDTO updateGrn(GrnRequestDTO grnRequest, HttpServletRequest request);
//	ApiCustomResponse activateProduct(ProductRequestDTO product, HttpServletRequest request);
//	ApiCustomResponse deactivateProduct(ProductRequestDTO product, HttpServletRequest request);
	
	List<GrnDetailResponseDTO> getAllGrnDetails(Long grnId, HttpServletRequest request);
	
	void createGrnDetail(GrnDetailRequestDTO grnDetailRequest, HttpServletRequest request);
	
	void removeGrnDetail(Long grnDetailId, Long grnId, HttpServletRequest request);
	
	boolean approveGrn(Long grnId, HttpServletRequest request);
	boolean cancelGrn(Long grnId, HttpServletRequest request);
}
