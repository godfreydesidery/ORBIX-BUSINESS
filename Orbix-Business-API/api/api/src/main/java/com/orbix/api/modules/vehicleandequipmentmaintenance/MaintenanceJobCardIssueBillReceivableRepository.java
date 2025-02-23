package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceJobCardIssueBillReceivableRepository extends JpaRepository<MaintenanceJobCardIssueBillReceivable, Long> {
	
	List<MaintenanceJobCardIssueBillReceivable> findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(Maintenance maintenance);
	
}
