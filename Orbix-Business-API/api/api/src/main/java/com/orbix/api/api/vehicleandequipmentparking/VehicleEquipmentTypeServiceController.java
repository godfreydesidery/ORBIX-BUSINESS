package com.orbix.api.api.vehicleandequipmentparking;

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
public class VehicleEquipmentTypeServiceController implements VehicleEquipmentTypeService {

	private final VehicleEquipmentTypeRepository vehicleEquipmentTypeRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final SystemProfileRepository systemProfileRepository;
	private final UserService userService;
	private final DayService dayService;
	
	

	@Override
	public List<VehicleEquipmentTypeResponseDTO> getAllVehicleEquipmentTypes(HttpServletRequest request) {
		List<VehicleEquipmentType> vehicleEquipmentTypes = vehicleEquipmentTypeRepository.findAll();
		List<VehicleEquipmentTypeResponseDTO> vehicleEquipmentTypeResponses = new ArrayList<>();

		for(VehicleEquipmentType vehicleEquipmentType : vehicleEquipmentTypes) {
			vehicleEquipmentTypeResponses.add(vehicleEquipmentTypeResponseDTOMapper(vehicleEquipmentType));					
		}		
		return vehicleEquipmentTypeResponses;
	}
	
	@Override
	public List<VehicleEquipmentTypeResponseDTO> getAllCompanyActiveVehicleEquipmentTypes(
			HttpServletRequest request) {
		Company company = userService.getUser(request).getCompany();	
		List<VehicleEquipmentType> vehicleEquipmentTypes = vehicleEquipmentTypeRepository.findAllByCompanyAndActive(company, true);
		List<VehicleEquipmentTypeResponseDTO> vehicleEquipmentTypeResponses = new ArrayList<>();

		for(VehicleEquipmentType vehicleEquipmentType : vehicleEquipmentTypes) {
			vehicleEquipmentTypeResponses.add(vehicleEquipmentTypeResponseDTOMapper(vehicleEquipmentType));					
		}		
		return vehicleEquipmentTypeResponses;
	}

	@Override
	public VehicleEquipmentTypeResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findById(id);
	if(vehicleEquipmentType_.isEmpty()) {
		throw new NotFoundException("Parking Zone not found");
	}		
	return vehicleEquipmentTypeResponseDTOMapper(vehicleEquipmentType_.get());	
	}

	@Override
	public VehicleEquipmentTypeResponseDTO createVehicleEquipmentType(VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateVehicleEquipmentTypeData(vehicleEquipmentTypeRequest)) {
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
		VehicleEquipmentType vehicleEquipmentType = new VehicleEquipmentType();		
		vehicleEquipmentType.setCode(String.valueOf(Math.random()));
		vehicleEquipmentType.setName(vehicleEquipmentTypeRequest.getName());
		vehicleEquipmentType.setDailyPrice(vehicleEquipmentTypeRequest.getDailyPrice());
		vehicleEquipmentType.setCompany(company_.get());
		
		vehicleEquipmentType.setCreatedByUser(userService.getUser(request));
		vehicleEquipmentType.setCreatedDateTime(dayService.getTimeStamp());
		
		vehicleEquipmentType = vehicleEquipmentTypeRepository.save(vehicleEquipmentType);
		/**Create  parking zone code*/
		vehicleEquipmentType.setCode("VEQ/"+ vehicleEquipmentType.getId().toString());
		vehicleEquipmentType = vehicleEquipmentTypeRepository.save(vehicleEquipmentType);
		
		return vehicleEquipmentTypeResponseDTOMapper(vehicleEquipmentType);		
	}

	@Override
	public VehicleEquipmentTypeResponseDTO updateVehicleEquipmentType(VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest, HttpServletRequest request) {
		
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findById(vehicleEquipmentTypeRequest.getId());
		if(vehicleEquipmentType_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found in database");
		}
		
		if(!validateVehicleEquipmentTypeData(vehicleEquipmentTypeRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		VehicleEquipmentType vehicleEquipmentType = vehicleEquipmentType_.get();
		vehicleEquipmentType.setName(vehicleEquipmentTypeRequest.getName());
		vehicleEquipmentType.setDailyPrice(vehicleEquipmentTypeRequest.getDailyPrice());
				
		vehicleEquipmentType = vehicleEquipmentTypeRepository.save(vehicleEquipmentType);
		
		return vehicleEquipmentTypeResponseDTOMapper(vehicleEquipmentType);	
		
	}

	@Override
	public ApiCustomResponse activateVehicleEquipmentType(VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest, HttpServletRequest request) {
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findById(vehicleEquipmentTypeRequest.getId());		
		if(vehicleEquipmentType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(vehicleEquipmentType_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		vehicleEquipmentType_.get().setActive(true);
		vehicleEquipmentTypeRepository.save(vehicleEquipmentType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateVehicleEquipmentType(VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest, HttpServletRequest request) {
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findById(vehicleEquipmentTypeRequest.getId());		
		if(vehicleEquipmentType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(vehicleEquipmentType_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		vehicleEquipmentType_.get().setActive(false);
		vehicleEquipmentTypeRepository.save(vehicleEquipmentType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type dectivated successifully");
	}
	
	
	private VehicleEquipmentTypeResponseDTO vehicleEquipmentTypeResponseDTOMapper(VehicleEquipmentType vehicleEquipmentType) {
		VehicleEquipmentTypeResponseDTO vehicleEquipmentTypeResponse = new VehicleEquipmentTypeResponseDTO();
		
		vehicleEquipmentTypeResponse.setId(vehicleEquipmentType.getId().toString());
		vehicleEquipmentTypeResponse.setCode(vehicleEquipmentType.getCode());
		vehicleEquipmentTypeResponse.setName(vehicleEquipmentType.getName());
		vehicleEquipmentTypeResponse.setDailyPrice(String.valueOf(vehicleEquipmentType.getDailyPrice()));
		vehicleEquipmentTypeResponse.setCompanyName(vehicleEquipmentType.getCompany().getName());
	
		if(vehicleEquipmentType.isActive()) {
			vehicleEquipmentTypeResponse.setActive("Active");
		}else {
			vehicleEquipmentTypeResponse.setActive("Inactive");
		}
		
		return vehicleEquipmentTypeResponse;
	}
	
	boolean validateVehicleEquipmentTypeData(VehicleEquipmentTypeRequestDTO vehicleEquipmentTypeRequest) {
		
		return true;
	}

	

}
