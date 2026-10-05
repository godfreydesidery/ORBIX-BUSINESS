package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Company;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
	
	List<Supplier> findAllByCompanyAndNameContainingIgnoreCase(Company company, String supplierName);

	List<Supplier> findAllByCompany(Company userCompany);

	// One page of the list, searched on the columns the list screen shows
	@Query("SELECT s FROM Supplier s WHERE :search = '%%' OR LOWER(s.code) LIKE :search OR LOWER(s.name) LIKE :search OR LOWER(s.contactName) LIKE :search OR LOWER(s.address) LIKE :search OR LOWER(s.phoneNo) LIKE :search")
	Page<Supplier> getPageBySearch(@Param("search") String search, Pageable pageable);
}
