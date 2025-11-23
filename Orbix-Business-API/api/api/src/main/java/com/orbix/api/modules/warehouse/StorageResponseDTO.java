package com.orbix.api.modules.warehouse;

import java.util.List;

import lombok.Data;

@Data
public class StorageResponseDTO {
	String id;
	
	String sn;
	
	String no;
	/** Owner information */

	String ownerFirstName;
	String ownerMiddleName;

	String ownerLastName;
	String ownerCompanyName;
	String ownerIdNo;
	String ownerIdType;
	String ownerPhoneNo;
	String ownerEmail;
	String ownerAddress;
	
	String comments;
	
	String status;

    String warehouseId;
    String warehouseName;
    
    String goodName;
    String goodDescription;
	
    String goodTypeId;	
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
    
    String goodTypeName;
    
    String billingType;
    String billingAmount;
    String billingStartAt;
    
    String width;
    String length;
    String height;
    String weight;
    String initialQty;
    String currentQty;
	
    List<ServiceBillItem> serviceBillItems;
}
