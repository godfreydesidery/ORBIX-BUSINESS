package com.orbix.api.modules.weighbridge;

import java.util.List;


import lombok.Data;

@Data
public class WeighResponseDTO {
String id;
	
	String sn;
	
	String no;
	/** Owner information */
	String regNo;
	String refNo;
	
	String recheck;
	

	String ownerFirstName;
	String ownerMiddleName;

	String ownerLastName;
	String ownerCompanyName;
	String ownerIdNo;
	String ownerIdType;
	String ownerPhoneNo;
	String ownerEmail;
	String ownerAddress;
	String ownerName;
	
	String weighStatus;
	
	String description;
	
	String comments;
	
	String status;
	
    String branchId;
    String companyId;
    
    String createdAt;
    String createdBy;
    String checkedInAt;
    String checkedInBy;
    String checkedOutAt;
    String checkedOutBy;
    String canceledAt;
    String canceledBy;
    

    
	
   // List<ServiceBillItem> serviceBillItems;
}
