package com.orbix.api.modules.adminunits;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeZoneRepository extends JpaRepository<TimeZone, Long> {
	
	boolean existsBy();

	List<TimeZone> findAllByIsDefault(boolean b);

}
