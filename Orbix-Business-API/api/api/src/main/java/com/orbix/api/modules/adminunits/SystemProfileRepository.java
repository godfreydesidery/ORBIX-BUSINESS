package com.orbix.api.modules.adminunits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemProfileRepository extends JpaRepository<SystemProfile, Long> {
	@Query("select count(s) > 0 from SystemProfile s")
	boolean hasData();
}
