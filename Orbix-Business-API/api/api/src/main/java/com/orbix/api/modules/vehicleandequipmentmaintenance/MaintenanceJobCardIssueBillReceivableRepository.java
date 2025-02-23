package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.finance.BillReceivable;

public interface MaintenanceJobCardIssueBillReceivableRepository extends JpaRepository<MaintenanceJobCardIssueBillReceivable, Long> {
	
	List<MaintenanceJobCardIssueBillReceivable> findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(Maintenance maintenance);

	Optional<MaintenanceJobCardIssueBillReceivable> findByBillReceivable(BillReceivable billReceivable);
	
}
