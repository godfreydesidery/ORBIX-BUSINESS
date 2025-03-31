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
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ServiceSpecialistServiceController implements ServiceSpecialistService {
	private final ServiceSpecialistRepository serviceSpecialistRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final SystemProfileRepository systemProfileRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final UserRepository userRepository;
	
	

	@Override
	public List<ServiceSpecialistResponseDTO> getAllServiceSpecialists(HttpServletRequest request) {
		List<ServiceSpecialist> serviceSpecialists = serviceSpecialistRepository.findAll();
		List<ServiceSpecialistResponseDTO> serviceSpecialistResponses = new ArrayList<>();

		for(ServiceSpecialist serviceSpecialist : serviceSpecialists) {
			serviceSpecialistResponses.add(serviceSpecialistResponseDTOMapper(serviceSpecialist));					
		}		
		return serviceSpecialistResponses;
	}
	
	@Override
	public List<ServiceSpecialistResponseDTO> getAllBranchActiveServiceSpecialists(
			HttpServletRequest request) {
		Branch branch = userService.getUser(request).getBranch();	
		List<ServiceSpecialist> serviceSpecialists = serviceSpecialistRepository.findAllByBranchAndActive(branch, true);
		List<ServiceSpecialistResponseDTO> serviceSpecialistResponses = new ArrayList<>();

		for(ServiceSpecialist serviceSpecialist : serviceSpecialists) {
			serviceSpecialistResponses.add(serviceSpecialistResponseDTOMapper(serviceSpecialist));					
		}		
		return serviceSpecialistResponses;
	}

	@Override
	public ServiceSpecialistResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<ServiceSpecialist> serviceSpecialist_ = serviceSpecialistRepository.findById(id);
	if(serviceSpecialist_.isEmpty()) {
		throw new NotFoundException("Parking Zone not found");
	}		
	return serviceSpecialistResponseDTOMapper(serviceSpecialist_.get());	
	}

	@Override
	public ServiceSpecialistResponseDTO createServiceSpecialist(ServiceSpecialistRequestDTO serviceSpecialistRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateServiceSpecialistData(serviceSpecialistRequest)) {
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
		
		Optional<User> user_ = userRepository.findByNickname(serviceSpecialistRequest.getNickname());
		if(user_.isEmpty()) {
			throw new NotFoundException("User not found");
		}
		
		if(user_.get().getBranch().getId() != branch_.get().getId()) {
			throw new InvalidOperationException("User does not belong to this branch");
		}
		
		if(serviceSpecialistRepository.existsByUserAndBranch(user_.get(), branch_.get())) {
			throw new InvalidOperationException("User already registered in this branch");
		}
		
		/**New parking zone*/
		ServiceSpecialist serviceSpecialist = new ServiceSpecialist();		
		serviceSpecialist.setUser(user_.get());
		serviceSpecialist.setBranch(branch_.get());
		
		serviceSpecialist.setCreatedByUser(userService.getUser(request));
		serviceSpecialist.setCreatedDateTime(dayService.getTimeStamp());
		
		serviceSpecialist = serviceSpecialistRepository.save(serviceSpecialist);
		/**Create  parking zone code*/
		serviceSpecialist = serviceSpecialistRepository.save(serviceSpecialist);
		
		return serviceSpecialistResponseDTOMapper(serviceSpecialist);		
	}

	@Override
	public ServiceSpecialistResponseDTO updateServiceSpecialist(ServiceSpecialistRequestDTO serviceSpecialistRequest, HttpServletRequest request) {
		
		Optional<ServiceSpecialist> serviceSpecialist_ = serviceSpecialistRepository.findById(serviceSpecialistRequest.getId());
		if(serviceSpecialist_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found in database");
		}
		
		if(!validateServiceSpecialistData(serviceSpecialistRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		ServiceSpecialist serviceSpecialist = serviceSpecialist_.get();
				
		serviceSpecialist = serviceSpecialistRepository.save(serviceSpecialist);
		
		return serviceSpecialistResponseDTOMapper(serviceSpecialist);	
		
	}

	@Override
	public ApiCustomResponse activateServiceSpecialist(ServiceSpecialistRequestDTO serviceSpecialistRequest, HttpServletRequest request) {
		Optional<ServiceSpecialist> serviceSpecialist_ = serviceSpecialistRepository.findById(serviceSpecialistRequest.getId());		
		if(serviceSpecialist_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(serviceSpecialist_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		serviceSpecialist_.get().setActive(true);
		serviceSpecialistRepository.save(serviceSpecialist_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateServiceSpecialist(ServiceSpecialistRequestDTO serviceSpecialistRequest, HttpServletRequest request) {
		Optional<ServiceSpecialist> serviceSpecialist_ = serviceSpecialistRepository.findById(serviceSpecialistRequest.getId());		
		if(serviceSpecialist_.isEmpty()) {
			throw new NotFoundException("Type not found");
		}		
		if(serviceSpecialist_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		serviceSpecialist_.get().setActive(false);
		serviceSpecialistRepository.save(serviceSpecialist_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Type dectivated successifully");
	}
	
	
	private ServiceSpecialistResponseDTO serviceSpecialistResponseDTOMapper(ServiceSpecialist serviceSpecialist) {
		ServiceSpecialistResponseDTO serviceSpecialistResponse = new ServiceSpecialistResponseDTO();
		
		serviceSpecialistResponse.setId(serviceSpecialist.getId().toString());
		serviceSpecialistResponse.setCode(serviceSpecialist.getUser().getCode());
		serviceSpecialistResponse.setNickname(serviceSpecialist.getUser().getNickname());
		serviceSpecialistResponse.setBranchName(serviceSpecialist.getBranch().getName());
	
		if(serviceSpecialist.isActive()) {
			serviceSpecialistResponse.setActive("Active");
		}else {
			serviceSpecialistResponse.setActive("Inactive");
		}
		
		return serviceSpecialistResponse;
	}
	
	boolean validateServiceSpecialistData(ServiceSpecialistRequestDTO serviceSpecialistRequest) {
		
		return true;
	}
}
