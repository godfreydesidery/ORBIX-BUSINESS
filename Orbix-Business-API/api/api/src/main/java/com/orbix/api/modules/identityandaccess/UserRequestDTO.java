package com.orbix.api.modules.identityandaccess;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
}
