package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class ServiceSpecialistResponseDTO {
	String id;	
	String active;
    String createdBy;		
	String createdDateTime;
	String companyId;
	String companyName;
	String branchId;
	String branchName;
    String userId;
    String nickname;
    String name;
    String code;
}
