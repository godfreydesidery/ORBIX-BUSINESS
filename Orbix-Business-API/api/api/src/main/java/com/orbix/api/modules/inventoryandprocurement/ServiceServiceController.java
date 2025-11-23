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
import com.orbix.api.modules.adminunits.Shop;
import com.orbix.api.modules.adminunits.ShopRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ServiceServiceController implements ServiceService {
	private final CompanyRepository companyRepository;
	private final ServiceRepository serviceRepository;
	private final UserService userService;
	private final DayService dayService;

	
	/**
	 * 
	 */
	@Override
	public List<ServiceResponseDTO> getAllServicels(HttpServletRequest request) {
		List<Servicel> services = serviceRepository.findAll();
		List<ServiceResponseDTO> serviceResponses = new ArrayList<>();

		for(Servicel service : services) {
			serviceResponses.add(serviceResponseDTOMapper(service));					
		}		
		return serviceResponses;
	}

	/**
	 * 
	 */
	@Override
	public ServiceResponseDTO get(
			Long id, 
			HttpServletRequest request) {
		Optional<Servicel> _service = serviceRepository.findById(id);
		if(_service.isEmpty()) {
			throw new NotFoundException("Service not found");
		}		
		return serviceResponseDTOMapper(_service.get());	
	}
	
	@Override
	public ServiceResponseDTO getCompanyService(
			Long serviceId, 
			HttpServletRequest request) {
		Optional<Servicel> _service = serviceRepository.findByIdAndCompany(serviceId, userService.getUserCompany(request));
		if(_service.isEmpty()) {
			throw new NotFoundException("Service not found");
		}		
		return serviceResponseDTOMapper(_service.get());	
	}
	
	/**
	 * 
	 */
	@Override
	public ServiceResponseDTO createService(
			ServiceRequestDTO serviceRequest, 
			HttpServletRequest request) {
		
		Optional<Company> company_ = companyRepository.findById(userService.getUser(request).getCompany().getId());
		if(company_.isEmpty()) {
			throw new InvalidEntryException("Company not found in database");
		}
		if(serviceRequest.getPrice() <= 0) {
			throw new InvalidEntryException("Invalid price");
		}
		
		Servicel service = new Servicel();
		//service.setCode(serviceRequest.getCode());
		service.setCode(String.valueOf(Math.random()));
		service.setName(serviceRequest.getName());
		service.setDescription(serviceRequest.getDescription());
		service.setBaseUom(serviceRequest.getBaseUom());		
		service.setCompany(company_.get());
		service.setPrice(serviceRequest.getPrice());
		
		service.setSellable(true);
		
		service.setCreatedByUser(userService.getUser(request));
		service.setCreatedDateTime(dayService.getTimeStamp());
		
		service = serviceRepository.save(service);
		/**Create  company code*/
		service.setCode("AC/"+ service.getId().toString());
		service = serviceRepository.save(service);
		
		return serviceResponseDTOMapper(service);
	}

	@Override
	public ServiceResponseDTO updateService(ServiceRequestDTO serviceRequest, HttpServletRequest request) {

		Optional<Servicel> service_ = serviceRepository.findById(serviceRequest.getId());
		if(service_.isEmpty()) {
			throw new NotFoundException("Service not be found in database");
		}		
		if(!validateServiceData(serviceRequest)) {
			throw new InvalidEntryException("Could not validate service data");
		}		
		if(serviceRequest.getPrice() <= 0) {
			throw new InvalidEntryException("Invalid price");
		}
		
		Servicel service = service_.get();
		service.setName(serviceRequest.getName());
		service.setDescription(serviceRequest.getDescription());
		service.setBaseUom(serviceRequest.getBaseUom());
		service.setPrice(serviceRequest.getPrice());
		
		service.setSellable(true);
		
		
		service = serviceRepository.save(service);		
		return serviceResponseDTOMapper(service);
	}
	
	private ServiceResponseDTO serviceResponseDTOMapper(Servicel service) {	
		ServiceResponseDTO serviceResponse = new ServiceResponseDTO();
		serviceResponse.setId(service.getId().toString());
		serviceResponse.setCode(service.getCode());
		serviceResponse.setName(service.getName());
		serviceResponse.setDescription(service.getDescription());
		serviceResponse.setBaseUom(service.getBaseUom());
		serviceResponse.setCompanyId(service.getCompany().getId().toString());	
		serviceResponse.setPrice(String.valueOf(service.getPrice()));
		if(service.isActive()) {
			serviceResponse.setActive("Active");
		}else {
			serviceResponse.setActive("Inactive");
		}
		
		//serviceResponse.setOtherInfo("Company: " + service.getCompany().getName() + " Location: " + service.getLocationName());
		return serviceResponse;
	}
	
	private ServiceResponseDTO serviceResponseDTOMapperWithImportedStatus(Servicel service, boolean importedToShop) {	
		ServiceResponseDTO serviceResponse = new ServiceResponseDTO();
		serviceResponse.setId(service.getId().toString());
		serviceResponse.setCode(service.getCode());
		serviceResponse.setName(service.getName());
		serviceResponse.setDescription(service.getDescription());
		serviceResponse.setBaseUom(service.getBaseUom());
		serviceResponse.setCompanyId(service.getCompany().getId().toString());		
		serviceResponse.setPrice(String.valueOf(service.getPrice()));
		if(service.isActive()) {
			serviceResponse.setActive("Active");
		}else {
			serviceResponse.setActive("Inactive");
		}
	
		//serviceResponse.setOtherInfo("Company: " + service.getCompany().getName() + " Location: " + service.getLocationName());
		return serviceResponse;
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateService(ServiceRequestDTO service, HttpServletRequest request) {
		Optional<Servicel> service_ = serviceRepository.findById(service.getId());		
		if(service_.isEmpty()) {
			throw new NotFoundException("Service not found");
		}		
		if(service_.get().isActive() == true) {
			throw new InvalidOperationException("Service already active");
		}
		service_.get().setActive(true);
		serviceRepository.save(service_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Service Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateService(ServiceRequestDTO service, HttpServletRequest request) {
		Optional<Servicel> service_ = serviceRepository.findById(service.getId());		
		if(service_.isEmpty()) {
			throw new NotFoundException("Service not found");
		}		
		if(service_.get().isActive() == false) {
			throw new InvalidOperationException("Service already inactive");
		}
		service_.get().setActive(false);
		serviceRepository.save(service_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Service Deactivated successifully");
	}
	
	/**
	 * 
	 * @param serviceRequest
	 * @return
	 */
	boolean validateServiceData(ServiceRequestDTO serviceRequest) {
		
		return true;
	}
	
	@Override
	public List<ServiceResponseDTO> getServicesByCompany(String serviceName, HttpServletRequest request) {
	    // Validate input
	    if (serviceName == null || serviceName.trim().isEmpty()) {
	        throw new IllegalArgumentException("Service name cannot be null or empty");
	    }

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }

	    // Fetch services
	    List<Servicel> services = serviceRepository.findAllByCompanyAndNameContainingIgnoreCase(company, serviceName);

	    // Map to DTOs
	    return services.stream()
	        .map(this::serviceResponseDTOMapper)
	        .collect(Collectors.toList());
	}
	
	@Override
	public List<ServiceResponseDTO> getCompanyServices(HttpServletRequest request) {
	    

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }

	    // Fetch services
	    List<Servicel> services = serviceRepository.findAllByCompany(company);

	    // Map to DTOs
	    return services.stream()
	        .map(this::serviceResponseDTOMapper)
	        .collect(Collectors.toList());
	}
	
	@Override
	public List<ServiceResponseDTO> getServicesByCompanyAndName(String serviceName, HttpServletRequest request) {
		
	    List<Servicel> services = serviceRepository.findAllByCompanyAndNameContainingIgnoreCase(userService.getUserCompany(request), serviceName);
	    
	    List<ServiceResponseDTO> serviceResponses = new ArrayList<>();
	    
	    for(Servicel service : services) {
	    	ServiceResponseDTO serviceResponse = new ServiceResponseDTO();
	    	serviceResponse.setId(service.getId().toString());
	    	serviceResponse.setName(service.getName());
	    	serviceResponses.add(serviceResponse);
	    }
	    return serviceResponses;
	}
}
