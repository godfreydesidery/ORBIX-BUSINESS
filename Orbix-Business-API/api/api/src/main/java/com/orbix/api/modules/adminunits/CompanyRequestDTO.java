package com.orbix.api.modules.adminunits;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class CompanyRequestDTO {
	private Long id;
	private String code;
	private String name; 
	private String brandName; 
	private String contactName;	
	private String symbol;
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
}
