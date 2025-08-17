package com.orbix.api.modules.inventoryandprocurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.RestaurantRepository;
import com.orbix.api.modules.adminunits.RestaurantService;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DineableServiceController implements DineableService {
	private final CompanyRepository companyRepository;
	private final DineableRepository dineableRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final RestaurantRepository restaurantRepository;
	
	private final RestaurantDineableRepository restaurantDineableRepository;
	
	/**
	 * 
	 */
	@Override
	public List<DineableResponseDTO> getAllDineablees(HttpServletRequest request) {
		List<Dineable> dineables = dineableRepository.findAll();
		List<DineableResponseDTO> dineableResponses = new ArrayList<>();

		for(Dineable dineable : dineables) {
			dineableResponses.add(dineableResponseDTOMapper(dineable));					
		}		
		return dineableResponses;
	}

	/**
	 * 
	 */
	@Override
	public DineableResponseDTO get(
			Long id, 
			HttpServletRequest request) {
		Optional<Dineable> _dineable = dineableRepository.findById(id);
		if(_dineable.isEmpty()) {
			throw new NotFoundException("Dineable not found");
		}		
		return dineableResponseDTOMapper(_dineable.get());	
	}
	
	@Override
	public DineableResponseDTO getCompanyDineable(
			Long dineableId, 
			HttpServletRequest request) {
		Optional<Dineable> _dineable = dineableRepository.findByIdAndCompany(dineableId, userService.getUserCompany(request));
		if(_dineable.isEmpty()) {
			throw new NotFoundException("Dineable not found");
		}		
		return dineableResponseDTOMapper(_dineable.get());	
	}
	
	/**
	 * 
	 */
	@Override
	public DineableResponseDTO createDineable(
			DineableRequestDTO dineableRequest, 
			HttpServletRequest request) {
		
		Optional<Company> company_ = companyRepository.findById(userService.getUser(request).getCompany().getId());
		if(company_.isEmpty()) {
			throw new InvalidEntryException("Company not found in database");
		}
		
		Dineable dineable = new Dineable();
		//dineable.setCode(dineableRequest.getCode());
		dineable.setCode(String.valueOf(Math.random()));
		dineable.setName(dineableRequest.getName());
		dineable.setDescription(dineableRequest.getDescription());
		dineable.setBaseUom(dineableRequest.getBaseUom());		
		dineable.setCompany(company_.get());
		
		dineable.setSellable(true);
		
		dineable.setCreatedByUser(userService.getUser(request));
		dineable.setCreatedDateTime(dayService.getTimeStamp());
		
		dineable = dineableRepository.save(dineable);
		/**Create  company code*/
		dineable.setCode("AC/"+ dineable.getId().toString());
		dineable = dineableRepository.save(dineable);
		
		return dineableResponseDTOMapper(dineable);
	}

	@Override
	public DineableResponseDTO updateDineable(DineableRequestDTO dineableRequest, HttpServletRequest request) {

		Optional<Dineable> dineable_ = dineableRepository.findById(dineableRequest.getId());
		if(dineable_.isEmpty()) {
			throw new NotFoundException("Dineable not be found in database");
		}		
		if(!validateDineableData(dineableRequest)) {
			throw new InvalidEntryException("Could not validate dineable data");
		}		
		Dineable dineable = dineable_.get();
		dineable.setName(dineableRequest.getName());
		dineable.setDescription(dineableRequest.getDescription());
		dineable.setBaseUom(dineableRequest.getBaseUom());
		
		dineable.setSellable(true);
		
		
		dineable = dineableRepository.save(dineable);		
		return dineableResponseDTOMapper(dineable);
	}
	
	private DineableResponseDTO dineableResponseDTOMapper(Dineable dineable) {	
		DineableResponseDTO dineableResponse = new DineableResponseDTO();
		dineableResponse.setId(dineable.getId().toString());
		dineableResponse.setCode(dineable.getCode());
		dineableResponse.setName(dineable.getName());
		dineableResponse.setDescription(dineable.getDescription());
		dineableResponse.setBaseUom(dineable.getBaseUom());
		dineableResponse.setCompanyId(dineable.getCompany().getId().toString());		
		if(dineable.isActive()) {
			dineableResponse.setActive("Active");
		}else {
			dineableResponse.setActive("Inactive");
		}
		
		//dineableResponse.setOtherInfo("Company: " + dineable.getCompany().getName() + " Location: " + dineable.getLocationName());
		return dineableResponse;
	}
	
	private DineableResponseDTO dineableResponseDTOMapperWithImportedStatus(Dineable dineable, boolean importedToRestaurant) {	
		DineableResponseDTO dineableResponse = new DineableResponseDTO();
		dineableResponse.setId(dineable.getId().toString());
		dineableResponse.setCode(dineable.getCode());
		dineableResponse.setName(dineable.getName());
		dineableResponse.setDescription(dineable.getDescription());
		dineableResponse.setBaseUom(dineable.getBaseUom());
		dineableResponse.setCompanyId(dineable.getCompany().getId().toString());		
		if(dineable.isActive()) {
			dineableResponse.setActive("Active");
		}else {
			dineableResponse.setActive("Inactive");
		}
		if(importedToRestaurant == true) {
			dineableResponse.setImported("1");
		}else {
			dineableResponse.setImported("0");
		}
		
		//dineableResponse.setOtherInfo("Company: " + dineable.getCompany().getName() + " Location: " + dineable.getLocationName());
		return dineableResponse;
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateDineable(DineableRequestDTO dineable, HttpServletRequest request) {
		Optional<Dineable> dineable_ = dineableRepository.findById(dineable.getId());		
		if(dineable_.isEmpty()) {
			throw new NotFoundException("Dineable not found");
		}		
		if(dineable_.get().isActive() == true) {
			throw new InvalidOperationException("Dineable already active");
		}
		dineable_.get().setActive(true);
		dineableRepository.save(dineable_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Dineable Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateDineable(DineableRequestDTO dineable, HttpServletRequest request) {
		Optional<Dineable> dineable_ = dineableRepository.findById(dineable.getId());		
		if(dineable_.isEmpty()) {
			throw new NotFoundException("Dineable not found");
		}		
		if(dineable_.get().isActive() == false) {
			throw new InvalidOperationException("Dineable already inactive");
		}
		dineable_.get().setActive(false);
		dineableRepository.save(dineable_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Dineable Deactivated successifully");
	}
	
	/**
	 * 
	 * @param dineableRequest
	 * @return
	 */
	boolean validateDineableData(DineableRequestDTO dineableRequest) {
		
		return true;
	}
	
	@Override
	public List<DineableResponseDTO> getDineablesByCompany(String dineableName, HttpServletRequest request) {
	    // Validate input
	    if (dineableName == null || dineableName.trim().isEmpty()) {
	        throw new IllegalArgumentException("Dineable name cannot be null or empty");
	    }

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }

	    // Fetch dineables
	    List<Dineable> dineables = dineableRepository.findAllByCompanyAndNameContainingIgnoreCase(company, dineableName);

	    // Map to DTOs
	    return dineables.stream()
	        .map(this::dineableResponseDTOMapper)
	        .collect(Collectors.toList());
	}
	
	@Override
	public List<DineableResponseDTO> getCompanyDineables(HttpServletRequest request) {
	    

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }

	    // Fetch dineables
	    List<Dineable> dineables = dineableRepository.findAllByCompany(company);

	    // Map to DTOs
	    return dineables.stream()
	        .map(this::dineableResponseDTOMapper)
	        .collect(Collectors.toList());
	}
	
	@Override
	public List<DineableResponseDTO> getCompanySellableDineables(HttpServletRequest request) {
	    

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }

	    // Fetch dineables
	    List<Dineable> dineables = dineableRepository.findAllByCompanyAndSellable(company, true);

	    // Map to DTOs
	    return dineables.stream()
	        .map(this::dineableResponseDTOMapper)
	        .collect(Collectors.toList());
	}
	
	@Override
	public List<DineableResponseDTO> getCompanySellableDineablesByRestaurant(Long restaurantId, HttpServletRequest request) {
	    

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }
	    Restaurant restaurant = restaurantRepository.findById(restaurantId)
	    	    .orElseThrow(() -> new NotFoundException("Restaurant not found"));

	    // Fetch dineables
	    List<Dineable> dineables = dineableRepository.findAllByCompanyAndSellable(company, true);
	    List<DineableResponseDTO> dineableResponses = new ArrayList<>();
	    
	    for(Dineable dineable : dineables) {
	    	boolean imported = false;
	    	Optional<RestaurantDineable> restaurantDineable_ = restaurantDineableRepository.findByDineableAndRestaurant(dineable, restaurant);
	    	if(restaurantDineable_.isPresent()) {
	    		imported = true;
	    	}	    	
	    	dineableResponses.add(dineableResponseDTOMapperWithImportedStatus(dineable, imported));
	    	
	    }

	    // Map to DTOs
	    return dineableResponses;
	}
	
	@Override
	public List<DineableResponseDTO> getDineablesByCompanyAndName(String dineableName, HttpServletRequest request) {
		
	    List<Dineable> dineables = dineableRepository.findAllByCompanyAndNameContainingIgnoreCase(userService.getUserCompany(request), dineableName);
	    
	    List<DineableResponseDTO> dineableResponses = new ArrayList<>();
	    
	    for(Dineable dineable : dineables) {
	    	DineableResponseDTO dineableResponse = new DineableResponseDTO();
	    	dineableResponse.setId(dineable.getId().toString());
	    	dineableResponse.setName(dineable.getName());
	    	dineableResponses.add(dineableResponse);
	    }
	    return dineableResponses;
	}
}
