package com.orbix.api.modules.vehicleandequipmentmaintenance;

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
public class MaintenanceIssueTypeServiceController implements MaintenanceIssueTypeService {
	
	private final MaintenanceIssueTypeRepository maintenanceIssueTypeRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final SystemProfileRepository systemProfileRepository;
	private final UserService userService;
	private final DayService dayService;
	
	

	@Override
	public List<MaintenanceIssueTypeResponseDTO> getAllMaintenanceIssueTypes(HttpServletRequest request) {
		List<MaintenanceIssueType> maintenanceIssueTypes = maintenanceIssueTypeRepository.findAll();
		List<MaintenanceIssueTypeResponseDTO> maintenanceIssueTypeResponses = new ArrayList<>();

		for(MaintenanceIssueType maintenanceIssueType : maintenanceIssueTypes) {
			maintenanceIssueTypeResponses.add(maintenanceIssueTypeResponseDTOMapper(maintenanceIssueType));					
		}		
		return maintenanceIssueTypeResponses;
	}
	
	@Override
	public List<MaintenanceIssueTypeResponseDTO> getAllCompanyActiveMaintenanceIssueTypes(
			HttpServletRequest request) {
		Company company = userService.getUser(request).getCompany();	
		List<MaintenanceIssueType> maintenanceIssueTypes = maintenanceIssueTypeRepository.findAllByCompanyAndActive(company, true);
		List<MaintenanceIssueTypeResponseDTO> maintenanceIssueTypeResponses = new ArrayList<>();

		for(MaintenanceIssueType maintenanceIssueType : maintenanceIssueTypes) {
			maintenanceIssueTypeResponses.add(maintenanceIssueTypeResponseDTOMapper(maintenanceIssueType));					
		}		
		return maintenanceIssueTypeResponses;
	}

	@Override
	public MaintenanceIssueTypeResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<MaintenanceIssueType> maintenanceIssueType_ = maintenanceIssueTypeRepository.findById(id);
	if(maintenanceIssueType_.isEmpty()) {
		throw new NotFoundException("Parking Zone not found");
	}		
	return maintenanceIssueTypeResponseDTOMapper(maintenanceIssueType_.get());	
	}

	@Override
	public MaintenanceIssueTypeResponseDTO createMaintenanceIssueType(MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateMaintenanceIssueTypeData(maintenanceIssueTypeRequest)) {
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
		MaintenanceIssueType maintenanceIssueType = new MaintenanceIssueType();		
		maintenanceIssueType.setCode(String.valueOf(Math.random()));
		maintenanceIssueType.setName(maintenanceIssueTypeRequest.getName());
		maintenanceIssueType.setCompany(company_.get());
		
		maintenanceIssueType.setCreatedByUser(userService.getUser(request));
		maintenanceIssueType.setCreatedDateTime(dayService.getTimeStamp());
		
		maintenanceIssueType = maintenanceIssueTypeRepository.save(maintenanceIssueType);
		/**Create  parking zone code*/
		maintenanceIssueType.setCode("MIT/"+ maintenanceIssueType.getId().toString());
		maintenanceIssueType = maintenanceIssueTypeRepository.save(maintenanceIssueType);
		
		return maintenanceIssueTypeResponseDTOMapper(maintenanceIssueType);		
	}

	@Override
	public MaintenanceIssueTypeResponseDTO updateMaintenanceIssueType(MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest, HttpServletRequest request) {
		
		Optional<MaintenanceIssueType> maintenanceIssueType_ = maintenanceIssueTypeRepository.findById(maintenanceIssueTypeRequest.getId());
		if(maintenanceIssueType_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found in database");
		}
		
		if(!validateMaintenanceIssueTypeData(maintenanceIssueTypeRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		MaintenanceIssueType maintenanceIssueType = maintenanceIssueType_.get();
		maintenanceIssueType.setName(maintenanceIssueTypeRequest.getName());
				
		maintenanceIssueType = maintenanceIssueTypeRepository.save(maintenanceIssueType);
		
		return maintenanceIssueTypeResponseDTOMapper(maintenanceIssueType);	
		
	}

	@Override
	public ApiCustomResponse activateMaintenanceIssueType(MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest, HttpServletRequest request) {
		Optional<MaintenanceIssueType> maintenanceIssueType_ = maintenanceIssueTypeRepository.findById(maintenanceIssueTypeRequest.getId());		
		if(maintenanceIssueType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(maintenanceIssueType_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		maintenanceIssueType_.get().setActive(true);
		maintenanceIssueTypeRepository.save(maintenanceIssueType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateMaintenanceIssueType(MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest, HttpServletRequest request) {
		Optional<MaintenanceIssueType> maintenanceIssueType_ = maintenanceIssueTypeRepository.findById(maintenanceIssueTypeRequest.getId());		
		if(maintenanceIssueType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(maintenanceIssueType_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		maintenanceIssueType_.get().setActive(false);
		maintenanceIssueTypeRepository.save(maintenanceIssueType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type dectivated successifully");
	}
	
	
	private MaintenanceIssueTypeResponseDTO maintenanceIssueTypeResponseDTOMapper(MaintenanceIssueType maintenanceIssueType) {
		MaintenanceIssueTypeResponseDTO maintenanceIssueTypeResponse = new MaintenanceIssueTypeResponseDTO();
		
		maintenanceIssueTypeResponse.setId(maintenanceIssueType.getId().toString());
		maintenanceIssueTypeResponse.setCode(maintenanceIssueType.getCode());
		maintenanceIssueTypeResponse.setName(maintenanceIssueType.getName());
		maintenanceIssueTypeResponse.setCompanyName(maintenanceIssueType.getCompany().getName());
	
		if(maintenanceIssueType.isActive()) {
			maintenanceIssueTypeResponse.setActive("Active");
		}else {
			maintenanceIssueTypeResponse.setActive("Inactive");
		}
		
		return maintenanceIssueTypeResponse;
	}
	
	boolean validateMaintenanceIssueTypeData(MaintenanceIssueTypeRequestDTO maintenanceIssueTypeRequest) {
		
		return true;
	}
}
