package com.orbix.api.modules.adminunits;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
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
	private final SystemProfileRepository systemProfileRepository;
	
	@Override
	public List<CompanyResponseDTO> getAll(HttpServletRequest request) {
		// TODO Auto-generated method stub
		
		List<Company> companies = companyRepository.findAll();
		List<CompanyResponseDTO> companyResponses = new ArrayList<>();

		for(Company company : companies) {
			companyResponses.add(companyResponseDTOMapper(company));					
		}		
		return companyResponses;
	}
	
	@Override
	public CompanyResponseDTO createCompany(CompanyRequestDTO companyRequest, HttpServletRequest request) {
		//Validate data
		if(!validateCompanyData(companyRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		SystemProfile systemProfile = new SystemProfile();
		List<SystemProfile> systems = systemProfileRepository.findAll();
		for(SystemProfile profile : systems) {
			systemProfile = profile;
		}
		
		Company company = new Company();
		
		company.setName(companyRequest.getName());
		company.setBrandName(companyRequest.getBrandName());
		company.setDomain(companyRequest.getDomain());
		
		company = companyRepository.save(company);
		
		return companyResponseDTOMapper(company);
	}
	
	boolean validateCompanyData(CompanyRequestDTO companyRequest) {
		
		return true;
	}

	@Override
	public CompanyResponseDTO updateCompany(CompanyRequestDTO company, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	

	private CompanyResponseDTO companyResponseDTOMapper(Company company) {
		CompanyResponseDTO companyResponse = new CompanyResponseDTO();
		companyResponse.setId(company.getId().toString());
		companyResponse.setName(company.getName());
		companyResponse.setBrandName(company.getBrandName());
		companyResponse.setDomain(company.getDomain());
		
		
		return companyResponse;
	}
}
