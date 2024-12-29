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
public class ShopServiceController implements ShopService {
	private final BranchRepository branchRepository;
	private final ShopRepository shopRepository;
	private final UserService userService;
	private final DayService dayService;
	
	/**
	 * 
	 */
	@Override
	public List<ShopResponseDTO> getAllShops(HttpServletRequest request) {
		List<Shop> shopes = shopRepository.findAll();
		List<ShopResponseDTO> shopResponses = new ArrayList<>();

		for(Shop shop : shopes) {
			shopResponses.add(shopResponseDTOMapper(shop));					
		}		
		return shopResponses;
	}

	/**
	 * 
	 */
	@Override
	public ShopResponseDTO get(
			Long id, 
			HttpServletRequest request) {
		Optional<Shop> _shop = shopRepository.findById(id);
		if(_shop.isEmpty()) {
			throw new NotFoundException("Shop not found");
		}		
		return shopResponseDTOMapper(_shop.get());	
	}
	
	/**
	 * 
	 */
	@Override
	public ShopResponseDTO createShop(
			ShopRequestDTO shopRequest, 
			HttpServletRequest request) {
		
		
		
		Optional<Branch> branch_ = branchRepository.findById(userService.getUser(request).getBranch().getId());
		if(branch_.isEmpty()) {
			throw new InvalidEntryException("Branch not found in database");
		}
		
		Shop shop = new Shop();
		shop.setCode(shopRequest.getCode());
		shop.setName(shopRequest.getName());
		shop.setLocationName(shopRequest.getLocationName());		
		shop.setBranch(branch_.get());
		
		shop.setCreatedByUser(userService.getUser(request));
		shop.setCreatedDateTime(dayService.getTimeStamp());
		
		shop = shopRepository.save(shop);
		/**Create  company code*/
		shop.setCode("SHOP/"+ shop.getId().toString());
		shop = shopRepository.save(shop);
		
		return shopResponseDTOMapper(shop);
	}

	@Override
	public ShopResponseDTO updateShop(ShopRequestDTO shopRequest, HttpServletRequest request) {

		Optional<Shop> shop_ = shopRepository.findById(shopRequest.getId());
		if(shop_.isEmpty()) {
			throw new NotFoundException("Shop not be found in database");
		}		
		if(!validateShopData(shopRequest)) {
			throw new InvalidEntryException("Could not validate shop data");
		}		
		Shop shop = shop_.get();
		shop.setName(shopRequest.getName());
		shop.setLocationName(shopRequest.getLocationName());		
		shop = shopRepository.save(shop);		
		return shopResponseDTOMapper(shop);
	}
	
	private ShopResponseDTO shopResponseDTOMapper(Shop shop) {
		ShopResponseDTO shopResponse = new ShopResponseDTO();
		shopResponse.setId(shop.getId().toString());
		shopResponse.setCode(shop.getCode());
		shopResponse.setName(shop.getName());
		shopResponse.setLocationName(shop.getLocationName());
		shopResponse.setBranchId(shop.getBranch().getId().toString());		
		if(shop.isActive()) {
			shopResponse.setActive("Active");
		}else {
			shopResponse.setActive("Inactive");
		}
		shopResponse.setOtherInfo("Branch: " + shop.getBranch().getName() + " Location: " + shop.getLocationName());
		return shopResponse;
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateShop(ShopRequestDTO shop, HttpServletRequest request) {
		Optional<Shop> shop_ = shopRepository.findById(shop.getId());		
		if(shop_.isEmpty()) {
			throw new NotFoundException("Shop not found");
		}		
		if(shop_.get().isActive() == true) {
			throw new InvalidOperationException("Shop already active");
		}
		shop_.get().setActive(true);
		shopRepository.save(shop_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Shop Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateShop(ShopRequestDTO shop, HttpServletRequest request) {
		Optional<Shop> shop_ = shopRepository.findById(shop.getId());		
		if(shop_.isEmpty()) {
			throw new NotFoundException("Shop not found");
		}		
		if(shop_.get().isActive() == false) {
			throw new InvalidOperationException("Shop already inactive");
		}
		shop_.get().setActive(false);
		shopRepository.save(shop_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Shop Deactivated successifully");
	}
	
	/**
	 * 
	 * @param shopRequest
	 * @return
	 */
	boolean validateShopData(ShopRequestDTO shopRequest) {
		
		return true;
	}

	@Override
	public List<ShopResponseDTO> getBranchAvailableShopsByUser(HttpServletRequest request) {
		User user = userService.getUser(request);
		List<Shop> shops = shopRepository.findAllByBranch(user.getBranch());
		
		List<ShopResponseDTO> shopResponses = new ArrayList<>();

		for(Shop shop : shops) {
			shopResponses.add(shopResponseDTOMapper(shop));					
		}		
		return shopResponses;
	}
	
	@Override
	public ShopResponseDTO getSelectedShop(
			Long id, 
			HttpServletRequest request) {
		Optional<Shop> _shop = shopRepository.findByIdAndBranch(id, userService.getUser(request));
		if(_shop.isEmpty()) {
			throw new NotFoundException("Shop not found");
		}		
		return shopResponseDTOMapper(_shop.get());	
	}
	
	@Override
	public List<ShopResponseDTO> getBranchShops(HttpServletRequest request) {
		List<Shop> shops = shopRepository.findAllByBranch(userService.getUserBranch(request));
		
		List<ShopResponseDTO> shopResponses = new ArrayList<>();

		for(Shop shop : shops) {
			shopResponses.add(shopResponseDTOMapper(shop));					
		}		
		return shopResponses;
	}
}
