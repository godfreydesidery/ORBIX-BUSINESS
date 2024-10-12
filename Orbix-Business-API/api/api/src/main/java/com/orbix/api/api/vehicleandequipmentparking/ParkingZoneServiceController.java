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
import com.orbix.api.modules.adminunits.CompanyRequestDTO;
import com.orbix.api.modules.adminunits.CompanyResponseDTO;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.SystemProfileRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ParkingZoneServiceController implements ParkingZoneService {
	
	private final ParkingZoneRepository parkingZoneRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final SystemProfileRepository systemProfileRepository;
	private final UserService userService;
	private final DayService dayService;
	
	

	@Override
	public List<ParkingZoneResponseDTO> getAllParkingZones(HttpServletRequest request) {
		List<ParkingZone> parkingZones = parkingZoneRepository.findAll();
		List<ParkingZoneResponseDTO> parkingZoneResponses = new ArrayList<>();

		for(ParkingZone parkingZone : parkingZones) {
			parkingZoneResponses.add(parkingZoneResponseDTOMapper(parkingZone));					
		}		
		return parkingZoneResponses;
	}

	@Override
	public ParkingZoneResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findById(id);
	if(parkingZone_.isEmpty()) {
		throw new NotFoundException("Parking Zone not found");
	}		
	return parkingZoneResponseDTOMapper(parkingZone_.get());	
	}

	@Override
	public ParkingZoneResponseDTO createParkingZone(ParkingZoneRequestDTO parkingZoneRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateParkingZoneData(parkingZoneRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		
		
		
		Optional<Company> _company = companyRepository.findById(userService.getUserCompany(request).getId());
		if(_company.isEmpty()) {
			throw new NotFoundException("Company not found");
		}
		Optional<Branch> _branch = branchRepository.findById(userService.getUserBranch(request).getId());
		if(_branch.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}
		
		/**New parking zone*/
		ParkingZone parkingZone = new ParkingZone();		
		parkingZone.setCode(String.valueOf(Math.random()));
		parkingZone.setName(parkingZoneRequest.getName());
		parkingZone.setNoOfSlots(parkingZoneRequest.getNoOfSlots());
		parkingZone.setCompany(_company.get());
		parkingZone.setBranch(_branch.get());
		
		parkingZone.setCreatedByUser(userService.getUser(request));
		parkingZone.setCreatedDateTime(dayService.getTimeStamp());
		
		parkingZone = parkingZoneRepository.save(parkingZone);
		/**Create  parking zone code*/
		parkingZone.setCode("PKZ/"+ parkingZone.getId().toString());
		parkingZone = parkingZoneRepository.save(parkingZone);
		
		return parkingZoneResponseDTOMapper(parkingZone);		
	}

	@Override
	public ParkingZoneResponseDTO updateParkingZone(ParkingZoneRequestDTO parkingZoneRequest, HttpServletRequest request) {
		
		Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findById(parkingZoneRequest.getId());
		if(parkingZone_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found in database");
		}
		
		if(!validateParkingZoneData(parkingZoneRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		ParkingZone parkingZone = parkingZone_.get();
		parkingZone.setName(parkingZoneRequest.getName());
		parkingZone.setNoOfSlots(parkingZoneRequest.getNoOfSlots());
				
		parkingZone = parkingZoneRepository.save(parkingZone);
		
		return parkingZoneResponseDTOMapper(parkingZone);	
		
	}

	@Override
	public ApiCustomResponse activateParkingZone(ParkingZoneRequestDTO parkingZoneRequest, HttpServletRequest request) {
		Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findById(parkingZoneRequest.getId());		
		if(parkingZone_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found");
		}		
		if(parkingZone_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		parkingZone_.get().setActive(true);
		parkingZoneRepository.save(parkingZone_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Parking Zone Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateParkingZone(ParkingZoneRequestDTO parkingZoneRequest, HttpServletRequest request) {
		Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findById(parkingZoneRequest.getId());		
		if(parkingZone_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found");
		}		
		if(parkingZone_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		parkingZone_.get().setActive(false);
		parkingZoneRepository.save(parkingZone_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Parking Zone dectivated successifully");
	}
	
	
	private ParkingZoneResponseDTO parkingZoneResponseDTOMapper(ParkingZone parkingZone) {
		ParkingZoneResponseDTO parkingZoneResponse = new ParkingZoneResponseDTO();
		
		parkingZoneResponse.setId(parkingZone.getId().toString());
		parkingZoneResponse.setCode(parkingZone.getCode());
		parkingZoneResponse.setName(parkingZone.getName());
		parkingZoneResponse.setNoOfSlots(String.valueOf(parkingZone.getNoOfSlots()));
		parkingZoneResponse.setCompanyName(parkingZone.getCompany().getName());
		parkingZoneResponse.setBranchName(parkingZone.getBranch().getName());
	
		if(parkingZone.isActive()) {
			parkingZoneResponse.setActive("Active");
		}else {
			parkingZoneResponse.setActive("Inactive");
		}
		
		return parkingZoneResponse;
	}
	
	boolean validateParkingZoneData(ParkingZoneRequestDTO parkingZoneRequest) {
		
		return true;
	}


}
