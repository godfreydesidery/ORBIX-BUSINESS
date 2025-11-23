package com.orbix.api.modules.servicebay;

import lombok.Data;

@Data
public class MachineServiceResponseDTO {
	private String sn;
	private String id;
	private String qty;
	private String price;
	private String status;
    private String createdBy;
	private String createdAt;
    private String machineId;
    private String machineName;
    private String serviceId;
    private String serviceName;
}
