package com.orbix.api.modules.adminunits;

import javax.servlet.http.HttpServletRequest;

public interface BranchService {
	
	BranchResponseDTO createBranch(BranchRequestDTO branch, HttpServletRequest request);
	BranchResponseDTO updateBranch(BranchRequestDTO branch, HttpServletRequest request);
}
