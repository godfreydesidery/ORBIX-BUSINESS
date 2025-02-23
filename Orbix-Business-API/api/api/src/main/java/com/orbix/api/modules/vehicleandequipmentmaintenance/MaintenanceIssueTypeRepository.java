package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.identityandaccess.User;

public interface MaintenanceIssueTypeRepository extends JpaRepository<MaintenanceIssueType, Long> {

	List<MaintenanceIssueType> findAllByCompanyAndActive(Company company, boolean b);

	Optional<MaintenanceIssueType> findByNameAndCompany(String maintenanceIssueTypeName, Company userCompany);

}
