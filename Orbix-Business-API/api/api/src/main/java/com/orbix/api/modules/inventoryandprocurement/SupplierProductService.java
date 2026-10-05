package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.api.commons.PageResponseDTO;

public interface SupplierProductService {
	List<SupplierProductResponseDTO> getAllSupplierProductsByBranch(Long supplierId, HttpServletRequest request);
	SupplierProductResponseDTO get(Long id, Long supplierId, HttpServletRequest request);
	SupplierProductResponseDTO getSupplierProduct(Long supplierId, Long productId, HttpServletRequest request);
	SupplierProductResponseDTO getProductInSupplier(Long productId, Long supplierId, HttpServletRequest request);
	SupplierProductResponseDTO createSupplierProduct(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	SupplierProductResponseDTO updateSupplierProduct(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	SupplierProductResponseDTO adjustSupplierStock(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	ApiCustomResponse activateSupplierProduct(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	ApiCustomResponse deactivateSupplierProduct(SupplierProductRequestDTO supplierProductRequest, HttpServletRequest request);
	
	List<ProductResponseDTO> getProductsBySupplierAndName(Long supplierId, String productName);

	PageResponseDTO<SupplierProductResponseDTO> getSupplierProductPageByBranch(Long supplierId, int page, int size, String search, HttpServletRequest request);
}
