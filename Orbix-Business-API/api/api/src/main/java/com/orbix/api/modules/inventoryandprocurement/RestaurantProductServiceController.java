package com.orbix.api.modules.inventoryandprocurement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.RestaurantRepository;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RestaurantProductServiceController implements RestaurantProductService {
	
	private final CompanyRepository companyRepository;
	private final RestaurantRepository restaurantRepository;
	private final ProductRepository productRepository;
	private final RestaurantProductRepository restaurantProductRepository;
	private final RestaurantProductLogRepository restaurantProductLogRepository;
	private final UserService userService;
	private final DayService dayService;

	@Override
	public List<RestaurantProductResponseDTO> getAllRestaurantProducts(Long restaurantId, HttpServletRequest request) {
	    // Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    
	    // Fetch all restaurant products
	    List<RestaurantProduct> restaurantProducts = restaurantProductRepository.findAllByRestaurant(restaurant);
	    
	    // Map to response DTOs using streams
	    return restaurantProducts.stream()
	            .map(this::restaurantProductResponseDTOMapper)
	            .collect(Collectors.toList());
	}
	
	@Override
	public List<RestaurantProductResponseDTO> getUnderstockRestaurantProducts(Long restaurantId, HttpServletRequest request) {
	    // Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    
	    // Fetch all restaurant products
	    List<RestaurantProduct> restaurantProducts = restaurantProductRepository.findProductsWithLowStock(restaurant.getId());

	    // Map to response DTOs using streams
	    return restaurantProducts.stream()
	            .map(this::restaurantProductResponseDTOMapper)
	            .collect(Collectors.toList());
	}
	
	@Override
	public List<RestaurantProductResponseDTO> getOutofstockRestaurantProducts(Long restaurantId, HttpServletRequest request) {
	    // Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    
	    // Fetch all restaurant products
	    List<RestaurantProduct> restaurantProducts = restaurantProductRepository.findByRestaurantAndCurrentStockLessThanEqual(restaurant, 0);

	    // Map to response DTOs using streams
	    return restaurantProducts.stream()
	            .map(this::restaurantProductResponseDTOMapper)
	            .collect(Collectors.toList());
	}

	@Override
	public RestaurantProductResponseDTO get(Long id, Long restaurantId, HttpServletRequest request) {
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	 // Validate and fetch the restaurant
	    RestaurantProduct restaurantProduct = restaurantProductRepository.findByIdAndRestaurant(id, restaurant)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Product not found"));
	    
	    return this.restaurantProductResponseDTOMapper(restaurantProduct);
	}
	
	@Override
	public RestaurantProductResponseDTO getProductInRestaurant(Long productId, Long restaurantId, HttpServletRequest request) {
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	 // Validate and fetch the restaurant
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Product not found"));
	    
	    RestaurantProduct restaurantProduct = restaurantProductRepository.findByProductAndRestaurant(product, restaurant)
                .orElseThrow(() -> new NotFoundException("Product not found in restaurant"));
	    
	    return this.restaurantProductResponseDTOMapper(restaurantProduct);
	}

	@Override
	public RestaurantProductResponseDTO createRestaurantProduct(RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		
		Long restaurantId = restaurantProductRequest.getRestaurantId();
		Long productId = restaurantProductRequest.getProductId();
		double vatRate = restaurantProductRequest.getVatRate();
		////////////////
		double costPriceVatIncl = restaurantProductRequest.getCostPriceVatIncl();
		double costPriceVatExcl = restaurantProductRequest.getCostPriceVatExcl();
		double sellingPriceVatIncl = restaurantProductRequest.getSellingPriceVatIncl();
		double sellingPriceVatExcl = restaurantProductRequest.getSellingPriceVatExcl();
		/////////////////
		double currentStock = restaurantProductRequest.getCurrentStock();
		double minStock = restaurantProductRequest.getMinStock();
		double maxStock = restaurantProductRequest.getMaxStock();
		double defaultReorderLevel = restaurantProductRequest.getDefaultReorderLevel();
		double defaultReorderQty = restaurantProductRequest.getDefaultReorderQty();
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Product not found"));
	   
	    if (restaurantProductRepository.existsByRestaurantAndProduct(restaurant, product)) {
	        throw new InvalidOperationException(
	            String.format("Product '%s' already exists in restaurant '%s'", product.getName(), restaurant.getName())
	        );
	    }
		
		
		
		
		RestaurantProduct restaurantProduct = new RestaurantProduct();
		restaurantProduct.setRestaurant(restaurant);
	    restaurantProduct.setProduct(product);
	    restaurantProduct.setCurrentStock(currentStock);
	    restaurantProduct.setMinStock(minStock);
	    restaurantProduct.setMaxStock(maxStock);
	    restaurantProduct.setVatRate(vatRate);
	    restaurantProduct.setCostPriceVatIncl(costPriceVatIncl);
	    restaurantProduct.setCostPriceVatExcl(costPriceVatExcl);
	    restaurantProduct.setSellingPriceVatIncl(sellingPriceVatIncl);
	    restaurantProduct.setSellingPriceVatExcl(sellingPriceVatExcl);
	    restaurantProduct.setDefaultReorderLevel(defaultReorderLevel);
	    restaurantProduct.setDefaultReorderQty(defaultReorderQty);
	    
	    restaurantProduct.setActive(true);
	    
	    restaurantProduct.setCreatedByUser(userService.getUser(request));
	    restaurantProduct.setCreatedDateTime(dayService.getTimeStamp());
	    
	    restaurantProduct = restaurantProductRepository.save(restaurantProduct);
	    
	    
	    
	    ////Update RestaurantProduct log for stock card
	    
	    this.createRestaurantProductLog(restaurant, product, currentStock, 0, currentStock, userService.getUser(request), dayService.getTimeStamp(), "Opening stock");

		
		
	    return this.restaurantProductResponseDTOMapper(restaurantProduct);
	}

	@Override
	public RestaurantProductResponseDTO updateRestaurantProduct(RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		
		
		
		Long restaurantId = restaurantProductRequest.getRestaurantId();
		Long productId = restaurantProductRequest.getProductId();
		double vatRate = restaurantProductRequest.getVatRate();
		////////////////
		double costPriceVatIncl = restaurantProductRequest.getCostPriceVatIncl();
		double costPriceVatExcl = restaurantProductRequest.getCostPriceVatExcl();
		double sellingPriceVatIncl = restaurantProductRequest.getSellingPriceVatIncl();
		double sellingPriceVatExcl = restaurantProductRequest.getSellingPriceVatExcl();
		///////////////// do not update stock
		double minStock = restaurantProductRequest.getMinStock();
		double maxStock = restaurantProductRequest.getMaxStock();
		double defaultReorderLevel = restaurantProductRequest.getDefaultReorderLevel();
		double defaultReorderQty = restaurantProductRequest.getDefaultReorderQty();
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Product not found"));
	    
	    Optional<RestaurantProduct> restaurantProduct_ = restaurantProductRepository.findByRestaurantAndProduct(restaurant, product);
	    if(restaurantProduct_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Product '%s' does not exist in restaurant '%s'", product.getName(), restaurant.getName())
		        );
	    }
	   
		RestaurantProduct restaurantProduct = restaurantProduct_.get();
		
	    restaurantProduct.setMinStock(minStock);
	    restaurantProduct.setMaxStock(maxStock);
	    restaurantProduct.setVatRate(vatRate);
	    restaurantProduct.setCostPriceVatIncl(costPriceVatIncl);
	    restaurantProduct.setCostPriceVatExcl(costPriceVatExcl);
	    restaurantProduct.setSellingPriceVatIncl(sellingPriceVatIncl);
	    restaurantProduct.setSellingPriceVatExcl(sellingPriceVatExcl);
	    restaurantProduct.setDefaultReorderLevel(defaultReorderLevel);
	    restaurantProduct.setDefaultReorderQty(defaultReorderQty);
	    
	    restaurantProduct.setActive(true);
	    
	    restaurantProduct = restaurantProductRepository.save(restaurantProduct);
	    
	    
	    
	    return this.restaurantProductResponseDTOMapper(restaurantProduct);
	}
	
	
	@Override
	public RestaurantProductResponseDTO adjustRestaurantStock(RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		
		
		
		Long restaurantId = restaurantProductRequest.getRestaurantId();
		Long productId = restaurantProductRequest.getProductId();
		
		/////////////////
		double currentStock = restaurantProductRequest.getCurrentStock();
		
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Product not found"));
	    
	    Optional<RestaurantProduct> restaurantProduct_ = restaurantProductRepository.findByRestaurantAndProduct(restaurant, product);
	    if(restaurantProduct_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Product '%s' does not exist in restaurant '%s'", product.getName(), restaurant.getName())
		        );
	    }
	   
		RestaurantProduct restaurantProduct = restaurantProduct_.get();
		boolean stockChanged = false;
		if(restaurantProduct.getCurrentStock() != currentStock) {
			stockChanged = true;
		}
	    restaurantProduct.setCurrentStock(currentStock);
	    
	    restaurantProduct.setActive(true);
	    
	    restaurantProduct = restaurantProductRepository.save(restaurantProduct);
	    
	    //Update RestaurantProduct log for stock card
	    
	    if(stockChanged == true)this.createRestaurantProductLog(restaurant, product, currentStock, 0, currentStock, userService.getUser(request), dayService.getTimeStamp(), restaurantProductRequest.getReason());

	    
	    return this.restaurantProductResponseDTOMapper(restaurantProduct);
	}
	
	@Override
	public RestaurantProductResponseDTO addRestaurantStock(RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		
		
		
		Long restaurantId = restaurantProductRequest.getRestaurantId();
		Long productId = restaurantProductRequest.getProductId();
		
		/////////////////
		
		
		double qty = restaurantProductRequest.getQty();
		
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Product not found"));
	    
	    Optional<RestaurantProduct> restaurantProduct_ = restaurantProductRepository.findByRestaurantAndProduct(restaurant, product);
	    if(restaurantProduct_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Product '%s' does not exist in restaurant '%s'", product.getName(), restaurant.getName())
		        );
	    }
	   
		RestaurantProduct restaurantProduct = restaurantProduct_.get();
		boolean stockChanged = false;
		double currentStock = restaurantProduct.getCurrentStock() + qty;
		if(restaurantProduct.getCurrentStock() != currentStock) {
			stockChanged = true;
		}
	    restaurantProduct.setCurrentStock(currentStock);
	    
	    restaurantProduct.setActive(true);
	    
	    restaurantProduct = restaurantProductRepository.save(restaurantProduct);
	    
	    //Update RestaurantProduct log for stock card
	    
	    if(stockChanged == true)this.createRestaurantProductLog(restaurant, product, qty, 0, currentStock, userService.getUser(request), dayService.getTimeStamp(), restaurantProductRequest.getReason());

	    
	    return this.restaurantProductResponseDTOMapper(restaurantProduct);
	}
	
	@Override
	public RestaurantProductResponseDTO deductRestaurantStock(RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		
		
		
		Long restaurantId = restaurantProductRequest.getRestaurantId();
		Long productId = restaurantProductRequest.getProductId();
		
		/////////////////
		
		
		double qty = restaurantProductRequest.getQty();
		
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Product not found"));
	    
	    Optional<RestaurantProduct> restaurantProduct_ = restaurantProductRepository.findByRestaurantAndProduct(restaurant, product);
	    if(restaurantProduct_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Product '%s' does not exist in restaurant '%s'", product.getName(), restaurant.getName())
		        );
	    }
	   
		RestaurantProduct restaurantProduct = restaurantProduct_.get();
		boolean stockChanged = false;
		double currentStock = restaurantProduct.getCurrentStock() - qty;
		if(restaurantProduct.getCurrentStock() != currentStock) {
			stockChanged = true;
		}
	    restaurantProduct.setCurrentStock(currentStock);
	    
	    restaurantProduct.setActive(true);
	    
	    restaurantProduct = restaurantProductRepository.save(restaurantProduct);
	    
	    //Update RestaurantProduct log for stock card
	    
	    if(stockChanged == true)this.createRestaurantProductLog(restaurant, product, 0, qty, currentStock, userService.getUser(request), dayService.getTimeStamp(), restaurantProductRequest.getReason());

	    
	    return this.restaurantProductResponseDTOMapper(restaurantProduct);
	}

	@Override
	public ApiCustomResponse activateRestaurantProduct(RestaurantProductRequestDTO restaurantProductRequest, HttpServletRequest request) {
		Optional<RestaurantProduct> restaurantProduct_ = restaurantProductRepository.findById(restaurantProductRequest.getId());		
		if(restaurantProduct_.isEmpty()) {
			throw new NotFoundException("Restaurant Product not found");
		}		
		if(restaurantProduct_.get().isActive() == false) {
			throw new InvalidOperationException("Restaurant Product already inactive");
		}
		restaurantProduct_.get().setActive(false);
		restaurantProductRepository.save(restaurantProduct_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Restaurant Product Deactivated successifully");
	}

	@Override
	public ApiCustomResponse deactivateRestaurantProduct(RestaurantProductRequestDTO restaurantProductRequest,
			HttpServletRequest request) {
		Optional<RestaurantProduct> restaurantProduct_ = restaurantProductRepository.findById(restaurantProductRequest.getId());		
		if(restaurantProduct_.isEmpty()) {
			throw new NotFoundException("Restaurant Product not found");
		}		
		if(restaurantProduct_.get().isActive() == true) {
			throw new InvalidOperationException("Restaurant Product already active");
		}
		restaurantProduct_.get().setActive(true);
		restaurantProductRepository.save(restaurantProduct_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Restaurant Product Activated successifully");
	}
	
	private RestaurantProductResponseDTO restaurantProductResponseDTOMapper(RestaurantProduct restaurantProduct) {
		RestaurantProductResponseDTO restaurantProductResponse = new RestaurantProductResponseDTO();
		
		restaurantProductResponse.setProductCode(restaurantProduct.getProduct().getCode());
		restaurantProductResponse.setProductName(restaurantProduct.getProduct().getName());
		restaurantProductResponse.setProductDescription(restaurantProduct.getProduct().getDescription());
		restaurantProductResponse.setBaseUom(restaurantProduct.getProduct().getBaseUom());
		
		// Convert entity ID to String
		restaurantProductResponse.setId(restaurantProduct.getId().toString());

		// Convert numerical values to String
		restaurantProductResponse.setCurrentStock(String.valueOf(restaurantProduct.getCurrentStock()));
		restaurantProductResponse.setMinStock(String.valueOf(restaurantProduct.getMinStock()));
		restaurantProductResponse.setMaxStock(String.valueOf(restaurantProduct.getMaxStock()));
		restaurantProductResponse.setDefaultReorderQty(String.valueOf(restaurantProduct.getDefaultReorderQty()));
		restaurantProductResponse.setDefaultReorderLevel(String.valueOf(restaurantProduct.getDefaultReorderLevel()));

		// Format vatRate and derive vatPercentage
		restaurantProductResponse.setVatRate(String.valueOf(restaurantProduct.getVatRate()));
		restaurantProductResponse.setVatPercentage(String.format("%.2f%%", restaurantProduct.getVatRate() * 100));

		// Convert prices to String
		restaurantProductResponse.setCostPriceVatIncl(String.valueOf(restaurantProduct.getCostPriceVatIncl()));
		restaurantProductResponse.setCostPriceVatExcl(String.valueOf(restaurantProduct.getCostPriceVatExcl()));
		restaurantProductResponse.setSellingPriceVatIncl(String.valueOf(restaurantProduct.getSellingPriceVatIncl()));
		restaurantProductResponse.setSellingPriceVatExcl(String.valueOf(restaurantProduct.getSellingPriceVatExcl()));

		// Convert boolean active to String
		restaurantProductResponse.setActive(String.valueOf(restaurantProduct.isActive()));

		// Map references to String
		restaurantProductResponse.setProductId(restaurantProduct.getProduct().getId().toString());
		restaurantProductResponse.setRestaurantId(restaurantProduct.getRestaurant().getId().toString());

		// Convert date/time to formatted String
		restaurantProductResponse.setCreatedDateTime(restaurantProduct.getCreatedDateTime().toString());
		restaurantProductResponse.setCreated(restaurantProduct.getCreatedDateTime().toLocalDate().toString());
		return restaurantProductResponse;
	}
	
	private boolean createRestaurantProductLog(Restaurant restaurant, Product product, double qtyIn, double qtyOut, double balance, User createdByUser, LocalDateTime createdDateTime, String reference) {
		// Create a restaurant product log
		RestaurantProductLog restaurantProductLog = new RestaurantProductLog();
		restaurantProductLog.setRestaurant(restaurant);
		restaurantProductLog.setProduct(product);
		restaurantProductLog.setQtyIn(qtyIn);
		restaurantProductLog.setQtyOut(qtyOut);
		restaurantProductLog.setBalance(balance);
		restaurantProductLog.setReference(reference);
		restaurantProductLog.setCreatedByUser(createdByUser);
		restaurantProductLog.setCreatedDateTime(createdDateTime);
		restaurantProductLogRepository.save(restaurantProductLog);
		return true;
	}
	@Override
	public List<ProductResponseDTO> getProductsByRestaurantAndName(Long restaurantId, String productName) {
		
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
		
	    List<RestaurantProduct> restaurantProducts = restaurantProductRepository.findAllByRestaurantAndProduct_NameContainingIgnoreCase(restaurant, productName);
	    
	    List<ProductResponseDTO> productResponses = new ArrayList<>();
	    
	    for(RestaurantProduct restaurantProduct : restaurantProducts) {
	    	ProductResponseDTO productResponse = new ProductResponseDTO();
	    	productResponse.setId(restaurantProduct.getProduct().getId().toString());
	    	productResponse.setName(restaurantProduct.getProduct().getName());
	    	productResponses.add(productResponse);
	    }
	    return productResponses;
	}

	@Override
	public long checkUnderstockByRestaurant(Long restaurantId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
		
		return restaurantProductRepository.countProductsBelowMinStock(restaurant);
	}
	
	@Override
	public long checkOutofstockByRestaurant(Long restaurantId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
		
		return restaurantProductRepository.countProductsOutofStock(restaurant);
	}
}
