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
import com.orbix.api.modules.adminunits.Shop;
import com.orbix.api.modules.adminunits.ShopResponseDTO;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BondZoneServiceController implements BondZoneService {
	private final BondZoneRepository bondZoneRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public List<BondZoneResponseDTO> getAllBondZones(HttpServletRequest request) {
		List<BondZone> bondZones = bondZoneRepository.findAll();
		List<BondZoneResponseDTO> bondZoneResponses = new ArrayList<>();

		for(BondZone bondZone : bondZones) {
			bondZoneResponses.add(bondZoneResponseDTOMapper(bondZone));					
		}		
		return bondZoneResponses;
	}
	
	@Override
	public List<BondZoneResponseDTO> getAllBranchActiveBondZones(
			HttpServletRequest request) {
		Branch branch = userService.getUser(request).getBranch();	
		List<BondZone> bondZones = bondZoneRepository.findAllByBranchAndActive(branch, true);
		List<BondZoneResponseDTO> bondZoneResponses = new ArrayList<>();

		for(BondZone bondZone : bondZones) {
			bondZoneResponses.add(bondZoneResponseDTOMapper(bondZone));					
		}		
		return bondZoneResponses;
	}

	@Override
	public BondZoneResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<BondZone> bondZone_ = bondZoneRepository.findById(id);
	if(bondZone_.isEmpty()) {
		throw new NotFoundException("Bond Zone not found");
	}		
	return bondZoneResponseDTOMapper(bondZone_.get());	
	}

	@Override
	public BondZoneResponseDTO createBondZone(BondZoneRequestDTO bondZoneRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateBondZoneData(bondZoneRequest)) {
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
		BondZone bondZone = new BondZone();		
		bondZone.setCode(String.valueOf(Math.random()));
		bondZone.setName(bondZoneRequest.getName());
		bondZone.setNoOfSections(bondZoneRequest.getNoOfSections());
		//bondZone.setCompany(_company.get());
		bondZone.setBranch(_branch.get());
		
		bondZone.setCreatedByUser(userService.getUser(request));
		bondZone.setCreatedDateTime(dayService.getTimeStamp());
		
		bondZone = bondZoneRepository.save(bondZone);
		/**Create  parking zone code*/
		bondZone.setCode("BDZ/"+ bondZone.getId().toString());
		bondZone = bondZoneRepository.save(bondZone);
		
		return bondZoneResponseDTOMapper(bondZone);		
	}

	@Override
	public BondZoneResponseDTO updateBondZone(BondZoneRequestDTO bondZoneRequest, HttpServletRequest request) {
		
		Optional<BondZone> bondZone_ = bondZoneRepository.findById(bondZoneRequest.getId());
		if(bondZone_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found in database");
		}
		
		if(!validateBondZoneData(bondZoneRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		BondZone bondZone = bondZone_.get();
		bondZone.setName(bondZoneRequest.getName());
		bondZone.setNoOfSections(bondZoneRequest.getNoOfSections());
				
		bondZone = bondZoneRepository.save(bondZone);
		
		return bondZoneResponseDTOMapper(bondZone);	
		
	}

	@Override
	public ApiCustomResponse activateBondZone(BondZoneRequestDTO bondZoneRequest, HttpServletRequest request) {
		Optional<BondZone> bondZone_ = bondZoneRepository.findById(bondZoneRequest.getId());		
		if(bondZone_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found");
		}		
		if(bondZone_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		bondZone_.get().setActive(true);
		bondZoneRepository.save(bondZone_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Parking Zone Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateBondZone(BondZoneRequestDTO bondZoneRequest, HttpServletRequest request) {
		Optional<BondZone> bondZone_ = bondZoneRepository.findById(bondZoneRequest.getId());		
		if(bondZone_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found");
		}		
		if(bondZone_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		bondZone_.get().setActive(false);
		bondZoneRepository.save(bondZone_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Parking Zone dectivated successifully");
	}
	
	
	private BondZoneResponseDTO bondZoneResponseDTOMapper(BondZone bondZone) {
		BondZoneResponseDTO bondZoneResponse = new BondZoneResponseDTO();
		
		bondZoneResponse.setId(bondZone.getId().toString());
		bondZoneResponse.setCode(bondZone.getCode());
		bondZoneResponse.setName(bondZone.getName());
		bondZoneResponse.setNoOfSections(String.valueOf(bondZone.getNoOfSections()));
		//bondZoneResponse.setCompanyName(bondZone.getCompany().getName());
		bondZoneResponse.setBranchName(bondZone.getBranch().getName());
	
		if(bondZone.isActive()) {
			bondZoneResponse.setActive("Active");
		}else {
			bondZoneResponse.setActive("Inactive");
		}
		
		return bondZoneResponse;
	}
	
	boolean validateBondZoneData(BondZoneRequestDTO bondZoneRequest) {
		
		return true;
	}
	
	@Override
	public List<BondZoneResponseDTO> getBranchAvailableBondZonesByUser(HttpServletRequest request) {
		User user = userService.getUser(request);
		List<BondZone> bondZones = bondZoneRepository.findAllByBranch(user.getBranch());
		
		List<BondZoneResponseDTO> bondZoneResponses = new ArrayList<>();

		for(BondZone bondZone : bondZones) {
			bondZoneResponses.add(bondZoneResponseDTOMapper(bondZone));
		}		
		return bondZoneResponses;
	}


}
