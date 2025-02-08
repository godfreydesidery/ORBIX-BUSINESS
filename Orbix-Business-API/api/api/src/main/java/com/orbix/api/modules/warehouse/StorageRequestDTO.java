package com.orbix.api.modules.warehouse;

import lombok.Data;

@Data
public class StorageRequestDTO {
	Long id;
	
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
		
	String status = "PENDING";

    Long warehouseId;
    String warehouseName;
	
    Long goodTypeId;	
    Long branchId;
    Long companyId;
    
    String goodTypeName;
    String goodName;
	String vehicleEquipmentColor;
        
    String billingType;
    double billingAmount;
    
    String startBillingAt;
}
