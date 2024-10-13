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
public class VehicleAndEquipmentTypeServiceController implements VehicleAndEquipmentTypeService {

	private final VehicleAndEquipmentTypeRepository vehicleAndEquipmentTypeRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final SystemProfileRepository systemProfileRepository;
	private final UserService userService;
	private final DayService dayService;
	
	

	@Override
	public List<VehicleAndEquipmentTypeResponseDTO> getAllVehicleAndEquipmentTypes(HttpServletRequest request) {
		List<VehicleAndEquipmentType> vehicleAndEquipmentTypes = vehicleAndEquipmentTypeRepository.findAll();
		List<VehicleAndEquipmentTypeResponseDTO> vehicleAndEquipmentTypeResponses = new ArrayList<>();

		for(VehicleAndEquipmentType vehicleAndEquipmentType : vehicleAndEquipmentTypes) {
			vehicleAndEquipmentTypeResponses.add(vehicleAndEquipmentTypeResponseDTOMapper(vehicleAndEquipmentType));					
		}		
		return vehicleAndEquipmentTypeResponses;
	}

	@Override
	public VehicleAndEquipmentTypeResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<VehicleAndEquipmentType> vehicleAndEquipmentType_ = vehicleAndEquipmentTypeRepository.findById(id);
	if(vehicleAndEquipmentType_.isEmpty()) {
		throw new NotFoundException("Parking Zone not found");
	}		
	return vehicleAndEquipmentTypeResponseDTOMapper(vehicleAndEquipmentType_.get());	
	}

	@Override
	public VehicleAndEquipmentTypeResponseDTO createVehicleAndEquipmentType(VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateVehicleAndEquipmentTypeData(vehicleAndEquipmentTypeRequest)) {
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
		VehicleAndEquipmentType vehicleAndEquipmentType = new VehicleAndEquipmentType();		
		vehicleAndEquipmentType.setCode(String.valueOf(Math.random()));
		vehicleAndEquipmentType.setName(vehicleAndEquipmentTypeRequest.getName());
		vehicleAndEquipmentType.setDailyPrice(vehicleAndEquipmentTypeRequest.getDailyPrice());
		vehicleAndEquipmentType.setCompany(company_.get());
		
		vehicleAndEquipmentType.setCreatedByUser(userService.getUser(request));
		vehicleAndEquipmentType.setCreatedDateTime(dayService.getTimeStamp());
		
		vehicleAndEquipmentType = vehicleAndEquipmentTypeRepository.save(vehicleAndEquipmentType);
		/**Create  parking zone code*/
		vehicleAndEquipmentType.setCode("VEQ/"+ vehicleAndEquipmentType.getId().toString());
		vehicleAndEquipmentType = vehicleAndEquipmentTypeRepository.save(vehicleAndEquipmentType);
		
		return vehicleAndEquipmentTypeResponseDTOMapper(vehicleAndEquipmentType);		
	}

	@Override
	public VehicleAndEquipmentTypeResponseDTO updateVehicleAndEquipmentType(VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest, HttpServletRequest request) {
		
		Optional<VehicleAndEquipmentType> vehicleAndEquipmentType_ = vehicleAndEquipmentTypeRepository.findById(vehicleAndEquipmentTypeRequest.getId());
		if(vehicleAndEquipmentType_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found in database");
		}
		
		if(!validateVehicleAndEquipmentTypeData(vehicleAndEquipmentTypeRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		VehicleAndEquipmentType vehicleAndEquipmentType = vehicleAndEquipmentType_.get();
		vehicleAndEquipmentType.setName(vehicleAndEquipmentTypeRequest.getName());
		vehicleAndEquipmentType.setDailyPrice(vehicleAndEquipmentTypeRequest.getDailyPrice());
				
		vehicleAndEquipmentType = vehicleAndEquipmentTypeRepository.save(vehicleAndEquipmentType);
		
		return vehicleAndEquipmentTypeResponseDTOMapper(vehicleAndEquipmentType);	
		
	}

	@Override
	public ApiCustomResponse activateVehicleAndEquipmentType(VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest, HttpServletRequest request) {
		Optional<VehicleAndEquipmentType> vehicleAndEquipmentType_ = vehicleAndEquipmentTypeRepository.findById(vehicleAndEquipmentTypeRequest.getId());		
		if(vehicleAndEquipmentType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(vehicleAndEquipmentType_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		vehicleAndEquipmentType_.get().setActive(true);
		vehicleAndEquipmentTypeRepository.save(vehicleAndEquipmentType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateVehicleAndEquipmentType(VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest, HttpServletRequest request) {
		Optional<VehicleAndEquipmentType> vehicleAndEquipmentType_ = vehicleAndEquipmentTypeRepository.findById(vehicleAndEquipmentTypeRequest.getId());		
		if(vehicleAndEquipmentType_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(vehicleAndEquipmentType_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		vehicleAndEquipmentType_.get().setActive(false);
		vehicleAndEquipmentTypeRepository.save(vehicleAndEquipmentType_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type dectivated successifully");
	}
	
	
	private VehicleAndEquipmentTypeResponseDTO vehicleAndEquipmentTypeResponseDTOMapper(VehicleAndEquipmentType vehicleAndEquipmentType) {
		VehicleAndEquipmentTypeResponseDTO vehicleAndEquipmentTypeResponse = new VehicleAndEquipmentTypeResponseDTO();
		
		vehicleAndEquipmentTypeResponse.setId(vehicleAndEquipmentType.getId().toString());
		vehicleAndEquipmentTypeResponse.setCode(vehicleAndEquipmentType.getCode());
		vehicleAndEquipmentTypeResponse.setName(vehicleAndEquipmentType.getName());
		vehicleAndEquipmentTypeResponse.setDailyPrice(String.valueOf(vehicleAndEquipmentType.getDailyPrice()));
		vehicleAndEquipmentTypeResponse.setCompanyName(vehicleAndEquipmentType.getCompany().getName());
	
		if(vehicleAndEquipmentType.isActive()) {
			vehicleAndEquipmentTypeResponse.setActive("Active");
		}else {
			vehicleAndEquipmentTypeResponse.setActive("Inactive");
		}
		
		return vehicleAndEquipmentTypeResponse;
	}
	
	boolean validateVehicleAndEquipmentTypeData(VehicleAndEquipmentTypeRequestDTO vehicleAndEquipmentTypeRequest) {
		
		return true;
	}

}
