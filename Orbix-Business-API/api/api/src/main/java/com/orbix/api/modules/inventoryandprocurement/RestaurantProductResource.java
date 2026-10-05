package com.orbix.api.modules.inventoryandprocurement;

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

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.api.commons.PageResponseDTO;
import com.orbix.api.modules.audit.Audited;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantProductResource {
	private final RestaurantProductRepository restaurantProductRepository;

	private final RestaurantProductService restaurantProductService;

	@GetMapping("/restaurant_products")
	public ResponseEntity<List<RestaurantProductResponseDTO>> getAllRestaurantProducts(@RequestParam(name = "restaurant_id") Long restaurantId,
			HttpServletRequest request) {
		return ResponseEntity.ok().body(restaurantProductService.getAllRestaurantProducts(restaurantId, request));
	}

	@GetMapping("/restaurant_products/get")
	public ResponseEntity<RestaurantProductResponseDTO> get(@RequestParam(name = "id") Long id,
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok().body(restaurantProductService.get(id, restaurantId, request));
	}

	@GetMapping("/restaurant_products/get_product_in_restaurant")
	public ResponseEntity<RestaurantProductResponseDTO> getProductInRestaurant(@RequestParam(name = "product_id") Long productId,
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok().body(restaurantProductService.getProductInRestaurant(productId, restaurantId, request));
	}

	@PostMapping("/restaurant_products/create")
	@Audited(category = "INVENTORY", action = "RECORD_CREATED", entityType = "RestaurantProduct", entityRef = "result.productCode", summary = "Created restaurant product {ref}")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantProductResponseDTO> create(@RequestBody RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_products/create").toUriString());
		return ResponseEntity.created(uri).body(restaurantProductService.createRestaurantProduct(restaurantProductRequest, request));
	}

	@PostMapping("/restaurant_products/update")
	@Audited(category = "INVENTORY", action = "RECORD_UPDATED", entityType = "RestaurantProduct", entityRef = "result.productCode", summary = "Updated restaurant product {ref}", changeOf = RestaurantProduct.class, changeId = "restaurantProductRequest.id", changeQuery = "select p.id from RestaurantProduct p where p.restaurant.id = ?1 and p.product.id = ?2", changeKeys = {"restaurantProductRequest.restaurantId", "restaurantProductRequest.productId"}, changedFieldPattern = "(?i).*price.*", changedAction = "PRICE_CHANGED")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantProductResponseDTO> update(@RequestBody RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_products/update").toUriString());
		return ResponseEntity.created(uri).body(restaurantProductService.updateRestaurantProduct(restaurantProductRequest, request));
	}

	@PostMapping("/restaurant_products/adjust_stock")
	@Audited(category = "INVENTORY", action = "STOCK_ADJUSTED", entityType = "RestaurantProduct", entityRef = "result.productCode", summary = "Adjusted the stock of {result.productCode} {result.productName} to {result.currentStock}", details = {"reason=restaurantProductRequest.reason"}, changeOf = RestaurantProduct.class, changeId = "restaurantProductRequest.id", changeQuery = "select p.id from RestaurantProduct p where p.restaurant.id = ?1 and p.product.id = ?2", changeKeys = {"restaurantProductRequest.restaurantId", "restaurantProductRequest.productId"})
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantProductResponseDTO> adjustStock(@RequestBody RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_products/adjust_stock").toUriString());
		return ResponseEntity.created(uri).body(restaurantProductService.adjustRestaurantStock(restaurantProductRequest, request));
	}
	
	@PostMapping("/restaurant_products/add_stock")
	@Audited(category = "INVENTORY", action = "STOCK_ADJUSTED", entityType = "RestaurantProduct", entityRef = "result.productCode", summary = "Added {restaurantProductRequest.qty} to the stock of {result.productCode} {result.productName}", details = {"qty=restaurantProductRequest.qty", "reason=restaurantProductRequest.reason"}, changeOf = RestaurantProduct.class, changeId = "restaurantProductRequest.id", changeQuery = "select p.id from RestaurantProduct p where p.restaurant.id = ?1 and p.product.id = ?2", changeKeys = {"restaurantProductRequest.restaurantId", "restaurantProductRequest.productId"})
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantProductResponseDTO> addStock(@RequestBody RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_products/adjust_stock").toUriString());
		return ResponseEntity.created(uri).body(restaurantProductService.addRestaurantStock(restaurantProductRequest, request));
	}
	
	@PostMapping("/restaurant_products/deduct_stock")
	@Audited(category = "INVENTORY", action = "STOCK_ADJUSTED", entityType = "RestaurantProduct", entityRef = "result.productCode", summary = "Deducted {restaurantProductRequest.qty} from the stock of {result.productCode} {result.productName}", details = {"qty=restaurantProductRequest.qty", "reason=restaurantProductRequest.reason"}, changeOf = RestaurantProduct.class, changeId = "restaurantProductRequest.id", changeQuery = "select p.id from RestaurantProduct p where p.restaurant.id = ?1 and p.product.id = ?2", changeKeys = {"restaurantProductRequest.restaurantId", "restaurantProductRequest.productId"})
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<RestaurantProductResponseDTO> deductStock(@RequestBody RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_products/deduct_stock").toUriString());
		return ResponseEntity.created(uri).body(restaurantProductService.deductRestaurantStock(restaurantProductRequest, request));
	}

	@PostMapping("/restaurant_products/activate")
	@Audited(category = "INVENTORY", action = "RECORD_ACTIVATED", entityType = "RestaurantProduct", entityId = "restaurantProductRequest.id", summary = "Activated restaurant product {ref}", changeOf = RestaurantProduct.class, changeId = "restaurantProductRequest.id")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse> activate(@RequestBody RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_products/activate").toUriString());
		return ResponseEntity.created(uri).body(restaurantProductService.activateRestaurantProduct(restaurantProductRequest, request));
	}

	@PostMapping("/restaurant_products/deactivate")
	@Audited(category = "INVENTORY", action = "RECORD_DEACTIVATED", entityType = "RestaurantProduct", entityId = "restaurantProductRequest.id", summary = "Deactivated restaurant product {ref}", changeOf = RestaurantProduct.class, changeId = "restaurantProductRequest.id")
	// @PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse> deactivate(@RequestBody RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/orbix-business-api/restaurant_products/deactivate").toUriString());
		return ResponseEntity.created(uri).body(restaurantProductService.activateRestaurantProduct(restaurantProductRequest, request));
	}

	@GetMapping("/restaurant_products/get_stock_by_restaurant")
	public ResponseEntity<List<RestaurantProductResponseDTO>> getAllRestaurantProductsByRestaurant(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok().body(restaurantProductService.getAllRestaurantProducts(restaurantId, request));
	}

	@GetMapping("/restaurant_products/get_stock_by_restaurant_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<RestaurantProductResponseDTO>>getStockByRestaurantPage(
			@RequestParam(name = "restaurant_id") Long restaurantId,
			@RequestParam(name = "stock", defaultValue = "") String stock,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(restaurantProductService.getRestaurantProductStockPage(restaurantId, stock, page, size, search, request));
	}


	@GetMapping("/restaurant_products/get_under_stock_by_restaurant")
	public ResponseEntity<List<RestaurantProductResponseDTO>> getUnderstockRestaurantProductsByRestaurant(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok().body(restaurantProductService.getUnderstockRestaurantProducts(restaurantId, request));
	}

	@GetMapping("/restaurant_products/get_out_of_stock_by_restaurant")
	public ResponseEntity<List<RestaurantProductResponseDTO>> getOutofstockRestaurantProductsByRestaurant(
			@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return ResponseEntity.ok().body(restaurantProductService.getOutofstockRestaurantProducts(restaurantId, request));
	}

	@GetMapping("/restaurant_products/get_products_by_restaurant_containing")
	public ResponseEntity<List<ProductResponseDTO>> getAllRestaurantProductsByRestaurantContaining(
			@RequestParam(name = "restaurant_id") Long restaurantId,
			@RequestParam(name = "product_name_like") String productNameLike, HttpServletRequest request) {

		return ResponseEntity.ok().body(restaurantProductService.getProductsByRestaurantAndName(restaurantId, productNameLike));

	}

	@GetMapping("/restaurant_products/get_check_under_stock_by_restaurant")
	public long checkUnderStockByRestaurant(@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return restaurantProductService.checkUnderstockByRestaurant(restaurantId, request);
	}

	@GetMapping("/restaurant_products/get_check_out_of_stock_by_restaurant")
	public long checkOutofStockByRestaurant(@RequestParam(name = "restaurant_id") Long restaurantId, HttpServletRequest request) {
		return restaurantProductService.checkOutofstockByRestaurant(restaurantId, request);
	}
}
