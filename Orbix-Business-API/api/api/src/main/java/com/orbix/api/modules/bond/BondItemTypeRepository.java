package com.orbix.api.modules.bond;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface BondItemTypeRepository extends JpaRepository<BondItemType, Long> {

	List<BondItemType> findAllByCompanyAndActive(Company company, boolean b);

	Optional<BondItemType> findByNameAndCompany(String goodTypeName, Company company);

}
