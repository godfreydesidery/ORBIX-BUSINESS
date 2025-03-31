package com.orbix.api.modules.warehouse;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class StorageRequestDTO {
	Long id;
	
	String no;
	/** Owner information */

	@NotBlank(message = "First name cannot be empty")
	String ownerFirstName;
	
	String ownerMiddleName;

	@NotBlank(message = "Last name cannot be empty")
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
    @NotBlank(message = "Good name cannot be empty")
    String goodName;
    String goodDescription;
	String vehicleEquipmentColor;
        
    String billingType;
    double billingAmount;
    
    String startBillingAt;
    
    double width;
    double length;
    double height;
    double weight;
}
