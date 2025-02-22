package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface MaintenanceIssueTypeRepository extends JpaRepository<MaintenanceIssueType, Long> {

	List<MaintenanceIssueType> findAllByCompanyAndActive(Company company, boolean b);

}
