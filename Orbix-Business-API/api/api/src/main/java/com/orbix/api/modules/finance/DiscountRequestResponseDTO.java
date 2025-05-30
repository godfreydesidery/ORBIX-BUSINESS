package com.orbix.api.modules.finance;

import lombok.Data;

@Data
public class DiscountRequestResponseDTO {
	String id;
	String serviceBillId;
	String serviceBillName;
	String billAmount;
	String discountAmount;
	String status;
    String created;
    String approved;
    String branch;
    
    String description;
}
