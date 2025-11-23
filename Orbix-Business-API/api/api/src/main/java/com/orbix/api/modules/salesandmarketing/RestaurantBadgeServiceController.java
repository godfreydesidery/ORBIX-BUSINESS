package com.orbix.api.modules.salesandmarketing;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.RestaurantRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.inventoryandprocurement.DineableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RestaurantBadgeServiceController implements RestaurantBadgeService {
	
	private final RestaurantRepository restaurantRepository;
	private final RestaurantBadgeRepository restaurantBadgeRepository;
	
	@Override
	public List<RestaurantBadgeResponseDTO> getAllRestaurantBadgesByRestaurantId(Long restaurantId, HttpServletRequest request) {
		
		Restaurant restaurant = restaurantRepository.findById(restaurantId)
			    .orElseThrow(() -> new NotFoundException("Restaurant not found, with id " + restaurantId));

			List<RestaurantBadge> restaurantBadges = restaurantBadgeRepository.findAllByRestaurant(restaurant);

			return restaurantBadges.stream()
			    .map(this::toDto)
			    .collect(Collectors.toList());
	}
	
	@Override
	public String generateBadge(HttpServletRequest request) {
		Long id = 0L;
		RestaurantBadge badge = restaurantBadgeRepository.findTopByOrderByIdDesc();
		if(badge != null) {
			id = badge.getId();
		}
		return "Sighman_Res-" + (id + 1);
	}

	@Override
	public RestaurantBadgeResponseDTO createBadge(RestaurantBadgeRequestDTO badgeRequest, HttpServletRequest request) {
		
		Restaurant restaurant = restaurantRepository.findById(badgeRequest.getRestaurantId())
			    .orElseThrow(() -> new NotFoundException("Restaurant not found"));
		
		RestaurantBadge badge = new RestaurantBadge();
		badge.setCode(String.valueOf(Math.random()));
		badge.setRestaurant(restaurant);
		badge = restaurantBadgeRepository.save(badge);
		badge.setCode("Sighman_Res-" + badge.getId());
		badge = restaurantBadgeRepository.saveAndFlush(badge);
		
		return toDto(badge);
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateBadge(RestaurantBadgeRequestDTO agent, HttpServletRequest request) {
		Optional<RestaurantBadge> badge_ = restaurantBadgeRepository.findById(agent.getId());		
		if(badge_.isEmpty()) {
			throw new NotFoundException("Badge not found");
		}		
		if(badge_.get().isActive() == true) {
			throw new InvalidOperationException("Badge already active");
		}
		badge_.get().setActive(true);
		restaurantBadgeRepository.save(badge_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Badge Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateBadge(RestaurantBadgeRequestDTO agent, HttpServletRequest request) {
		Optional<RestaurantBadge> badge_ = restaurantBadgeRepository.findById(agent.getId());		
		if(badge_.isEmpty()) {
			throw new NotFoundException("Badge not found");
		}		
		if(badge_.get().isActive() == false) {
			throw new InvalidOperationException("Badge already inactive");
		}
		badge_.get().setActive(false);
		restaurantBadgeRepository.save(badge_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Badge Deactivated successifully");
	}
	
	private RestaurantBadgeResponseDTO toDto(RestaurantBadge badge) {
		RestaurantBadgeResponseDTO res = new RestaurantBadgeResponseDTO();
		res.setId(badge.getId().toString());
		res.setCode(badge.getCode());
		res.setRestaurantId(badge.getRestaurant().getId().toString());
		res.setActiveStatus(badge != null && badge.isActive() ? "Active" : "Inactive");
		return res;
	}

	

}
