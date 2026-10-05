package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Company;

public interface DineableRepository extends JpaRepository<Dineable, Long> {
	List<Dineable> findAllByCompanyAndNameContainingIgnoreCase(Company company, String dineableName);

	List<Dineable> findAllByCompanyAndSellable(Company company, boolean b);

	List<Dineable> findAllByCompany(Company company);

	Optional<Dineable> findByIdAndCompany(Long dineableId, Company userCompany);

	// One page of the list, searched on the columns the list screen shows
	@Query("SELECT d FROM Dineable d WHERE :search = '%%' OR LOWER(d.code) LIKE :search OR LOWER(d.name) LIKE :search OR LOWER(d.description) LIKE :search OR LOWER(d.baseUom) LIKE :search")
	Page<Dineable> getPageBySearch(@Param("search") String search, Pageable pageable);
}
