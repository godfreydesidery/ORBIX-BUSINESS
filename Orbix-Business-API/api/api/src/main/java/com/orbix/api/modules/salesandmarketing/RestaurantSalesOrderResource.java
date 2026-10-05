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
import com.orbix.api.api.commons.PageResponseDTO;
import com.orbix.api.modules.audit.Audited;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantSalesOrderResource {
	private final RestaurantSalesOrderService restaurantSalesOrderService;

	@GetMapping("/restaurant_sales_orders/get_all_pending_by_restaurant")
	public ResponseEntity<List<RestaurantSalesOrderResponseDTO>> getAllPendingByRestaurant(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok()
				.body(restaurantSalesOrderService.getAllPendingRestaurantSalesOrders(restaurantId, request));
	}

	@GetMapping("/restaurant_sales_orders/get_all_pending_by_restaurant_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<RestaurantSalesOrderResponseDTO>>getAllPendingByRestaurantPage(
			@RequestParam(name = "restaurant_id") Long restaurantId,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantSalesOrderService.getPendingRestaurantSalesOrderPage(restaurantId, page, size, search, request));
	}

	@GetMapping("/restaurant_sales_orders/get")
	public ResponseEntity<RestaurantSalesOrderResponseDTO> get(Long id, HttpServletRequest request) {
		return ResponseEntity.ok().body(restaurantSalesOrderService.get(id, request));
	}

	@PostMapping("/restaurant_sales_orders/create")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantSalesOrderResponseDTO> create(
			@RequestBody RestaurantSalesOrderRequestDTO restaurantSalesOrderRequest, HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_sales_orders/create").toUriString());
		return ResponseEntity.created(uri)
				.body(restaurantSalesOrderService.createRestaurantSalesOrder(restaurantSalesOrderRequest, request));
	}

	@PostMapping("/restaurant_sales_orders/create_detail")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public void createDetail(@RequestBody RestaurantSalesOrderDetailRequestDTO restaurantSalesOrderDetailRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_sales_orders/create").toUriString());
		restaurantSalesOrderService.createRestaurantSalesOrderDetail(restaurantSalesOrderDetailRequest, request);
	}

	@GetMapping("/restaurant_sales_orders/remove_detail")
	public void removeDetail(@RequestParam(name = "restaurant_sales_order_id") Long restaurantSalesOrderId,
			@RequestParam(name = "restaurant_sales_order_detail_id") Long restaurantSalesOrderDetailId,
			HttpServletRequest request) {
		restaurantSalesOrderService.removeRestaurantOrderDetail(restaurantSalesOrderDetailId, restaurantSalesOrderId,
				request);
	}

	@PostMapping("/restaurant_sales_orders/confirm")
	@Audited(category = "SALES", action = "SALES_ORDER_CONFIRMED", entityType = "RestaurantSalesOrder", entityId = "restaurantOrderId", summary = "Confirmed restaurant sales order {ref} ({payCode}, ref {payRefNo})", details = {"payCode", "payRefNo"}, changeOf = RestaurantSalesOrder.class, changeId = "restaurantOrderId")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean confirm(@RequestParam(name = "restaurant_sales_order_id") Long restaurantOrderId,
			@RequestParam(name = "pay_code") PayCode payCode, @RequestParam(name = "pay_ref_no") String payRefNo,
			HttpServletRequest request) {
		return restaurantSalesOrderService.confirmRestaurantSalesOrder(restaurantOrderId, payCode, payRefNo, request);
	}

	@PostMapping("/restaurant_sales_orders/cancel")
	@Audited(category = "SALES", action = "SALES_ORDER_CANCELLED", entityType = "RestaurantSalesOrder", entityId = "restaurantOrderId", summary = "Cancelled restaurant sales order {ref}", changeOf = RestaurantSalesOrder.class, changeId = "restaurantOrderId")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean cancel(@RequestParam(name = "restaurant_sales_order_id") Long restaurantOrderId,
			HttpServletRequest request) {
		return restaurantSalesOrderService.cancelRestaurantSalesOrder(restaurantOrderId, request);
	}
}
