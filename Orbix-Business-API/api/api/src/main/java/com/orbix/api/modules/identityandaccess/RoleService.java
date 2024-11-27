package com.orbix.api.modules.identityandaccess;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface RoleService {
	List<RoleResponseDTO> getAllRoles(HttpServletRequest request);	
	RoleResponseDTO createRole(RoleRequestDTO roleRequst, HttpServletRequest request);
	RoleResponseDTO updateRole(RoleRequestDTO roleRequest, HttpServletRequest request);
}
