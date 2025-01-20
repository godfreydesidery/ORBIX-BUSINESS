package com.orbix.api.modules.inventoryandprocurement;

import java.time.LocalDate;

import com.orbix.api.api.commons.WorkFlowStatus;
import lombok.Data;

@Data
public class LpoRequestDTO {
	
	private Long id;
	private String no;
	private LocalDate validUntilDate;
	private WorkFlowStatus status;
	
	private Long branchId;
	private Long shopId;
	private Long supplierId;
}
