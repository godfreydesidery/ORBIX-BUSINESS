package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface DineableRepository extends JpaRepository<Dineable, Long> {
	List<Dineable> findAllByCompanyAndNameContainingIgnoreCase(Company company, String dineableName);

	List<Dineable> findAllByCompanyAndSellable(Company company, boolean b);

	List<Dineable> findAllByCompany(Company company);

	Optional<Dineable> findByIdAndCompany(Long dineableId, Company userCompany);
}
