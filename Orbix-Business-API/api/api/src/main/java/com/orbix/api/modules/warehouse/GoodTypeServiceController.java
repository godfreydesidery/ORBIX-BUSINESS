package com.orbix.api.modules.warehouse;

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
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.SystemProfileRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GoodTypeServiceController implements GoodTypeService {
	private final GoodTypeRepository goodTypeRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final SystemProfileRepository systemProfileRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public List<GoodTypeResponseDTO> getAllGoodTypes(HttpServletRequest request) {
		List<GoodType> goodTypes = goodTypeRepository.findAll();
		List<GoodTypeResponseDTO> goodTypeResponses = new ArrayList<>();

		for(GoodType goodType : goodTypes) {
			goodTypeResponses.add(goodTypeResponseDTOMapper(goodType));					
		}		
		return goodTypeResponses;
	}
	
	@Override
	public List<GoodTypeResponseDTO> getAllCompanyActiveGoodTypes(
			HttpServletRequest request) {
		Company company = userService.getUser(request).getCompany();	
		List<GoodType> goodTypes = goodTypeRepository.findAllByCompanyAndActive(company, true);
		List<GoodTypeResponseDTO> goodTypeResponses = new ArrayList<>();

		for(GoodType goodType : goodTypes) {
			goodTypeResponses.add(goodTypeResponseDTOMapper(goodType));					
		}		
		return goodTypeResponses;
	}

	@Override
	public GoodTypeResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<GoodType> goodType_ = goodTypeRepository.findById(id);
	if(goodType_.isEmpty()) {
		throw new NotFoundException("Good Type not found");
	}		
	return goodTypeResponseDTOMapper(goodType_.get());	
	}

	@Override
	public GoodTypeResponseDTO createGoodType(GoodTypeRequestDTO goodTypeRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateGoodTypeData(goodTypeRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) {
			throw new NotFoundException("Company not found");
		}
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}
		
		/**New parking zone*/
		GoodType goodType = new GoodType();		
		goodType.setCode(String.valueOf(Math.random()));
		goodType.setName(goodTypeRequest.getName());
		goodType.setDailyPrice(goodTypeRequest.getDailyPrice());
		goodType.setCompany(company_.get());
		
		goodType.setCreatedByUser(userService.getUser(request));
		goodType.setCreatedDateTime(dayService.getTimeStamp());
		
		goodType = goodTypeRepository.save(goodType);
		/**Create  parking zone code*/
		goodType.setCode("VEQ/"+ goodType.getId().toString());
		goodType = goodTypeRepository.save(goodType);
		
		return goodTypeResponseDTOMapper(goodType);		
	}

	@Override
	public GoodTypeResponseDTO updateGoodType(GoodTypeRequestDTO goodTypeRequest, HttpServletRequest request) {
		
		Optional<GoodType> goodType_ = goodTypeRepository.findById(goodTypeRequest.getId());
		if(goodType_.isEmpty()) {
			throw new NotFoundException("Good Type not found in database");
		}
		
		if(!validateGoodTypeData(goodTypeRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		GoodType goodType = goodType_.get();
		goodType.setName(goodTypeRequest.getName());
		goodType.setDailyPrice(goodTypeRequest.getDailyPrice());
				
		goodType = goodTypeRepository.save(goodType);
		
		return goodTypeResponseDTOMapper(goodType);	
		
	}

	@Override
	public ApiCustomResponse activateGoodType(GoodTypeRequestDTO goodTypeRequest, HttpServletRequest request) {
		Optional<GoodType> goodType_ = goodTypeRepository.findById(goodTypeRequest.getId());		
		if(goodType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(goodType_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		goodType_.get().setActive(true);
		goodTypeRepository.save(goodType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateGoodType(GoodTypeRequestDTO goodTypeRequest, HttpServletRequest request) {
		Optional<GoodType> goodType_ = goodTypeRepository.findById(goodTypeRequest.getId());		
		if(goodType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(goodType_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		goodType_.get().setActive(false);
		goodTypeRepository.save(goodType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type dectivated successifully");
	}
	
	
	private GoodTypeResponseDTO goodTypeResponseDTOMapper(GoodType goodType) {
		GoodTypeResponseDTO goodTypeResponse = new GoodTypeResponseDTO();
		
		goodTypeResponse.setId(goodType.getId().toString());
		goodTypeResponse.setCode(goodType.getCode());
		goodTypeResponse.setName(goodType.getName());
		goodTypeResponse.setDailyPrice(String.valueOf(goodType.getDailyPrice()));
		goodTypeResponse.setCompanyName(goodType.getCompany().getName());
	
		if(goodType.isActive()) {
			goodTypeResponse.setActive("Active");
		}else {
			goodTypeResponse.setActive("Inactive");
		}
		
		return goodTypeResponse;
	}
	
	boolean validateGoodTypeData(GoodTypeRequestDTO goodTypeRequest) {
		
		return true;
	}
}
