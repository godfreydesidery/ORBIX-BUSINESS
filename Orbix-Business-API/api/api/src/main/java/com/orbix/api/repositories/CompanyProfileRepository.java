package com.orbix.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.orbix.api.domain.CompanyProfile;


@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
	@Query("select count(c) > 0 from CompanyProfile c")
	boolean hasData();
}
