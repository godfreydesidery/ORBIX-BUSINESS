package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.PayCode;

public interface LpoService {
	List<LpoResponseDTO> getAllLpos(HttpServletRequest request);
	List<LpoResponseDTO> getAllPendingLpos(HttpServletRequest request);
	List<LpoResponseDTO> getAllVisibleLposByBranch(HttpServletRequest request);
	LpoResponseDTO get(Long id, HttpServletRequest request);
	LpoResponseDTO createLpo(LpoRequestDTO lpoRequest, HttpServletRequest request);
	LpoResponseDTO updateLpo(LpoRequestDTO lpoRequest, HttpServletRequest request);
//	ApiCustomResponse activateProduct(ProductRequestDTO product, HttpServletRequest request);
//	ApiCustomResponse deactivateProduct(ProductRequestDTO product, HttpServletRequest request);
	
	List<LpoDetailResponseDTO> getAllLpoDetails(Long lpoId, HttpServletRequest request);
	
	void createLpoDetail(LpoDetailRequestDTO lpoDetailRequest, HttpServletRequest request);
	
	void removeLpoDetail(Long lpoDetailId, Long lpoId, HttpServletRequest request);
	
	boolean approveLpo(Long lpoId, HttpServletRequest request);
	boolean archiveLpo(Long lpoId, HttpServletRequest request);
	boolean cancelLpo(Long lpoId, HttpServletRequest request);
}
