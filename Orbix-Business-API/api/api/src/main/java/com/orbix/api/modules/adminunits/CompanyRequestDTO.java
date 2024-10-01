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
}
