package com.orbix.api.modules.servicebay;

import lombok.Data;

@Data
public class MachineServiceRequestDTO {
	private Long id;
	private double qty = 1;
	private double price;
    private double machineId;
    private double serviceId;
}
