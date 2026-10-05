package com.orbix.api.modules.bond;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Company;

public interface BondItemTypeRepository extends JpaRepository<BondItemType, Long> {

	List<BondItemType> findAllByCompanyAndActive(Company company, boolean b);

	Optional<BondItemType> findByNameAndCompany(String goodTypeName, Company company);

	// Search on the columns the bond item type list shows
	@Query("SELECT t FROM BondItemType t WHERE :search = '%%' OR LOWER(t.code) LIKE :search OR LOWER(t.name) LIKE :search")
	Page<BondItemType> getPageBySearch(@Param("search") String search, Pageable pageable);
}
