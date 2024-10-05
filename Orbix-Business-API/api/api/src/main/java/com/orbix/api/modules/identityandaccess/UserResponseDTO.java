package com.orbix.api.modules.identityandaccess;

import lombok.Data;

@Data
public class UserResponseDTO {
	private String id;
	private String code;	
	private String type;
	private String firstName;
	private String middleName;
	private String lastName;
	private String nickname;
	private String username;
	private String password;
	private String active;
}
