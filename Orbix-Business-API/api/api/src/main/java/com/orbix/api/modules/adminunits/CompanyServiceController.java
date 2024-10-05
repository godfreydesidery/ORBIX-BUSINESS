package com.orbix.api.modules.adminunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.exceptions.ResourceNotFoundException;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.swagger2.mappers.ModelMapper;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompanyServiceController implements CompanyService {
	
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final SystemProfileRepository systemProfileRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public List<CompanyResponseDTO> getAllCompanies(HttpServletRequest request) {		
		List<Company> companies = companyRepository.findAll();
		List<CompanyResponseDTO> companyResponses = new ArrayList<>();

		for(Company company : companies) {
			companyResponses.add(companyResponseDTOMapper(company));					
		}		
		return companyResponses;
	}
	
	@Override
	public CompanyResponseDTO get(
			Long id, 
			HttpServletRequest request) {		
		Optional<Company> _company = companyRepository.findById(id);
		if(_company.isEmpty()) {
			throw new NotFoundException("Company not found");
		}		
		return companyResponseDTOMapper(_company.get());	
	}
	
	@Override
	public CompanyResponseDTO createCompany(CompanyRequestDTO companyRequest, HttpServletRequest request) {
		/**Validate data*/		
		if(!validateCompanyData(companyRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		/**New company*/
		Company company = new Company();		
		company.setCode(String.valueOf(Math.random()));
		company.setName(companyRequest.getName());
		company.setBrandName(companyRequest.getBrandName());
		company.setContactName(companyRequest.getContactName());
		company.setDomain(companyRequest.getDomain().replace(" ", ""));
		company.setSymbol(companyRequest.getSymbol());
		company.setLegalType(companyRequest.getLegalType());
		company.setIndustry(companyRequest.getIndustry());
		company.setCountry(companyRequest.getCountry());
		company.setTimeZone(companyRequest.getTimeZone());
		company.setFoundingDate(companyRequest.getFoundingDate());
		company.setTin(companyRequest.getTin());
		company.setVrn(companyRequest.getVrn());
		company.setPhysicalAddress(companyRequest.getPhysicalAddress());
		company.setPostalCode(companyRequest.getPostalCode());
		company.setPostalAddress(companyRequest.getPostalAddress());
		company.setTelephone(companyRequest.getTelephone());
		company.setMobile(companyRequest.getMobile());
		company.setEmail(companyRequest.getEmail());
		company.setFax(companyRequest.getFax());
		
		company.setCreatedByUser(userService.getUser(request));
		company.setCreatedOnDay(dayService.getDay());
		company.setCreatedDateTime(dayService.getTimeStamp());
		
		company = companyRepository.save(company);
		/**Create  company code*/
		company.setCode("CMP/"+ company.getId().toString());
		company = companyRepository.save(company);
		
		
		/**Create a main branch for the company
		 * A company requires a main branch*/
		Branch branch = new Branch();
		branch.setCode(String.valueOf(Math.random()));
		branch.setName(company.getName() + "-MAIN-BRANCH");
		branch.setLevel("L1");
		branch.setCompany(company);
		
		branch.setType("MAIN");		
		branch.setParentBranch(null);
		
		branch.setCreatedByUser(userService.getUser(request));
		branch.setCreatedOnDay(dayService.getDay());
		branch.setCreatedDateTime(dayService.getTimeStamp());
		
		
		branch = branchRepository.save(branch);
		
		/**Create  branch code*/
		branch.setCode("BR/"+ branch.getId().toString());
		branch = branchRepository.save(branch);
		
		return companyResponseDTOMapper(company);
	}
	
	
	@Override
	public CompanyResponseDTO updateCompany(CompanyRequestDTO companyRequest, HttpServletRequest request) {
		
		Optional<Company> company_ = companyRepository.findById(companyRequest.getId());
		if(company_.isEmpty()) {
			throw new NotFoundException("Company not be found in database");
		}
		
		if(!validateCompanyData(companyRequest)) {
			throw new InvalidEntryException("Could not validate company data");
		}
		
		Company company = company_.get();
		company.setName(companyRequest.getName());
		company.setBrandName(companyRequest.getBrandName());
		company.setContactName(companyRequest.getContactName());
		company.setDomain(companyRequest.getDomain().replace(" ", ""));
		company.setSymbol(companyRequest.getSymbol());
		company.setLegalType(companyRequest.getLegalType());
		company.setIndustry(companyRequest.getIndustry());
		company.setCountry(companyRequest.getCountry());
		company.setTimeZone(companyRequest.getTimeZone());
		company.setFoundingDate(companyRequest.getFoundingDate());
		company.setTin(companyRequest.getTin());
		company.setVrn(companyRequest.getVrn());
		company.setPhysicalAddress(companyRequest.getPhysicalAddress());
		company.setPostalCode(companyRequest.getPostalCode());
		company.setPostalAddress(companyRequest.getPostalAddress());
		company.setTelephone(companyRequest.getTelephone());
		company.setMobile(companyRequest.getMobile());
		company.setEmail(companyRequest.getEmail());
		company.setFax(companyRequest.getFax());
		
		company = companyRepository.save(company);
		
		return companyResponseDTOMapper(company);
	}
	/**
	 * Activate an inactive company
	 */
	@Override
	public ApiCustomResponse activateCompany(CompanyRequestDTO company, HttpServletRequest request) {
		Optional<Company> company_ = companyRepository.findById(company.getId());		
		if(company_.isEmpty()) {
			throw new NotFoundException("Company not found in database");
		}		
		if(company_.get().isActive() == true) {
			throw new InvalidOperationException("Company already active");
		}
		company_.get().setActive(true);
		companyRepository.save(company_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Company Activated successifully");
	}
	/**
	 * Deactivate an inactive company
	 */
	@Override
	public ApiCustomResponse deactivateCompany(CompanyRequestDTO company, HttpServletRequest request) {
		Optional<Company> company_ = companyRepository.findById(company.getId());		
		if(company_.isEmpty()) {
			throw new NotFoundException("Company not found in database");
		}		
		if(company_.get().isActive() == false) {
			throw new InvalidOperationException("Company already inactive");
		}
		company_.get().setActive(false);
		companyRepository.save(company_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Company Deactivated successifully");
	}


	/**
	 * Maps company data to Company Response DTO
	 * @param company
	 * @return
	 */
	private CompanyResponseDTO companyResponseDTOMapper(Company company) {
		CompanyResponseDTO companyResponse = new CompanyResponseDTO();
		companyResponse.setId(company.getId().toString());
		companyResponse.setCode(company.getCode());
		companyResponse.setName(company.getName());
		companyResponse.setBrandName(company.getBrandName());
		companyResponse.setContactName(company.getContactName());
		companyResponse.setDomain(company.getDomain());
		companyResponse.setSymbol(company.getSymbol());
		companyResponse.setLegalType(company.getLegalType());
		companyResponse.setIndustry(company.getIndustry());
		companyResponse.setCountry(company.getCountry());
		companyResponse.setTimeZone(company.getTimeZone());
		companyResponse.setFoundingDate(company.getFoundingDate());
		companyResponse.setTin(company.getTin());
		companyResponse.setVrn(company.getVrn());
		companyResponse.setPhysicalAddress(company.getPhysicalAddress());
		companyResponse.setPostalCode(company.getPostalCode());
		companyResponse.setPostalAddress(company.getPostalAddress());
		companyResponse.setTelephone(company.getTelephone());
		companyResponse.setMobile(company.getMobile());
		companyResponse.setEmail(company.getEmail());
		companyResponse.setFax(company.getFax());
		if(company.isActive()) {
			companyResponse.setActive("Active");
		}else {
			companyResponse.setActive("Inactive");
		}
		
	
		return companyResponse;
	}

	/**
	 * Company data validator
	 * @param companyRequest
	 * @return
	 */
	boolean validateCompanyData(CompanyRequestDTO companyRequest) {
		
		return true;
	}

	
	
}


