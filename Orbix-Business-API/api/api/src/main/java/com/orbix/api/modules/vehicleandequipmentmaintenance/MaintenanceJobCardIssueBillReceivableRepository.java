package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.finance.BillReceivable;

public interface MaintenanceJobCardIssueBillReceivableRepository extends JpaRepository<MaintenanceJobCardIssueBillReceivable, Long> {
	
	List<MaintenanceJobCardIssueBillReceivable> findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(Maintenance maintenance);

	Optional<MaintenanceJobCardIssueBillReceivable> findByBillReceivable(BillReceivable billReceivable);

	List<MaintenanceJobCardIssueBillReceivable> findAllByBillReceivableIn(List<BillReceivable> billReceivables);

	List<MaintenanceJobCardIssueBillReceivable> findAllByMaintenanceJobCardIssue(
			MaintenanceJobCardIssue maintenanceJobCardIssue);

	@Query("SELECT b FROM MaintenanceJobCardIssueBillReceivable b LEFT JOIN FETCH b.billReceivable WHERE b.maintenanceJobCardIssue IN :maintenanceJobCardIssues ORDER BY b.id")
	List<MaintenanceJobCardIssueBillReceivable> findAllByMaintenanceJobCardIssueIn(@Param("maintenanceJobCardIssues") List<MaintenanceJobCardIssue> maintenanceJobCardIssues);

	@Query("SELECT b FROM MaintenanceJobCardIssueBillReceivable b LEFT JOIN FETCH b.billReceivable "
			+ "JOIN b.maintenanceJobCardIssue i JOIN i.maintenanceJobCard c WHERE c.maintenance IN :maintenances")
	List<MaintenanceJobCardIssueBillReceivable> findAllByMaintenanceJobCardIssue_MaintenanceJobCard_MaintenanceIn(@Param("maintenances") List<Maintenance> maintenances);
	
}
