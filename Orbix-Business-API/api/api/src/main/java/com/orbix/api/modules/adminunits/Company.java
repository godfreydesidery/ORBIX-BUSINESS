package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;



@Entity
@Data 
//@NoArgsConstructor 
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "companies")
public class Company {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank
	@Column(unique = true)
	private String code;
	
	@NotBlank
	@Column(unique = true)
	private String name; 
	@NotBlank
	@Column(unique = true)
	private String brandName; 
	private String contactName;
	
	private String symbol;
	
	@NotBlank
	@Column(unique = true)
	private String domain; 
	
	private String legalType;
	private String industry;
	private String country;
	private String timeZone;
	
	private LocalDate foundingDate;
	
	@Lob
	private byte[] logo;
	private String tin;
	private String vrn;
	private String physicalAddress;
	private String postalCode;
	private String postalAddress;
	private String telephone;
	private String mobile;
	private String email;
	private String website;
	private String fax;
	
	private boolean active = false;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
	
	@ManyToOne(targetEntity = Day.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_on_day_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Day createdOnDay;
	
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
	private Collection<Branch> branches = new ArrayList<>();
	
	
	
	
	
	
	
	
	

}
