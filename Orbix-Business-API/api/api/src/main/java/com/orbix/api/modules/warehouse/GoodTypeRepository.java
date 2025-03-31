package com.orbix.api.modules.warehouse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface GoodTypeRepository extends JpaRepository<GoodType, Long> {

	List<GoodType> findAllByCompanyAndActive(Company company, boolean b);

	Optional<GoodType> findByNameAndCompany(String goodTypeName, Company company);

}
