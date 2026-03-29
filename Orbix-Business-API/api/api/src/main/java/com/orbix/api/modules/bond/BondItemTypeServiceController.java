package com.orbix.api.modules.bond;

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
public class BondItemTypeServiceController implements BondItemTypeService {
	private final BondItemTypeRepository bondItemTypeRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final SystemProfileRepository systemProfileRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public List<BondItemTypeResponseDTO> getAllBondItemTypes(HttpServletRequest request) {
		List<BondItemType> bondItemTypes = bondItemTypeRepository.findAll();
		List<BondItemTypeResponseDTO> bondItemTypeResponses = new ArrayList<>();

		for(BondItemType bondItemType : bondItemTypes) {
			bondItemTypeResponses.add(bondItemTypeResponseDTOMapper(bondItemType));					
		}		
		return bondItemTypeResponses;
	}
	
	@Override
	public List<BondItemTypeResponseDTO> getAllCompanyActiveBondItemTypes(
			HttpServletRequest request) {
		Company company = userService.getUser(request).getCompany();	
		List<BondItemType> bondItemTypes = bondItemTypeRepository.findAllByCompanyAndActive(company, true);
		List<BondItemTypeResponseDTO> bondItemTypeResponses = new ArrayList<>();

		for(BondItemType bondItemType : bondItemTypes) {
			bondItemTypeResponses.add(bondItemTypeResponseDTOMapper(bondItemType));					
		}		
		return bondItemTypeResponses;
	}

	@Override
	public BondItemTypeResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<BondItemType> bondItemType_ = bondItemTypeRepository.findById(id);
	if(bondItemType_.isEmpty()) {
		throw new NotFoundException("Good Type not found");
	}		
	return bondItemTypeResponseDTOMapper(bondItemType_.get());	
	}

	@Override
	public BondItemTypeResponseDTO createBondItemType(BondItemTypeRequestDTO bondItemTypeRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateBondItemTypeData(bondItemTypeRequest)) {
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
		BondItemType bondItemType = new BondItemType();		
		bondItemType.setCode(String.valueOf(Math.random()));
		bondItemType.setName(bondItemTypeRequest.getName());
		bondItemType.setDailyPrice(bondItemTypeRequest.getDailyPrice());
		bondItemType.setCompany(company_.get());
		
		bondItemType.setCurrency(java.util.Currency.getInstance(bondItemTypeRequest.getCurrency().getCurrencyCode()));
		
		bondItemType.setCreatedByUser(userService.getUser(request));
		bondItemType.setCreatedDateTime(dayService.getTimeStamp());
		
		bondItemType = bondItemTypeRepository.save(bondItemType);
		/**Create  parking zone code*/
		bondItemType.setCode("BIT/"+ bondItemType.getId().toString());
		bondItemType = bondItemTypeRepository.save(bondItemType);
		
		return bondItemTypeResponseDTOMapper(bondItemType);		
	}

	@Override
	public BondItemTypeResponseDTO updateBondItemType(BondItemTypeRequestDTO bondItemTypeRequest, HttpServletRequest request) {
		
		Optional<BondItemType> bondItemType_ = bondItemTypeRepository.findById(bondItemTypeRequest.getId());
		if(bondItemType_.isEmpty()) {
			throw new NotFoundException("Good Type not found in database");
		}
		
		if(!validateBondItemTypeData(bondItemTypeRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		BondItemType bondItemType = bondItemType_.get();
		bondItemType.setName(bondItemTypeRequest.getName());
		bondItemType.setDailyPrice(bondItemTypeRequest.getDailyPrice());
		
		bondItemType.setCurrency(java.util.Currency.getInstance(bondItemTypeRequest.getCurrency().getCurrencyCode()));

				
		bondItemType = bondItemTypeRepository.save(bondItemType);
		
		return bondItemTypeResponseDTOMapper(bondItemType);	
		
	}

	@Override
	public ApiCustomResponse activateBondItemType(BondItemTypeRequestDTO bondItemTypeRequest, HttpServletRequest request) {
		Optional<BondItemType> bondItemType_ = bondItemTypeRepository.findById(bondItemTypeRequest.getId());		
		if(bondItemType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(bondItemType_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		bondItemType_.get().setActive(true);
		bondItemTypeRepository.save(bondItemType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateBondItemType(BondItemTypeRequestDTO bondItemTypeRequest, HttpServletRequest request) {
		Optional<BondItemType> bondItemType_ = bondItemTypeRepository.findById(bondItemTypeRequest.getId());		
		if(bondItemType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(bondItemType_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		bondItemType_.get().setActive(false);
		bondItemTypeRepository.save(bondItemType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type dectivated successifully");
	}
	
	
	private BondItemTypeResponseDTO bondItemTypeResponseDTOMapper(BondItemType bondItemType) {
		BondItemTypeResponseDTO bondItemTypeResponse = new BondItemTypeResponseDTO();
		
		bondItemTypeResponse.setId(bondItemType.getId().toString());
		bondItemTypeResponse.setCode(bondItemType.getCode());
		bondItemTypeResponse.setName(bondItemType.getName());
		bondItemTypeResponse.setDailyPrice(String.valueOf(bondItemType.getDailyPrice()));
		bondItemTypeResponse.setCompanyName(bondItemType.getCompany().getName());
		bondItemTypeResponse.setCurrency(bondItemType.getCurrency().toString());
	
		if(bondItemType.isActive()) {
			bondItemTypeResponse.setActive("Active");
		}else {
			bondItemTypeResponse.setActive("Inactive");
		}
		
		return bondItemTypeResponse;
	}
	
	boolean validateBondItemTypeData(BondItemTypeRequestDTO bondItemTypeRequest) {
		
		return true;
	}
}
