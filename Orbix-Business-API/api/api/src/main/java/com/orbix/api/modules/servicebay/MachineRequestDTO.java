package com.orbix.api.modules.servicebay;

import lombok.Data;

@Data
public class MachineRequestDTO {
	Long id;
	private String no;
	private String ownerName;
	private String ownerPhoneNo;
	private String regNo;
	private String name;	
	private Long workshopId;
}
