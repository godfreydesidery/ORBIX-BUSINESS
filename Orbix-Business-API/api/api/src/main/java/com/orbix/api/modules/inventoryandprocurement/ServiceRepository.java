package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Company;

public interface ServiceRepository extends JpaRepository<Servicel, Long> {
	
	List<Servicel> findAllByCompanyAndNameContainingIgnoreCase(Company company, String productName);

	List<Servicel> findAllByCompany(Company company);

	Optional<Servicel> findByIdAndCompany(Long productId, Company userCompany);

	// One page of the list, searched on the columns the list screen shows
	@Query("SELECT s FROM Servicel s WHERE :search = '%%' OR LOWER(s.code) LIKE :search OR LOWER(s.name) LIKE :search OR LOWER(s.description) LIKE :search OR LOWER(str(s.price)) LIKE :search")
	Page<Servicel> getPageBySearch(@Param("search") String search, Pageable pageable);
}
