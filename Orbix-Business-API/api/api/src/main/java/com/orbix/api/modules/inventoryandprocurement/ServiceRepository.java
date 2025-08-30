package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface ServiceRepository extends JpaRepository<Servicel, Long> {
	
	List<Servicel> findAllByCompanyAndNameContainingIgnoreCase(Company company, String productName);

	List<Servicel> findAllByCompany(Company company);

	Optional<Servicel> findByIdAndCompany(Long productId, Company userCompany);
}
