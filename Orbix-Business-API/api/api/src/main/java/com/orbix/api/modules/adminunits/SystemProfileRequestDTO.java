package com.orbix.api.modules.adminunits;

import javax.persistence.Lob;

import lombok.Data;

@Data
public class SystemProfileRequestDTO {
	private String id;
	private String name; 
	private boolean multiCompany = false;
	private boolean strictMode = true;
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
}
