package com.orbix.api.modules.identityandaccess;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;

@Data
public class RoleResponseDTO {
	private String id;
	private String name; 	
	private String owner;
	private Collection<Privilege> privileges = new ArrayList<>();	
}
