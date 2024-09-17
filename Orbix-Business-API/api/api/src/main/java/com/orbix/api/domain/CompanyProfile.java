/**
 * 
 */
package com.orbix.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author GODFREY
 *
 */
@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "company_profile")
public class CompanyProfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank
	private String companyName;
	@NotBlank
	private String contactName;
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
	
	//@Length(max = 2000)//
	private String quotationNotes;
	//@Length(max = 2000)
	private String salesInvoiceNotes;
	
	private double registrationFee = 0;
	private String publicPath;
	
	//Prefixes
	private String employeePrefix = "EMP";

}
