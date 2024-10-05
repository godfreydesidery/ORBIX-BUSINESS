package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class BranchResponseDTO {
	private String id;
	private String code;
	private String name;	
	private String level;
	private String type;
	private String active;
	private String physicalAddress;
	private String postalCode;
	private String postalAddress;
	private String telephone;
	private String mobile;
	private String email;
	private String website;
	private String fax;
	private String city;
	private String state;
	private String country;
	private String managerName;
	private String openingHours;
	private int numberOfStaff;
	private double salesTargets;
	private LocalDate dateEstablished;
	private String notes;
	
	private String companyId;
	private String companyName;
	private String parentBranchId;
    //child branches
    private List<BranchResponseDTO> childBranches;
}
