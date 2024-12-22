package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.PayCode;

public interface ShopSalesOrderService {
	List<ShopSalesOrderResponseDTO> getAllShopSalesOrders(Long shopId, HttpServletRequest request);
	List<ShopSalesOrderResponseDTO> getAllPendingShopSalesOrders(Long shopId, HttpServletRequest request);
	ShopSalesOrderResponseDTO get(Long id, HttpServletRequest request);
	ShopSalesOrderResponseDTO createShopSalesOrder(ShopSalesOrderRequestDTO shopSalesOrder, HttpServletRequest request);
	ShopSalesOrderResponseDTO updateShopSalesOrder(ShopSalesOrderRequestDTO shopSalesOrder, HttpServletRequest request);
//	ApiCustomResponse activateProduct(ProductRequestDTO product, HttpServletRequest request);
//	ApiCustomResponse deactivateProduct(ProductRequestDTO product, HttpServletRequest request);
	
	List<ShopSalesOrderDetailResponseDTO> getAllShopSalesOrderDetails(Long salesOrderId, HttpServletRequest request);
	
	void createShopSalesOrderDetail(ShopSalesOrderDetailRequestDTO shopSalesOrderDetail, HttpServletRequest request);
	
	void removeShopOrderDetail(Long shopOrderDetailId, Long shopOrderId, HttpServletRequest request);
	
	boolean confirmShopSalesOrder(Long shopOrderId, PayCode payCode, String payRefNo, HttpServletRequest request);
	boolean cancelShopSalesOrder(Long shopOrderId, HttpServletRequest request);
}
