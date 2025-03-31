package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.identityandaccess.User;

public interface MaintenanceJobCardIssueRepository extends JpaRepository<MaintenanceJobCardIssue, Long> {

	List<MaintenanceJobCardIssue> findAllByServiceSpecialistUserAndStatus(User user, String string);

}
