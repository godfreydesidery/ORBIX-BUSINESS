package com.orbix.api.modules.adminunits;

import java.util.List;

import lombok.Data;

@Data
public class BranchRequestDTO {
	private Long id;
	private String name;	
	private String level;
	private String type;
	
	//Company information - company in which branch belong
    private Long companyId;
    private String companyName;   
    //parent branch
    private Long parentBranchId;
}
