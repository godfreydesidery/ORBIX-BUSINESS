package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.RestaurantRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RestaurantDinableProductServiceController implements RestaurantDineableProductService {

	private final CompanyRepository companyRepository;
	private final RestaurantRepository restaurantRepository;
	private final DineableRepository dineableRepository;
	private final ProductRepository productRepository;
	private final RestaurantDineableProductRepository restaurantDineableProductRepository;
	//private final RestaurantDineableLogRepository restaurantDineableLogRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public List<RestaurantDineableProductResponseDTO> getAllRestaurantDineableProducts(Long restaurantId, Long dineableId, HttpServletRequest request) {
	    // Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    
	    Dineable dineable = dineableRepository.findById(dineableId)
                .orElseThrow(() -> new NotFoundException("Dineable not found"));
	    
	    // Fetch all restaurant dineables
	    List<RestaurantDineableProduct> restaurantDineableProducts = restaurantDineableProductRepository.findAllByRestaurantAndDineable(restaurant, dineable);
	    
	    // Map to response DTOs using streams
	    return restaurantDineableProducts.stream()
	            .map(this::restaurantDineableProductResponseDTOMapper)
	            .collect(Collectors.toList());
	}
	
	@Override
	public RestaurantDineableProductResponseDTO createRestaurantDineableProduct(
			RestaurantDineableProductRequestDTO restaurantDineableProductRequest, HttpServletRequest request) {
		
		Long restaurantId = restaurantDineableProductRequest.getRestaurantId();
		Long dineableId = restaurantDineableProductRequest.getDineableId();
		Long productId = restaurantDineableProductRequest.getProductId();


		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the dineable
	    Dineable dineable = dineableRepository.findById(dineableId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Dineable not found"));
	    
	    Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Restaurant Product not found"));
	   
	    if (restaurantDineableProductRepository.existsByRestaurantAndDineableAndProduct(restaurant, dineable, product)) {
	        throw new InvalidOperationException(
	            String.format("Dineable '%s' already exists in restaurant '%s'", dineable.getName(), restaurant.getName())
	        );
	    }
		
		RestaurantDineableProduct restaurantDineableProduct = new RestaurantDineableProduct();
		restaurantDineableProduct.setRestaurant(restaurant);
		restaurantDineableProduct.setDineable(dineable);
		restaurantDineableProduct.setProduct(product);
		
		restaurantDineableProduct.setQty(restaurantDineableProductRequest.getProductQty());
 
		restaurantDineableProduct.setActive(true);
	    
		restaurantDineableProduct.setCreatedByUser(userService.getUser(request));
		restaurantDineableProduct.setCreatedDateTime(dayService.getTimeStamp());
	    
		restaurantDineableProduct = restaurantDineableProductRepository.save(restaurantDineableProduct);
	    
	    return this.restaurantDineableProductResponseDTOMapper(restaurantDineableProduct);
	}
	
	@Override
	public RestaurantDineableProductResponseDTO updateRestaurantDineableProduct(
			RestaurantDineableProductRequestDTO restaurantDineableProductRequest, HttpServletRequest request) {
		
		RestaurantDineableProduct restaurantDineableProduct = restaurantDineableProductRepository.findById(restaurantDineableProductRequest.getId())
				 .orElseThrow(() -> new NotFoundException("Not found"));
		
		restaurantDineableProduct.setQty(restaurantDineableProductRequest.getProductQty());
		
		restaurantDineableProduct = restaurantDineableProductRepository.save(restaurantDineableProduct);
		
		return this.restaurantDineableProductResponseDTOMapper(restaurantDineableProduct);
	}
	
	private RestaurantDineableProductResponseDTO restaurantDineableProductResponseDTOMapper(RestaurantDineableProduct restaurantDineableProduct) {
		RestaurantDineableProductResponseDTO restaurantDineableProductResponse = new RestaurantDineableProductResponseDTO();

		restaurantDineableProductResponse.setId(restaurantDineableProduct.getId().toString());
		
		restaurantDineableProductResponse.setRestaurantId(restaurantDineableProduct.getRestaurant().getId().toString());
		
		restaurantDineableProductResponse.setDineableId(restaurantDineableProduct.getDineable().getId().toString());
		restaurantDineableProductResponse.setDineableCode(restaurantDineableProduct.getDineable().getCode());
		restaurantDineableProductResponse.setDineableName(restaurantDineableProduct.getDineable().getName());
//		restaurantDineableProductResponse.setDineableDescription(restaurantDineableProduct.getDineable().getDescription());
		
		restaurantDineableProductResponse.setProductId(restaurantDineableProduct.getProduct().getId().toString());
		restaurantDineableProductResponse.setProductCode(restaurantDineableProduct.getProduct().getCode());
		restaurantDineableProductResponse.setProductName(restaurantDineableProduct.getProduct().getName());
//		restaurantDineableProductResponse.setProductDescription(restaurantDineableProduct.getProduct().getDescription());
		
		restaurantDineableProductResponse.setProductQty(String.valueOf(restaurantDineableProduct.getQty()));

		// Convert date/time to formatted String
		restaurantDineableProductResponse.setCreatedDateTime(restaurantDineableProduct.getCreatedDateTime().toString());
		restaurantDineableProductResponse.setCreated(restaurantDineableProduct.getCreatedDateTime().toLocalDate().toString());
		return restaurantDineableProductResponse;
	}

	@Override
	public boolean remove(Long id, HttpServletRequest request) {
		RestaurantDineableProduct restaurantDineableProduct = restaurantDineableProductRepository.findById(id)
				 .orElseThrow(() -> new NotFoundException("Not found"));
		
		restaurantDineableProductRepository.delete(restaurantDineableProduct);
		
		return true;
	}

	@Override
	public RestaurantDineableProductResponseDTO get(Long id, HttpServletRequest request) {
		RestaurantDineableProduct restaurantDineableProduct = restaurantDineableProductRepository.findById(id)
				 .orElseThrow(() -> new NotFoundException("Not found"));
		
		return this.restaurantDineableProductResponseDTOMapper(restaurantDineableProduct);
	}
}
