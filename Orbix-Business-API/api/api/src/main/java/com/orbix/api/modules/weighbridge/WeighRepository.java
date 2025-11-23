package com.orbix.api.modules.weighbridge;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WeighRepository extends JpaRepository<Weigh, Long> {

	List<Weigh> findByCreatedDateTimeAfter(LocalDateTime last48Hours);

}
