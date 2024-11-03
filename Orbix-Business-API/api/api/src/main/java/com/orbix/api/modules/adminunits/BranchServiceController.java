package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BranchServiceController implements BranchService {
	
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final UserService userService;
	private final DayService dayService;
	
	/**
	 * 
	 */
	@Override
	public List<BranchResponseDTO> getAllBranches(HttpServletRequest request) {
		List<Branch> branches = branchRepository.findAll();
		List<BranchResponseDTO> branchResponses = new ArrayList<>();

		for(Branch branch : branches) {
			branchResponses.add(branchResponseDTOMapper(branch));					
		}		
		return branchResponses;
	}

	/**
	 * 
	 */
	@Override
	public BranchResponseDTO get(
			Long id, 
			HttpServletRequest request) {
		Optional<Branch> _branch = branchRepository.findById(id);
		if(_branch.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}		
		return branchResponseDTOMapper(_branch.get());	
	}
	
	/**
	 * 
	 */
	@Override
	public BranchResponseDTO createBranch(
			BranchRequestDTO branchRequest, 
			HttpServletRequest request) {
		
		Optional<Company> company_ = companyRepository.findById(branchRequest.getCompanyId());
		if(company_.isEmpty()) {
			throw new InvalidEntryException("Company not found in database");
		}
		
		Optional<Branch> parentBranch_ = branchRepository.findById(branchRequest.getParentBranchId());
		if(branchRequest.getParentBranchId() != null) {
			if(parentBranch_.isEmpty()) {
				throw new InvalidEntryException("Branch not found in database");
			}
		}
		
		
		Branch branch = new Branch();
		branch.setCode(branchRequest.getCode());
		branch.setName(branchRequest.getName());
		branch.setType(branchRequest.getType());		
		branch.setCompany(company_.get());
		
		
		branch.setLevel(branchRequest.getLevel());
		branch.setType(branchRequest.getType());
		branch.setPhysicalAddress(branchRequest.getPhysicalAddress());
		branch.setPostalCode(branchRequest.getPostalCode());
		branch.setPostalAddress(branchRequest.getPostalAddress());
		branch.setTelephone(branchRequest.getTelephone());
		branch.setMobile(branchRequest.getMobile());
		branch.setEmail(branchRequest.getEmail());
		branch.setWebsite(branchRequest.getWebsite());
		branch.setFax(branchRequest.getFax());
		branch.setCity(branchRequest.getCity());
		branch.setState(branchRequest.getState());
		branch.setCountry(branchRequest.getCountry());
		branch.setManagerName(branchRequest.getManagerName());
		branch.setOpeningHours(branchRequest.getOpeningHours());
		branch.setNumberOfStaff(branchRequest.getNumberOfStaff());
		branch.setSalesTargets(branchRequest.getSalesTargets());
		branch.setDateEstablished(branchRequest.getDateEstablished());
		branch.setNotes(branchRequest.getNotes());
		
		branch.setTimeZone(company_.get().getTimeZone());
		branch.setCurrency(company_.get().getCurrency());
		
		branch.setCreatedByUser(userService.getUser(request));
		//branch.setCreatedOnDay(dayService.getDay());
		branch.setCreatedDateTime(dayService.getTimeStamp());
		
		branch = branchRepository.save(branch);
		/**Create  company code*/
		branch.setCode("BR/"+ branch.getId().toString());
		branch = branchRepository.save(branch);
		
		
		return branchResponseDTOMapper(branch);
	}

	@Override
	public BranchResponseDTO updateBranch(BranchRequestDTO branchRequest, HttpServletRequest request) {

		Optional<Branch> branch_ = branchRepository.findById(branchRequest.getId());
		if(branch_.isEmpty()) {
			throw new NotFoundException("Branch not be found in database");
		}		
		if(!validateBranchData(branchRequest)) {
			throw new InvalidEntryException("Could not validate branch data");
		}
		
		Branch branch = branch_.get();
		branch.setName(branchRequest.getName());
		branch.setType(branchRequest.getType());		
		//branch.setCompany(company_.get());
		branch.setLevel(branchRequest.getLevel());
		branch.setType(branchRequest.getType());
		branch.setPhysicalAddress(branchRequest.getPhysicalAddress());
		branch.setPostalCode(branchRequest.getPostalCode());
		branch.setPostalAddress(branchRequest.getPostalAddress());
		branch.setTelephone(branchRequest.getTelephone());
		branch.setMobile(branchRequest.getMobile());
		branch.setEmail(branchRequest.getEmail());
		branch.setWebsite(branchRequest.getWebsite());
		branch.setFax(branchRequest.getFax());
		branch.setCity(branchRequest.getCity());
		branch.setState(branchRequest.getState());
		branch.setCountry(branchRequest.getCountry());
		branch.setManagerName(branchRequest.getManagerName());
		branch.setOpeningHours(branchRequest.getOpeningHours());
		branch.setNumberOfStaff(branchRequest.getNumberOfStaff());
		branch.setSalesTargets(branchRequest.getSalesTargets());
		branch.setDateEstablished(branchRequest.getDateEstablished());
		branch.setNotes(branchRequest.getNotes());
		
		branch = branchRepository.save(branch);
		
		return branchResponseDTOMapper(branch);
	}
	
	private BranchResponseDTO branchResponseDTOMapper(Branch branch) {
		BranchResponseDTO branchResponse = new BranchResponseDTO();
		branchResponse.setId(branch.getId().toString());
		branchResponse.setCode(branch.getCode());
		branchResponse.setName(branch.getName());
		branchResponse.setLevel(branch.getLevel());
		branchResponse.setType(branch.getType());
		branchResponse.setPhysicalAddress(branch.getPhysicalAddress());
		branchResponse.setPostalCode(branch.getPostalCode());
		branchResponse.setPostalAddress(branch.getPostalAddress());
		branchResponse.setTelephone(branch.getTelephone());
		branchResponse.setMobile(branch.getMobile());
		branchResponse.setEmail(branch.getEmail());
		branchResponse.setWebsite(branch.getWebsite());
		branchResponse.setFax(branch.getFax());
		branchResponse.setCity(branch.getCity());
		branchResponse.setState(branch.getState());
		branchResponse.setCountry(branch.getCountry());
		branchResponse.setManagerName(branch.getManagerName());
		branchResponse.setOpeningHours(branch.getOpeningHours());
		branchResponse.setNumberOfStaff(branch.getNumberOfStaff());
		branchResponse.setSalesTargets(branch.getSalesTargets());
		branchResponse.setDateEstablished(branch.getDateEstablished());
		branchResponse.setNotes(branch.getNotes());
		
		if(branch.isActive()) {
			branchResponse.setActive("Active");
		}else {
			branchResponse.setActive("Inactive");
		}	
		branchResponse.setCompanyId(branch.getCompany().getId().toString());
		branchResponse.setCompanyName(branch.getCompany().getName());
		return branchResponse;
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateBranch(BranchRequestDTO branch, HttpServletRequest request) {
		Optional<Branch> branch_ = branchRepository.findById(branch.getId());		
		if(branch_.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}		
		if(branch_.get().isActive() == true) {
			throw new InvalidOperationException("Branch already active");
		}
		branch_.get().setActive(true);
		branchRepository.save(branch_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Branch Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateBranch(BranchRequestDTO branch, HttpServletRequest request) {
		Optional<Branch> branch_ = branchRepository.findById(branch.getId());		
		if(branch_.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}		
		if(branch_.get().isActive() == false) {
			throw new InvalidOperationException("Branch already inactive");
		}
		branch_.get().setActive(false);
		branchRepository.save(branch_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Branch Deactivated successifully");
	}
	
	/**
	 * 
	 * @param branchRequest
	 * @return
	 */
	boolean validateBranchData(BranchRequestDTO branchRequest) {
		
		return true;
	}

	

}
