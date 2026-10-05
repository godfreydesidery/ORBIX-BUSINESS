package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Company;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByCompanyAndNameContainingIgnoreCase(Company company, String productName);

	List<Product> findAllByCompanyAndSellable(Company company, boolean b);

	List<Product> findAllByCompany(Company company);

	Optional<Product> findByIdAndCompany(Long productId, Company userCompany);

	// One page of the list, searched on the columns the list screen shows
	@Query("SELECT p FROM Product p WHERE :search = '%%' OR LOWER(p.code) LIKE :search OR LOWER(p.name) LIKE :search OR LOWER(p.description) LIKE :search OR LOWER(p.baseUom) LIKE :search")
	Page<Product> getPageBySearch(@Param("search") String search, Pageable pageable);
}
