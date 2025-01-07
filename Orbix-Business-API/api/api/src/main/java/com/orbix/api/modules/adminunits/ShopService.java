package com.orbix.api.modules.adminunits;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface ShopService {
	List<ShopResponseDTO> getAllShops(HttpServletRequest request);
	ShopResponseDTO get(Long id, HttpServletRequest request);
	ShopResponseDTO createShop(ShopRequestDTO shop, HttpServletRequest request);
	ShopResponseDTO updateShop(ShopRequestDTO shop, HttpServletRequest request);
	ApiCustomResponse activateShop(ShopRequestDTO shop, HttpServletRequest request);
	ApiCustomResponse deactivateShop(ShopRequestDTO shop, HttpServletRequest request);
	
	
	List<ShopResponseDTO> getBranchAvailableShopsByUser(HttpServletRequest request);
	ShopResponseDTO getSelectedShop(Long id, HttpServletRequest request);
	
	List<ShopResponseDTO> getBranchShops(HttpServletRequest request);
}
