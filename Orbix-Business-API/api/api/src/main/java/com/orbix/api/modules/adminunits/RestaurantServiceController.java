package com.orbix.api.modules.adminunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RestaurantServiceController implements RestaurantService {
	private final BranchRepository branchRepository;
	private final RestaurantRepository restaurantRepository;
	private final UserService userService;
	private final DayService dayService;
	
	/**
	 * 
	 */
	@Override
	public List<RestaurantResponseDTO> getAllRestaurants(HttpServletRequest request) {
		List<Restaurant> restaurantes = restaurantRepository.findAll();
		List<RestaurantResponseDTO> restaurantResponses = new ArrayList<>();

		for(Restaurant restaurant : restaurantes) {
			restaurantResponses.add(restaurantResponseDTOMapper(restaurant));					
		}		
		return restaurantResponses;
	}

	/**
	 * 
	 */
	@Override
	public RestaurantResponseDTO get(
			Long id, 
			HttpServletRequest request) {
		Optional<Restaurant> _restaurant = restaurantRepository.findById(id);
		if(_restaurant.isEmpty()) {
			throw new NotFoundException("Restaurant not found");
		}		
		return restaurantResponseDTOMapper(_restaurant.get());	
	}
	
	/**
	 * 
	 */
	@Override
	public RestaurantResponseDTO createRestaurant(
			RestaurantRequestDTO restaurantRequest, 
			HttpServletRequest request) {
		
		
		
		Optional<Branch> branch_ = branchRepository.findById(userService.getUser(request).getBranch().getId());
		if(branch_.isEmpty()) {
			throw new InvalidEntryException("Branch not found in database");
		}
		
		Restaurant restaurant = new Restaurant();
		restaurant.setCode(restaurantRequest.getCode());
		restaurant.setName(restaurantRequest.getName());
		restaurant.setLocationName(restaurantRequest.getLocationName());		
		restaurant.setBranch(branch_.get());
		
		restaurant.setRestaurantCategory(restaurantRequest.getRestaurantCategory());
		
		restaurant.setCreatedByUser(userService.getUser(request));
		restaurant.setCreatedDateTime(dayService.getTimeStamp());
		
		restaurant = restaurantRepository.save(restaurant);
		/**Create  company code*/
		restaurant.setCode("RST/"+ restaurant.getId().toString());
		restaurant = restaurantRepository.save(restaurant);
		
		return restaurantResponseDTOMapper(restaurant);
	}

	@Override
	public RestaurantResponseDTO updateRestaurant(RestaurantRequestDTO restaurantRequest, HttpServletRequest request) {

		Optional<Restaurant> restaurant_ = restaurantRepository.findById(restaurantRequest.getId());
		if(restaurant_.isEmpty()) {
			throw new NotFoundException("Restaurant not be found in database");
		}		
		if(!validateRestaurantData(restaurantRequest)) {
			throw new InvalidEntryException("Could not validate restaurant data");
		}	
		
		Restaurant restaurant = restaurant_.get();
		restaurant.setName(restaurantRequest.getName());
		restaurant.setLocationName(restaurantRequest.getLocationName());
		restaurant.setRestaurantCategory(restaurantRequest.getRestaurantCategory());
		restaurant = restaurantRepository.save(restaurant);		
		return restaurantResponseDTOMapper(restaurant);
	}
	
	private RestaurantResponseDTO restaurantResponseDTOMapper(Restaurant restaurant) {
		RestaurantResponseDTO restaurantResponse = new RestaurantResponseDTO();
		restaurantResponse.setId(restaurant.getId().toString());
		restaurantResponse.setCode(restaurant.getCode());
		restaurantResponse.setName(restaurant.getName());
		restaurantResponse.setLocationName(restaurant.getLocationName());
		restaurantResponse.setRestaurantCategory(String.valueOf(restaurant.getRestaurantCategory()));
		restaurantResponse.setBranchId(restaurant.getBranch().getId().toString());		
		if(restaurant.isActive()) {
			restaurantResponse.setActive("Active");
		}else {
			restaurantResponse.setActive("Inactive");
		}
		restaurantResponse.setOtherInfo("Branch: " + restaurant.getBranch().getName() + " Location: " + restaurant.getLocationName());
		return restaurantResponse;
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateRestaurant(RestaurantRequestDTO restaurant, HttpServletRequest request) {
		Optional<Restaurant> restaurant_ = restaurantRepository.findById(restaurant.getId());		
		if(restaurant_.isEmpty()) {
			throw new NotFoundException("Restaurant not found");
		}		
		if(restaurant_.get().isActive() == true) {
			throw new InvalidOperationException("Restaurant already active");
		}
		restaurant_.get().setActive(true);
		restaurantRepository.save(restaurant_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Restaurant Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateRestaurant(RestaurantRequestDTO restaurant, HttpServletRequest request) {
		Optional<Restaurant> restaurant_ = restaurantRepository.findById(restaurant.getId());		
		if(restaurant_.isEmpty()) {
			throw new NotFoundException("Restaurant not found");
		}		
		if(restaurant_.get().isActive() == false) {
			throw new InvalidOperationException("Restaurant already inactive");
		}
		restaurant_.get().setActive(false);
		restaurantRepository.save(restaurant_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Restaurant Deactivated successifully");
	}
	
	/**
	 * 
	 * @param restaurantRequest
	 * @return
	 */
	boolean validateRestaurantData(RestaurantRequestDTO restaurantRequest) {
		
		return true;
	}

	@Override
	public List<RestaurantResponseDTO> getBranchAvailableRestaurantsByUser(HttpServletRequest request) {
		User user = userService.getUser(request);
		List<Restaurant> restaurants = restaurantRepository.findAllByBranch(user.getBranch());
		
		List<RestaurantResponseDTO> restaurantResponses = new ArrayList<>();

		for(Restaurant restaurant : restaurants) {
			restaurantResponses.add(restaurantResponseDTOMapper(restaurant));					
		}		
		return restaurantResponses;
	}
	
	@Override
	public RestaurantResponseDTO getSelectedRestaurant(
			Long id, 
			HttpServletRequest request) {
		Optional<Restaurant> _restaurant = restaurantRepository.findByIdAndBranch(id, userService.getUser(request));
		if(_restaurant.isEmpty()) {
			throw new NotFoundException("Restaurant not found");
		}		
		return restaurantResponseDTOMapper(_restaurant.get());	
	}
	
	@Override
	public List<RestaurantResponseDTO> getBranchRestaurants(HttpServletRequest request) {
		List<Restaurant> restaurants = restaurantRepository.findAllByBranch(userService.getUserBranch(request));
		
		List<RestaurantResponseDTO> restaurantResponses = new ArrayList<>();

		for(Restaurant restaurant : restaurants) {
			restaurantResponses.add(restaurantResponseDTOMapper(restaurant));					
		}		
		return restaurantResponses;
	}
}
