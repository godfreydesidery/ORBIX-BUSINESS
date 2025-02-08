package com.orbix.api.modules.warehouse;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<Storage, Long> {

	List<Storage> findAllByStatusIn(List<String> statuses);

	List<Storage> findAllByStatusInAndCheckedOutDateTimeBetween(List<String> statuses, LocalDateTime startOfToday,
			LocalDateTime endOfYesterday);

}
