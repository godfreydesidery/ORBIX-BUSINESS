package com.orbix.api.modules.servicebay;

import java.util.List;

import lombok.Data;

@Data
public class MachineResponseDTO {
	
	private String id;
	private String no;
	private String ownerName;
	private String ownerPhoneNo;
	private String machineRegNo;
	private String name;
	private String machineName;
	private String status;
	private String workshopId;
	private String workshopName;
	private String createdBy;
	private String createdAt;
	
	List<MachineServiceResponseDTO> machineServices;
}
