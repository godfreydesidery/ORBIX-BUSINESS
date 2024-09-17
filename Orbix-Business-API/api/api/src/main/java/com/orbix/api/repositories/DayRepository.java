package com.orbix.api.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.orbix.api.domain.Day;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {

	Optional <Day> findByBussinessDate(LocalDate bussinessDate);
	
	@Query("select d from Day d where d.status='STARTED'")
	Day getCurrentBussinessDay();
	
	@Query("select count(d) > 0 from Day d")
	boolean hasData();
	
	@Query("SELECT MAX(d.id) FROM Day d")
	Long getLastId();

}
