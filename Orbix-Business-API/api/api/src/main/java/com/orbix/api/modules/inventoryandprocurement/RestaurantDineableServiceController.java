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
import com.orbix.api.modules.adminunits.Company;
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
public class RestaurantDineableServiceController implements RestaurantDineableService {
	
	private final CompanyRepository companyRepository;
	private final RestaurantRepository restaurantRepository;
	private final DineableRepository dineableRepository;
	private final RestaurantDineableRepository restaurantDineableRepository;
	//private final RestaurantDineableLogRepository restaurantDineableLogRepository;
	private final UserService userService;
	private final DayService dayService;

	@Override
	public List<RestaurantDineableResponseDTO> getAllRestaurantDineables(Long restaurantId, HttpServletRequest request) {
	    // Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    
	    // Fetch all restaurant dineables
	    List<RestaurantDineable> restaurantDineables = restaurantDineableRepository.findAllByRestaurant(restaurant);
	    
	    // Map to response DTOs using streams
	    return restaurantDineables.stream()
	            .map(this::restaurantDineableResponseDTOMapper)
	            .collect(Collectors.toList());
	}
	
//	@Override
//	public List<RestaurantDineableResponseDTO> getUnderstockRestaurantDineables(Long restaurantId, HttpServletRequest request) {
//	    // Validate and fetch the restaurant
//	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
//	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
//	    
//	    // Fetch all restaurant dineables
//	    List<RestaurantDineable> restaurantDineables = restaurantDineableRepository.findDineablesWithLowStock(restaurant.getId());
//
//	    // Map to response DTOs using streams
//	    return restaurantDineables.stream()
//	            .map(this::restaurantDineableResponseDTOMapper)
//	            .collect(Collectors.toList());
//	}
	
//	@Override
//	public List<RestaurantDineableResponseDTO> getOutofstockRestaurantDineables(Long restaurantId, HttpServletRequest request) {
//	    // Validate and fetch the restaurant
//	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
//	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
//	    
//	    // Fetch all restaurant dineables
//	    List<RestaurantDineable> restaurantDineables = restaurantDineableRepository.findByRestaurantAndCurrentStockLessThanEqual(restaurant, 0);
//
//	    // Map to response DTOs using streams
//	    return restaurantDineables.stream()
//	            .map(this::restaurantDineableResponseDTOMapper)
//	            .collect(Collectors.toList());
//	}

	@Override
	public RestaurantDineableResponseDTO get(Long id, Long restaurantId, HttpServletRequest request) {
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	 // Validate and fetch the restaurant
	    RestaurantDineable restaurantDineable = restaurantDineableRepository.findByIdAndRestaurant(id, restaurant)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Dineable not found"));
	    
	    return this.restaurantDineableResponseDTOMapper(restaurantDineable);
	}
	
	@Override
	public RestaurantDineableResponseDTO getDineableInRestaurant(Long dineableId, Long restaurantId, HttpServletRequest request) {
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	 // Validate and fetch the restaurant
	    Dineable dineable = dineableRepository.findById(dineableId)
	                              .orElseThrow(() -> new NotFoundException("Dineable not found"));
	    
	    RestaurantDineable restaurantDineable = restaurantDineableRepository.findByDineableAndRestaurant(dineable, restaurant)
                .orElseThrow(() -> new NotFoundException("Dineable not found in restaurant"));
	    
	    return this.restaurantDineableResponseDTOMapper(restaurantDineable);
	}

	@Override
	public RestaurantDineableResponseDTO createRestaurantDineable(RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request) {
		
		Long restaurantId = restaurantDineableRequest.getRestaurantId();
		Long dineableId = restaurantDineableRequest.getDineableId();
		double vatRate = restaurantDineableRequest.getVatRate();
		////////////////
		double costPriceVatIncl = restaurantDineableRequest.getCostPriceVatIncl();
		double costPriceVatExcl = restaurantDineableRequest.getCostPriceVatExcl();
		double sellingPriceVatIncl = restaurantDineableRequest.getSellingPriceVatIncl();
		double sellingPriceVatExcl = restaurantDineableRequest.getSellingPriceVatExcl();
		/////////////////
		double currentStock = restaurantDineableRequest.getCurrentStock();
		double minStock = restaurantDineableRequest.getMinStock();
		double maxStock = restaurantDineableRequest.getMaxStock();
		double defaultReorderLevel = restaurantDineableRequest.getDefaultReorderLevel();
		double defaultReorderQty = restaurantDineableRequest.getDefaultReorderQty();
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the dineable
	    Dineable dineable = dineableRepository.findById(dineableId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Dineable not found"));
	   
	    if (restaurantDineableRepository.existsByRestaurantAndDineable(restaurant, dineable)) {
	        throw new InvalidOperationException(
	            String.format("Dineable '%s' already exists in restaurant '%s'", dineable.getName(), restaurant.getName())
	        );
	    }
		
		
		
		
		RestaurantDineable restaurantDineable = new RestaurantDineable();
		restaurantDineable.setRestaurant(restaurant);
	    restaurantDineable.setDineable(dineable);
//	    restaurantDineable.setCurrentStock(currentStock);
//	    restaurantDineable.setMinStock(minStock);
//	    restaurantDineable.setMaxStock(maxStock);
	    restaurantDineable.setVatRate(vatRate);
	    restaurantDineable.setCostPriceVatIncl(costPriceVatIncl);
	    restaurantDineable.setCostPriceVatExcl(costPriceVatExcl);
	    restaurantDineable.setSellingPriceVatIncl(sellingPriceVatIncl);
	    restaurantDineable.setSellingPriceVatExcl(sellingPriceVatExcl);
//	    restaurantDineable.setDefaultReorderLevel(defaultReorderLevel);
//	    restaurantDineable.setDefaultReorderQty(defaultReorderQty);
	    
	    restaurantDineable.setActive(true);
	    
	    restaurantDineable.setCreatedByUser(userService.getUser(request));
	    restaurantDineable.setCreatedDateTime(dayService.getTimeStamp());
	    
	    restaurantDineable = restaurantDineableRepository.save(restaurantDineable);
	    
	    
	    
	    ////Update RestaurantDineable log for stock card
	    
//	    this.createRestaurantDineableLog(restaurant, dineable, currentStock, 0, currentStock, userService.getUser(request), dayService.getTimeStamp(), "Opening stock");

		
	    return this.restaurantDineableResponseDTOMapper(restaurantDineable);
	}

	@Override
	public RestaurantDineableResponseDTO updateRestaurantDineable(RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request) {
		
		
		
		Long restaurantId = restaurantDineableRequest.getRestaurantId();
		Long dineableId = restaurantDineableRequest.getDineableId();
		double vatRate = restaurantDineableRequest.getVatRate();
		////////////////
		double costPriceVatIncl = restaurantDineableRequest.getCostPriceVatIncl();
		double costPriceVatExcl = restaurantDineableRequest.getCostPriceVatExcl();
		double sellingPriceVatIncl = restaurantDineableRequest.getSellingPriceVatIncl();
		double sellingPriceVatExcl = restaurantDineableRequest.getSellingPriceVatExcl();
		///////////////// do not update stock
		double minStock = restaurantDineableRequest.getMinStock();
		double maxStock = restaurantDineableRequest.getMaxStock();
		double defaultReorderLevel = restaurantDineableRequest.getDefaultReorderLevel();
		double defaultReorderQty = restaurantDineableRequest.getDefaultReorderQty();
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the dineable
	    Dineable dineable = dineableRepository.findById(dineableId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Dineable not found"));
	    
	    Optional<RestaurantDineable> restaurantDineable_ = restaurantDineableRepository.findByRestaurantAndDineable(restaurant, dineable);
	    if(restaurantDineable_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Dineable '%s' does not exist in restaurant '%s'", dineable.getName(), restaurant.getName())
		        );
	    }
	   
		RestaurantDineable restaurantDineable = restaurantDineable_.get();
		
//	    restaurantDineable.setMinStock(minStock);
//	    restaurantDineable.setMaxStock(maxStock);
	    restaurantDineable.setVatRate(vatRate);
	    restaurantDineable.setCostPriceVatIncl(costPriceVatIncl);
	    restaurantDineable.setCostPriceVatExcl(costPriceVatExcl);
	    restaurantDineable.setSellingPriceVatIncl(sellingPriceVatIncl);
	    restaurantDineable.setSellingPriceVatExcl(sellingPriceVatExcl);
//	    restaurantDineable.setDefaultReorderLevel(defaultReorderLevel);
//	    restaurantDineable.setDefaultReorderQty(defaultReorderQty);
	    
	    restaurantDineable.setActive(true);
	    
	    restaurantDineable = restaurantDineableRepository.save(restaurantDineable);
	    
	    
	    
	    return this.restaurantDineableResponseDTOMapper(restaurantDineable);
	}
	
	
	@Override
	public RestaurantDineableResponseDTO adjustRestaurantStock(RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request) {
		
		
		
		Long restaurantId = restaurantDineableRequest.getRestaurantId();
		Long dineableId = restaurantDineableRequest.getDineableId();
		
		/////////////////
		double currentStock = restaurantDineableRequest.getCurrentStock();
		
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
	    // Validate and fetch the dineable
	    Dineable dineable = dineableRepository.findById(dineableId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant Dineable not found"));
	    
	    Optional<RestaurantDineable> restaurantDineable_ = restaurantDineableRepository.findByRestaurantAndDineable(restaurant, dineable);
	    if(restaurantDineable_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Dineable '%s' does not exist in restaurant '%s'", dineable.getName(), restaurant.getName())
		        );
	    }
	   
		RestaurantDineable restaurantDineable = restaurantDineable_.get();
		boolean stockChanged = false;
//		if(restaurantDineable.getCurrentStock() != currentStock) {
//			stockChanged = true;
//		}
//	    restaurantDineable.setCurrentStock(currentStock);
	    
	    restaurantDineable.setActive(true);
	    
	    restaurantDineable = restaurantDineableRepository.save(restaurantDineable);
	    
	    //Update RestaurantDineable log for stock card
	    
//	    if(stockChanged == true)this.createRestaurantDineableLog(restaurant, dineable, currentStock, 0, currentStock, userService.getUser(request), dayService.getTimeStamp(), "Stock Adjustment");

	    
	    return this.restaurantDineableResponseDTOMapper(restaurantDineable);
	}

	@Override
	public ApiCustomResponse activateRestaurantDineable(RestaurantDineableRequestDTO restaurantDineableRequest, HttpServletRequest request) {
		Optional<RestaurantDineable> restaurantDineable_ = restaurantDineableRepository.findById(restaurantDineableRequest.getId());		
		if(restaurantDineable_.isEmpty()) {
			throw new NotFoundException("Restaurant Dineable not found");
		}		
		if(restaurantDineable_.get().isActive() == false) {
			throw new InvalidOperationException("Restaurant Dineable already inactive");
		}
		restaurantDineable_.get().setActive(false);
		restaurantDineableRepository.save(restaurantDineable_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Restaurant Dineable Deactivated successifully");
	}

	@Override
	public ApiCustomResponse deactivateRestaurantDineable(RestaurantDineableRequestDTO restaurantDineableRequest,
			HttpServletRequest request) {
		Optional<RestaurantDineable> restaurantDineable_ = restaurantDineableRepository.findById(restaurantDineableRequest.getId());		
		if(restaurantDineable_.isEmpty()) {
			throw new NotFoundException("Restaurant Dineable not found");
		}		
		if(restaurantDineable_.get().isActive() == true) {
			throw new InvalidOperationException("Restaurant Dineable already active");
		}
		restaurantDineable_.get().setActive(true);
		restaurantDineableRepository.save(restaurantDineable_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Restaurant Dineable Activated successifully");
	}
	
	private RestaurantDineableResponseDTO restaurantDineableResponseDTOMapper(RestaurantDineable restaurantDineable) {
		RestaurantDineableResponseDTO restaurantDineableResponse = new RestaurantDineableResponseDTO();
		
		restaurantDineableResponse.setDineableCode(restaurantDineable.getDineable().getCode());
		restaurantDineableResponse.setDineableName(restaurantDineable.getDineable().getName());
		restaurantDineableResponse.setDineableDescription(restaurantDineable.getDineable().getDescription());
		restaurantDineableResponse.setBaseUom(restaurantDineable.getDineable().getBaseUom());
		
		// Convert entity ID to String
		restaurantDineableResponse.setId(restaurantDineable.getId().toString());

		// Convert numerical values to String
//		restaurantDineableResponse.setCurrentStock(String.valueOf(restaurantDineable.getCurrentStock()));
//		restaurantDineableResponse.setMinStock(String.valueOf(restaurantDineable.getMinStock()));
//		restaurantDineableResponse.setMaxStock(String.valueOf(restaurantDineable.getMaxStock()));
//		restaurantDineableResponse.setDefaultReorderQty(String.valueOf(restaurantDineable.getDefaultReorderQty()));
//		restaurantDineableResponse.setDefaultReorderLevel(String.valueOf(restaurantDineable.getDefaultReorderLevel()));

		// Format vatRate and derive vatPercentage
		restaurantDineableResponse.setVatRate(String.valueOf(restaurantDineable.getVatRate()));
		restaurantDineableResponse.setVatPercentage(String.format("%.2f%%", restaurantDineable.getVatRate() * 100));

		// Convert prices to String
		restaurantDineableResponse.setCostPriceVatIncl(String.valueOf(restaurantDineable.getCostPriceVatIncl()));
		restaurantDineableResponse.setCostPriceVatExcl(String.valueOf(restaurantDineable.getCostPriceVatExcl()));
		restaurantDineableResponse.setSellingPriceVatIncl(String.valueOf(restaurantDineable.getSellingPriceVatIncl()));
		restaurantDineableResponse.setSellingPriceVatExcl(String.valueOf(restaurantDineable.getSellingPriceVatExcl()));

		// Convert boolean active to String
		restaurantDineableResponse.setActive(String.valueOf(restaurantDineable.isActive()));

		// Map references to String
		restaurantDineableResponse.setDineableId(restaurantDineable.getDineable().getId().toString());
		restaurantDineableResponse.setRestaurantId(restaurantDineable.getRestaurant().getId().toString());

		// Convert date/time to formatted String
		restaurantDineableResponse.setCreatedDateTime(restaurantDineable.getCreatedDateTime().toString());
		restaurantDineableResponse.setCreated(restaurantDineable.getCreatedDateTime().toLocalDate().toString());
		return restaurantDineableResponse;
	}
	
//	private boolean createRestaurantDineableLog(Restaurant restaurant, Dineable dineable, double qtyIn, double qtyOut, double balance, User createdByUser, LocalDateTime createdDateTime, String reference) {
//		// Create a restaurant dineable log
//		RestaurantDineableLog restaurantDineableLog = new RestaurantDineableLog();
//		restaurantDineableLog.setRestaurant(restaurant);
//		restaurantDineableLog.setDineable(dineable);
//		restaurantDineableLog.setQtyIn(qtyIn);
//		restaurantDineableLog.setQtyOut(qtyOut);
//		restaurantDineableLog.setBalance(balance);
//		restaurantDineableLog.setReference(reference);
//		restaurantDineableLog.setCreatedByUser(createdByUser);
//		restaurantDineableLog.setCreatedDateTime(createdDateTime);
//		restaurantDineableLogRepository.save(restaurantDineableLog);
//		return true;
//	}
	@Override
	public List<DineableResponseDTO> getDineablesByRestaurantAndName(Long restaurantId, String dineableName) {
		
		// Validate and fetch the restaurant
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	                              .orElseThrow(() -> new NotFoundException("Restaurant not found"));
		
	    List<RestaurantDineable> restaurantDineables = restaurantDineableRepository.findAllByRestaurantAndDineable_NameContainingIgnoreCase(restaurant, dineableName);
	    
	    List<DineableResponseDTO> dineableResponses = new ArrayList<>();
	    
	    for(RestaurantDineable restaurantDineable : restaurantDineables) {
	    	DineableResponseDTO dineableResponse = new DineableResponseDTO();
	    	dineableResponse.setId(restaurantDineable.getDineable().getId().toString());
	    	dineableResponse.setName(restaurantDineable.getDineable().getName());
	    	dineableResponses.add(dineableResponse);
	    }
	    return dineableResponses;
	}

//	@Override
//	public long checkUnderstockByRestaurant(Long restaurantId, HttpServletRequest request) {
//		// TODO Auto-generated method stub
//		Restaurant restaurant = restaurantRepository.findById(restaurantId)
//                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
//		
//		return restaurantDineableRepository.countDineablesBelowMinStock(restaurant);
//	}
	
//	@Override
//	public long checkOutofstockByRestaurant(Long restaurantId, HttpServletRequest request) {
//		// TODO Auto-generated method stub
//		Restaurant restaurant = restaurantRepository.findById(restaurantId)
//                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
//		
//		return restaurantDineableRepository.countDineablesOutofStock(restaurant);
//	}
	
	

}
