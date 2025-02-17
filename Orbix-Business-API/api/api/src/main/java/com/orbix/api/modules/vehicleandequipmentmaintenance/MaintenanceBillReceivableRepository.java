package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceBillReceivableRepository extends JpaRepository<MaintenanceBillReceivable, Long> {

	List<MaintenanceBillReceivable> findAllByMaintenance(Maintenance maintenance);

}
