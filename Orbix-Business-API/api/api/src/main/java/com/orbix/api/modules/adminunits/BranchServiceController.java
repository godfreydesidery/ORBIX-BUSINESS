package com.orbix.api.modules.adminunits;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidEntryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BranchServiceController implements BranchService {
	
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	
	@Override
	public BranchResponseDTO createBranch(BranchRequestDTO branchRequest, HttpServletRequest request) {
		
		Optional<Company> company_ = companyRepository.findById(branchRequest.getCompanyId());
		if(company_.isEmpty()) {
			throw new InvalidEntryException("Company not found");
		}
		
		Optional<Branch> parentBranch_ = branchRepository.findById(branchRequest.getParentBranchId());
		if(branchRequest.getParentBranchId() != null) {
			if(parentBranch_.isEmpty()) {
				throw new InvalidEntryException("Branch not found");
			}
		}
		
		
		Branch branch = new Branch();
		branch.setName(branchRequest.getName());
		branch.setType(branchRequest.getType());
		branch.setCompany(company_.get());
		
		//check on branch type tommorrow, tired
		
		return null;
	}

	@Override
	public BranchResponseDTO updateBranch(BranchRequestDTO branchRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
