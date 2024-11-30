package com.orbix.api.modules.adminunits;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface ShopService {
	List<ShopResponseDTO> getAllShopes(HttpServletRequest request);
	ShopResponseDTO get(Long id, HttpServletRequest request);
	ShopResponseDTO createShop(ShopRequestDTO shop, HttpServletRequest request);
	ShopResponseDTO updateShop(ShopRequestDTO shop, HttpServletRequest request);
	ApiCustomResponse activateShop(ShopRequestDTO shop, HttpServletRequest request);
	ApiCustomResponse deactivateShop(ShopRequestDTO shop, HttpServletRequest request);
}
