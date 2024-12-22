package com.orbix.api.modules.inventoryandprocurement;

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
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UomServiceController implements UomService {
	private final CompanyRepository companyRepository;
	private final UomRepository uomRepository;
	private final UserService userService;
	private final DayService dayService;
	
	/**
	 * 
	 */
	@Override
	public List<UomResponseDTO> getAllUomes(HttpServletRequest request) {
		List<Uom> uomes = uomRepository.findAll();
		List<UomResponseDTO> uomResponses = new ArrayList<>();

		for(Uom uom : uomes) {
			uomResponses.add(uomResponseDTOMapper(uom));					
		}		
		return uomResponses;
	}

	/**
	 * 
	 */
	@Override
	public UomResponseDTO get(
			Long id, 
			HttpServletRequest request) {
		Optional<Uom> _uom = uomRepository.findById(id);
		if(_uom.isEmpty()) {
			throw new NotFoundException("Uom not found");
		}		
		return uomResponseDTOMapper(_uom.get());	
	}
	
	/**
	 * 
	 */
	@Override
	public UomResponseDTO createUom(
			UomRequestDTO uomRequest, 
			HttpServletRequest request) {
		
		Optional<Company> company_ = companyRepository.findById(userService.getUser(request).getCompany().getId());
		if(company_.isEmpty()) {
			throw new InvalidEntryException("Company not found in database");
		}
		
		Uom uom = new Uom();
		uom.setCode(uomRequest.getCode());
		uom.setName(uomRequest.getName());
		uom.setType(uomRequest.getType());		
		uom.setCompany(company_.get());
		
		uom.setCreatedByUser(userService.getUser(request));
		uom.setCreatedDateTime(dayService.getTimeStamp());
		
		uom = uomRepository.save(uom);

		
		return uomResponseDTOMapper(uom);
	}

	@Override
	public UomResponseDTO updateUom(UomRequestDTO uomRequest, HttpServletRequest request) {

		Optional<Uom> uom_ = uomRepository.findById(uomRequest.getId());
		if(uom_.isEmpty()) {
			throw new NotFoundException("Uom not be found in database");
		}		
		if(!validateUomData(uomRequest)) {
			throw new InvalidEntryException("Could not validate uom data");
		}		
		Uom uom = uom_.get();
		uom.setCode(uomRequest.getCode());
		uom.setName(uomRequest.getName());
		uom.setType(uomRequest.getType());		
		uom = uomRepository.save(uom);		
		return uomResponseDTOMapper(uom);
	}
	
	private UomResponseDTO uomResponseDTOMapper(Uom uom) {
		UomResponseDTO uomResponse = new UomResponseDTO();
		uomResponse.setId(uom.getId().toString());
		uomResponse.setCode(uom.getCode());
		uomResponse.setName(uom.getName());
		uomResponse.setType(uom.getType());
		uomResponse.setCompanyId(uom.getCompany().getId().toString());		
		if(uom.isActive()) {
			uomResponse.setActive("Active");
		}else {
			uomResponse.setActive("Inactive");
		}
		//uomResponse.setOtherInfo("Company: " + uom.getCompany().getName() + " Location: " + uom.getLocationName());
		return uomResponse;
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateUom(UomRequestDTO uom, HttpServletRequest request) {
		Optional<Uom> uom_ = uomRepository.findById(uom.getId());		
		if(uom_.isEmpty()) {
			throw new NotFoundException("Uom not found");
		}		
		if(uom_.get().isActive() == true) {
			throw new InvalidOperationException("Uom already active");
		}
		uom_.get().setActive(true);
		uomRepository.save(uom_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Uom Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateUom(UomRequestDTO uom, HttpServletRequest request) {
		Optional<Uom> uom_ = uomRepository.findById(uom.getId());		
		if(uom_.isEmpty()) {
			throw new NotFoundException("Uom not found");
		}		
		if(uom_.get().isActive() == false) {
			throw new InvalidOperationException("Uom already inactive");
		}
		uom_.get().setActive(false);
		uomRepository.save(uom_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Uom Deactivated successifully");
	}
	
	/**
	 * 
	 * @param uomRequest
	 * @return
	 */
	boolean validateUomData(UomRequestDTO uomRequest) {
		
		return true;
	}
}