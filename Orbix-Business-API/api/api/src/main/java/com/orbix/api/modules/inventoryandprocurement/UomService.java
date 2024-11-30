package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface UomService {
	List<UomResponseDTO> getAllUomes(HttpServletRequest request);
	UomResponseDTO get(Long id, HttpServletRequest request);
	UomResponseDTO createUom(UomRequestDTO uom, HttpServletRequest request);
	UomResponseDTO updateUom(UomRequestDTO uom, HttpServletRequest request);
	ApiCustomResponse activateUom(UomRequestDTO uom, HttpServletRequest request);
	ApiCustomResponse deactivateUom(UomRequestDTO uom, HttpServletRequest request);
}
