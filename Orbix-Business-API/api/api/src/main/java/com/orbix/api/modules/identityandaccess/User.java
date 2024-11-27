package com.orbix.api.modules.identityandaccess;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data  
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = {"nickname", "company_id"})})
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true, nullable = false, updatable = false)
	private String code;
	@NotBlank
	private String type;
	@NotBlank
	private String firstName;
	private String middleName;
	@NotBlank
	private String lastName;
	@NotBlank
	private String nickname;
	@NotBlank
	@Column(unique = true)
	private String username;
	@NotBlank
	private String password;
	

	private String authorizationToken;
	
	private boolean active = false;	
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	private Collection<Role> roles = new ArrayList<>();
	
	@Column(name = "created_by_user_id", nullable = true , updatable = false)
    private Long createdBy;
	@Column(name = "created_on_day_id", nullable = true , updatable = false)
    private Long createdOn;
	private LocalDateTime createdAt = LocalDateTime.now();
	
	@OneToOne(targetEntity = Company.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "company_id", nullable = true , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("branches")
    private Company company;
	
	@OneToOne(targetEntity = Branch.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "branch_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("branches")
    private Branch branch;
}
