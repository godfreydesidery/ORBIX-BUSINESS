package com.orbix.api.modules.bond;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.api.commons.PageResponseDTO;

public interface BondItemTypeService {
	List<BondItemTypeResponseDTO> getAllBondItemTypes(HttpServletRequest request);	
	PageResponseDTO<BondItemTypeResponseDTO> getBondItemTypePage(int page, int size, String search, HttpServletRequest request);
	List<BondItemTypeResponseDTO> getAllCompanyActiveBondItemTypes(HttpServletRequest request);		
	BondItemTypeResponseDTO get(Long id, HttpServletRequest request);
	BondItemTypeResponseDTO createBondItemType(BondItemTypeRequestDTO bondItemTypeRequest, HttpServletRequest request);
	BondItemTypeResponseDTO updateBondItemType(BondItemTypeRequestDTO bondItemTypeRequest, HttpServletRequest request);
	ApiCustomResponse activateBondItemType(BondItemTypeRequestDTO bondItemTypeRequest, HttpServletRequest request);
	ApiCustomResponse deactivateBondItemType(BondItemTypeRequestDTO bondItemTypeRequest, HttpServletRequest request);
}
