package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import lombok.Data;

@Data
public class GrnResponseDTO {
	private String id;
	private String no;
	private String status;
	
	private String branchId;
	private String branchCode;
	private String shopId;
	private String lpoId;
	
	List<GrnDetailResponseDTO> grnDetails;
}
