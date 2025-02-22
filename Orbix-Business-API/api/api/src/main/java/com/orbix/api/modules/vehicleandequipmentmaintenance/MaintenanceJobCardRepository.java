package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceJobCardRepository extends JpaRepository<MaintenanceJobCard, Long> {

	Optional<MaintenanceJobCard> findByMaintenance(Maintenance maintenance);

	Optional<MaintenanceJobCard> findFirstByMaintenanceAndStatusIn(Maintenance maintenance, List<String> statuses);

}
