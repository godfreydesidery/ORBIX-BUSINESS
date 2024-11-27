package com.orbix.api.modules.identityandaccess;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;

@Data
public class RoleRequestDTO {
	private Long id;
	private String name; 	
	private String owner;
	private Long companyId;
	private String companyCode;
	private String companyName;
	private Collection<Privilege> privileges = new ArrayList<>();	
}
