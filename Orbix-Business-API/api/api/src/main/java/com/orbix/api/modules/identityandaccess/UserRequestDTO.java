package com.orbix.api.modules.identityandaccess;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;

@Data
public class UserRequestDTO {
	private Long id;
	private String code;	
	private String type;
	private String firstName;
	private String middleName;
	private String lastName;
	private String nickname;
	private String username;
	private String password;
	private boolean active = false;	
	
	private Long companyId;
	private String companyCode;
	private String companyName;
	private Long branchId;
	private String branchCode;
	private String branchName;
	
	private Collection<Role> roles = new ArrayList<>();
	
}
