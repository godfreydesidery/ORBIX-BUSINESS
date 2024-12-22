package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface SaleService {
	public Sale createSale(SaleRequestDTO saleRequest, HttpServletRequest request);
	
//	List<SaleResponseDTO> getAllSalesBy(Long shopId, HttpServletRequest request);
//	List<ShopSalesOrderResponseDTO> getAllPendingShopSalesOrders(Long shopId, HttpServletRequest request);
//	ShopSalesOrderResponseDTO get(Long id, HttpServletRequest request);
//	ShopSalesOrderResponseDTO createShopSalesOrder(ShopSalesOrderRequestDTO shopSalesOrder, HttpServletRequest request);
//	ShopSalesOrderResponseDTO updateShopSalesOrder(ShopSalesOrderRequestDTO shopSalesOrder, HttpServletRequest request);
}
