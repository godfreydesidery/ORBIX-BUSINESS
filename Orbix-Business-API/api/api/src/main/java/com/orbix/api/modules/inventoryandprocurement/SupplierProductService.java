package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface SupplierProductService {
	List<SupplierProductResponseDTO> getAllSupplierProductsByBranch(Long supplierId, HttpServletRequest request);
	SupplierProductResponseDTO get(Long id, Long supplierId, HttpServletRequest request);
	SupplierProductResponseDTO getProductInSupplier(Long productId, Long supplierId, HttpServletRequest request);
	SupplierProductResponseDTO createSupplierProduct(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	SupplierProductResponseDTO updateSupplierProduct(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	SupplierProductResponseDTO adjustSupplierStock(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	ApiCustomResponse activateSupplierProduct(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	ApiCustomResponse deactivateSupplierProduct(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	
	List<ProductResponseDTO> getProductsBySupplierAndName(Long supplierId, String productName);
}
