package com.orbix.api.modules.warehouse;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RemovedGoodRepository extends JpaRepository<RemovedGood, Long> {

	List<RemovedGood> findAllByCreatedDateTimeBetween(LocalDateTime atStartOfDay, LocalDateTime atTime);

}
