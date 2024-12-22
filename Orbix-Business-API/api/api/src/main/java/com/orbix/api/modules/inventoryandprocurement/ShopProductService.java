package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.modules.adminunits.Shop;

public interface ShopProductService {
	List<ShopProductResponseDTO> getAllShopProducts(Long shopId, HttpServletRequest request);
	ShopProductResponseDTO get(Long id, Long shopId, HttpServletRequest request);
	ShopProductResponseDTO getProductInShop(Long productId, Long shopId, HttpServletRequest request);
	ShopProductResponseDTO createShopProduct(ShopProductRequestDTO shopProductRequest, HttpServletRequest request);
	ShopProductResponseDTO updateShopProduct(ShopProductRequestDTO shopProductRequest, HttpServletRequest request);
	ShopProductResponseDTO adjustShopStock(ShopProductRequestDTO shopProductRequest, HttpServletRequest request);
	ApiCustomResponse activateShopProduct(ShopProductRequestDTO shopProductRequest, HttpServletRequest request);
	ApiCustomResponse deactivateShopProduct(ShopProductRequestDTO shopProductRequest, HttpServletRequest request);
	
	List<ProductResponseDTO> getProductsByShopAndName(Long shopId, String productName);
}
