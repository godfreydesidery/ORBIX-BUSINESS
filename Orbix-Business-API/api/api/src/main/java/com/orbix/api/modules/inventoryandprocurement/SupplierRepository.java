package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
	
	List<Supplier> findAllByCompanyAndNameContainingIgnoreCase(Company company, String supplierName);
}
