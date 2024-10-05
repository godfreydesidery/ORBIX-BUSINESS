package com.orbix.api.modules.adminunits;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface BranchService {
	List<BranchResponseDTO> getAllBranches(HttpServletRequest request);
	BranchResponseDTO get(Long id, HttpServletRequest request);
	BranchResponseDTO createBranch(BranchRequestDTO branch, HttpServletRequest request);
	BranchResponseDTO updateBranch(BranchRequestDTO branch, HttpServletRequest request);
	ApiCustomResponse activateBranch(BranchRequestDTO branch, HttpServletRequest request);
	ApiCustomResponse deactivateBranch(BranchRequestDTO branch, HttpServletRequest request);
	
	

	
}
