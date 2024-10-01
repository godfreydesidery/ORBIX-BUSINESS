package com.orbix.api.modules.adminunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.exceptions.ResourceNotFoundException;

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
	
	@Override
	public List<CompanyResponseDTO> getAllCompanies(HttpServletRequest request) {
		// TODO Auto-generated method stub
		
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
		//Validate data
		if(!validateCompanyData(companyRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		
		Company company = new Company();
		
		company.setCode("CMP" + String.valueOf(Math.random()));
		company.setName(companyRequest.getName());
		company.setBrandName(companyRequest.getBrandName());
		company.setContactName(companyRequest.getContactName());
		company.setDomain(companyRequest.getDomain().replace(" ", ""));
		
		company = companyRepository.save(company);
		
		//company.setCode(company.getId().toString());
		
		//Create a main branch for the company. This branch can be edited later
		Branch branch = new Branch();		
		branch.setLevel("L1");
		branch.setCompany(company);
		branch.setName(company.getName() + "-MAIN-BRANCH");
		branch.setType("MAIN");
		branch.setParentBranch(null);		
		branch = branchRepository.save(branch);
		
		return companyResponseDTOMapper(company);
	}
	
	boolean validateCompanyData(CompanyRequestDTO companyRequest) {
		
		return true;
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
		company.setDomain(companyRequest.getDomain());
		
		company = companyRepository.save(company);
		
		return companyResponseDTOMapper(company);
	}

	

	private CompanyResponseDTO companyResponseDTOMapper(Company company) {
		CompanyResponseDTO companyResponse = new CompanyResponseDTO();
		companyResponse.setId(company.getId().toString());
		companyResponse.setCode(company.getCode());
		companyResponse.setName(company.getName());
		companyResponse.setBrandName(company.getBrandName());
		companyResponse.setContactName(company.getContactName());
		companyResponse.setDomain(company.getDomain());
		
		return companyResponse;
	}

	
}
