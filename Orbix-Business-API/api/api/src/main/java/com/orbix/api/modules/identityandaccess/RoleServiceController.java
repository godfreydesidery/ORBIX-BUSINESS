package com.orbix.api.modules.identityandaccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoleServiceController implements RoleService {
	
	private final RoleRepository roleRepository;
	
	@Override
	public List<RoleResponseDTO> getAllRoles(HttpServletRequest request) {

		List<Role> roles = roleRepository.findAll();
		List<RoleResponseDTO> roleResponses = new ArrayList<>();

		for(Role role : roles) {
			roleResponses.add(roleResponseDTOMapper(role));					
		}		
		return roleResponses;
	}

	@Override
	public RoleResponseDTO createRole(RoleRequestDTO roleRequest, HttpServletRequest request) {
		//Validate data
		if(!validateRoleData(roleRequest)) {
			throw new InvalidEntryException("Validation failed");
		}	
		Role role = new Role();
		role.setName(roleRequest.getName());
		role.setOwner("COMPANY");
		role = roleRepository.save(role);	
		return roleResponseDTOMapper(role);
	}

	@Override
	public RoleResponseDTO updateRole(RoleRequestDTO roleRequest, HttpServletRequest request) {
		Optional<Role> role_ = roleRepository.findById(roleRequest.getId());
		if(role_.isEmpty()) {
			throw new NotFoundException("Role not found in database");
		}		
		if(role_.get().getOwner().equals("SYSTEM")) {
			throw new InvalidOperationException("Invalid Operation: Can not edit role owned by the system");
		}	
		if(!validateRoleData(roleRequest)) {
			throw new InvalidEntryException("Could not validate role data");
		}	
		Role role = role_.get();
		role.setName(roleRequest.getName());
		role = roleRepository.save(role);
		return roleResponseDTOMapper(role);
	}
	
	private RoleResponseDTO roleResponseDTOMapper(Role role) {
		RoleResponseDTO roleResponse = new RoleResponseDTO();
		roleResponse.setId(role.getId().toString());
		roleResponse.setName(role.getName());
		roleResponse.setOwner(role.getOwner());

		return roleResponse;
	}
	
	boolean validateRoleData(RoleRequestDTO roleRequest) {
		
		return true;
	}

}
