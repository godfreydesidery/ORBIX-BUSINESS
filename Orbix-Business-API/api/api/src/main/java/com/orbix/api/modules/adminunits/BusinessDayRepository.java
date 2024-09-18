package com.orbix.api.modules.adminunits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessDayRepository extends JpaRepository<BusinessDay, Long> {

}
