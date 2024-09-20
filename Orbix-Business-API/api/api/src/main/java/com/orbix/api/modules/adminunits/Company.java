package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;



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
	private String postCode;
	private String postAddress;
	private String telephone;
	private String mobile;
	private String email;
	private String website;
	private String fax;
	
	private String bankAccountName;
	private String bankPhysicalAddress;
	private String bankPostCode;
	private String bankPostAddress;
	private String bankName;
	private String bankAccountNo;
	
	private String bankAccountName2;
	private String bankPhysicalAddress2;
	private String bankPostCode2;
	private String bankPostAddress2;
	private String bankName2;
	private String bankAccountNo2;
	
	private String bankAccountName3;
	private String bankPhysicalAddress3;
	private String bankPostCode3;
	private String bankPostAddress3;
	private String bankName3;
	private String bankAccountNo3;

	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	private Collection<Branch> branches = new ArrayList<>();
	
	
	
	
	
	
	
	
	

}
