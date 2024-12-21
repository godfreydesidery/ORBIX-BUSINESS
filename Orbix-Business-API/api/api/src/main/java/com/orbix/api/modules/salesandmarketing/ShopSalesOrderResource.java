package com.orbix.api.modules.salesandmarketing;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.api.commons.PayCode;
import com.orbix.api.modules.inventoryandprocurement.ProductResponseDTO;
import com.orbix.api.modules.inventoryandprocurement.UomRequestDTO;
import com.orbix.api.modules.inventoryandprocurement.UomResponseDTO;
import com.orbix.api.modules.inventoryandprocurement.UomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class ShopSalesOrderResource {
	
	private final ShopSalesOrderService shopSalesOrderService;
	
	
	@GetMapping("/shop_sales_orders/get_all_pending_by_shop")
	public ResponseEntity<List<ShopSalesOrderResponseDTO>>getAllPendingByShop(
			@RequestParam(name = "shop_id") Long shopId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(shopSalesOrderService.getAllPendingShopSalesOrders(shopId, request));
	}
	
	@GetMapping("/shop_sales_orders/get")
	public ResponseEntity<ShopSalesOrderResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(shopSalesOrderService.get(id, request));		
	}
	
	@PostMapping("/shop_sales_orders/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ShopSalesOrderResponseDTO>create(
			@RequestBody ShopSalesOrderRequestDTO shopSalesOrderRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shop_sales_orders/create").toUriString());
		return ResponseEntity.created(uri).body(shopSalesOrderService.createShopSalesOrder(shopSalesOrderRequest, request));
	}
	
	
	@PostMapping("/shop_sales_orders/create_detail")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public void createDetail(
			@RequestBody ShopSalesOrderDetailRequestDTO shopSalesOrderDetailRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/shop_sales_orders/create").toUriString());
		shopSalesOrderService.createShopSalesOrderDetail(shopSalesOrderDetailRequest, request);
	}
	
	@GetMapping("/shop_sales_orders/remove_detail")
	public void removeDetail(
			@RequestParam(name = "shop_sales_order_id") Long shopSalesOrderId,
			@RequestParam(name = "shop_sales_order_detail_id") Long shopSalesOrderDetailId,
			HttpServletRequest request){		
		shopSalesOrderService.removeShopOrderDetail(shopSalesOrderDetailId, shopSalesOrderId, request);		
	}
	
	@PostMapping("/shop_sales_orders/confirm")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean confirm(
			@RequestParam(name = "shop_sales_order_id") Long shopOrderId,
			@RequestParam(name = "pay_code") PayCode payCode,
			@RequestParam(name = "pay_ref_no") String payRefNo,
			HttpServletRequest request){		
		return shopSalesOrderService.confirmShopSalesOrder(shopOrderId, payCode, payRefNo,  request);
	}
	
	@PostMapping("/shop_sales_orders/cancel")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean cancel(
			@RequestParam(name = "shop_sales_order_id") Long shopOrderId,
			HttpServletRequest request){		
		return shopSalesOrderService.cancelShopSalesOrder(shopOrderId, request);
	}
	
}
