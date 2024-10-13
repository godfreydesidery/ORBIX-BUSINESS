package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface VehicleAndEquipmentTypeRepository extends JpaRepository<VehicleAndEquipmentType, Long> {

	List<VehicleAndEquipmentType> findAllByCompanyAndActive(Company company, boolean b);

	Optional<VehicleAndEquipmentType> findByNameAndCompany(String vehicleAndEquipmentTypeName, Company company);

}
