package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface VehicleEquipmentTypeRepository extends JpaRepository<VehicleEquipmentType, Long> {

	List<VehicleEquipmentType> findAllByCompanyAndActive(Company company, boolean b);

	Optional<VehicleEquipmentType> findByNameAndCompany(String vehicleEquipmentTypeName, Company company);

}
