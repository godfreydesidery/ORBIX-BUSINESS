package com.orbix.api.modules.vehicleandequipmentmaintenance;

import lombok.Data;

@Data
public class MaintenanceJobCardRequestDTO {
	Long id;
	String no;
	String status;
    Long maintenanceId;
    String maintenanceNo;
}
