package com.orbix.api.modules.warehouse;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface GoodTypeService {
	List<GoodTypeResponseDTO> getAllGoodTypes(HttpServletRequest request);	
	List<GoodTypeResponseDTO> getAllCompanyActiveGoodTypes(HttpServletRequest request);		
	GoodTypeResponseDTO get(Long id, HttpServletRequest request);
	GoodTypeResponseDTO createGoodType(GoodTypeRequestDTO goodTypeRequest, HttpServletRequest request);
	GoodTypeResponseDTO updateGoodType(GoodTypeRequestDTO goodTypeRequest, HttpServletRequest request);
	ApiCustomResponse activateGoodType(GoodTypeRequestDTO goodTypeRequest, HttpServletRequest request);
	ApiCustomResponse deactivateGoodType(GoodTypeRequestDTO goodTypeRequest, HttpServletRequest request);
}
