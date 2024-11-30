package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface ProductService {
	List<ProductResponseDTO> getAllProductes(HttpServletRequest request);
	ProductResponseDTO get(Long id, HttpServletRequest request);
	ProductResponseDTO createProduct(ProductRequestDTO product, HttpServletRequest request);
	ProductResponseDTO updateProduct(ProductRequestDTO product, HttpServletRequest request);
	ApiCustomResponse activateProduct(ProductRequestDTO product, HttpServletRequest request);
	ApiCustomResponse deactivateProduct(ProductRequestDTO product, HttpServletRequest request);
}
