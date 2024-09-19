package com.orbix.api.modules.adminunits;

import java.util.List;

import lombok.Data;

@Data
public class BranchResponseDTO {
	private String id;
	private String name;	
	private String level;
	private String type;
	
	//Company information - company in which branch belong
    private String companyId;
    private String companyName;   
    //parent branch
    private String parentBranchId;
    //child branches
    private List<BranchResponseDTO> childBranches;
}
