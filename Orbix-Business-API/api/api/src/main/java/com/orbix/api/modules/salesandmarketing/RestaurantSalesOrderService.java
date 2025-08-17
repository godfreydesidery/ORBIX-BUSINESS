package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.PayCode;

public interface RestaurantSalesOrderService {
	List<RestaurantSalesOrderResponseDTO> getAllRestaurantSalesOrders(Long restaurantId, HttpServletRequest request);
	List<RestaurantSalesOrderResponseDTO> getAllPendingRestaurantSalesOrders(Long restaurantId, HttpServletRequest request);
	RestaurantSalesOrderResponseDTO get(Long id, HttpServletRequest request);
	RestaurantSalesOrderResponseDTO createRestaurantSalesOrder(RestaurantSalesOrderRequestDTO restaurantSalesOrder, HttpServletRequest request);
	RestaurantSalesOrderResponseDTO updateRestaurantSalesOrder(RestaurantSalesOrderRequestDTO restaurantSalesOrder, HttpServletRequest request);
//	ApiCustomResponse activateDineable(DineableRequestDTO dineable, HttpServletRequest request);
//	ApiCustomResponse deactivateDineable(DineableRequestDTO dineable, HttpServletRequest request);
	
	List<RestaurantSalesOrderDetailResponseDTO> getAllRestaurantSalesOrderDetails(Long salesOrderId, HttpServletRequest request);
	
	void createRestaurantSalesOrderDetail(RestaurantSalesOrderDetailRequestDTO restaurantSalesOrderDetail, HttpServletRequest request);
	
	void removeRestaurantOrderDetail(Long restaurantOrderDetailId, Long restaurantOrderId, HttpServletRequest request);
	
	boolean confirmRestaurantSalesOrder(Long restaurantOrderId, PayCode payCode, String payRefNo, HttpServletRequest request);
	boolean cancelRestaurantSalesOrder(Long restaurantOrderId, HttpServletRequest request);
}
