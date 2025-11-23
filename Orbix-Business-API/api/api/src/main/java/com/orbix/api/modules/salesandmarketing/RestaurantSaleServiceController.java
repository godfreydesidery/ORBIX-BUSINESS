package com.orbix.api.modules.salesandmarketing;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.inventoryandprocurement.DineableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RestaurantSaleServiceController implements RestaurantSaleService {
	private final RestaurantSaleRepository restaurantSaleRepository;
	private final RestaurantSaleDetailRepository restaurantSaleDetailRepository;
	private final DineableRepository dineableRepository;
	
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public RestaurantSale createRestaurantSale(RestaurantSaleRequestDTO restaurantSaleRequest, HttpServletRequest request) {
		
		RestaurantSale restaurantSale = new RestaurantSale();
		
		restaurantSale.setCreatedByUser(userService.getUser(request));
		restaurantSale.setCreatedDateTime(dayService.getTimeStamp());
		
		restaurantSale = restaurantSaleRepository.saveAndFlush(restaurantSale);
		
		if (restaurantSaleRequest.getRestaurantSaleDetails() == null || restaurantSaleRequest.getRestaurantSaleDetails().isEmpty()) {
		    throw new IllegalArgumentException("Sale details cannot be empty.");
		}
		
		List<RestaurantSaleDetail> restaurantSaleDetails = new ArrayList<>();
		
		for(RestaurantSaleDetailRequestDTO restaurantSaleDetailRequest : restaurantSaleRequest.getRestaurantSaleDetails()) {
			RestaurantSaleDetail restaurantSaleDetail = new RestaurantSaleDetail();
			restaurantSaleDetail.setRestaurantSale(restaurantSale);
			restaurantSaleDetail.setDineable(restaurantSaleDetailRequest.getDineable());
			restaurantSaleDetail.setCostPriceVatIncl(restaurantSaleDetailRequest.getCostPriceVatIncl());
			restaurantSaleDetail.setSellingPriceVatIncl(restaurantSaleDetailRequest.getSellingPriceVatIncl());
			restaurantSaleDetail.setVatRate(restaurantSaleDetailRequest.getVatRate());
			restaurantSaleDetail.setQty(restaurantSaleDetailRequest.getQty());
			if(restaurantSaleDetailRequest.getDiscount() > restaurantSaleDetailRequest.getSellingPriceVatIncl() * restaurantSaleDetailRequest.getQty()) throw new InvalidOperationException("Invalid discount");
			restaurantSaleDetail.setDiscount(restaurantSaleDetailRequest.getDiscount());
			restaurantSaleDetail = restaurantSaleDetailRepository.saveAndFlush(restaurantSaleDetail);
			restaurantSaleDetails.add(restaurantSaleDetail);
		}
		
		restaurantSale = restaurantSaleRepository.findById(restaurantSale.getId()).get();
		restaurantSale.setRestaurantSaleDetails(restaurantSaleDetails);
		
		return restaurantSale;
	}
}
