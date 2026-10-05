package com.orbix.api.modules.weighbridge;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeighRepository extends JpaRepository<Weigh, Long> {

	List<Weigh> findByCreatedDateTimeAfter(LocalDateTime last48Hours);

	// Search on the columns the weigh lists show
	String WEIGH_SEARCH = "(:search = '%%' OR LOWER(w.no) LIKE :search OR LOWER(w.regNo) LIKE :search"
			+ " OR LOWER(w.ownerFirstName) LIKE :search OR LOWER(w.ownerPhoneNo) LIKE :search"
			+ " OR LOWER(w.weighStatus) LIKE :search OR LOWER(w.status) LIKE :search)";

	@Query("SELECT w FROM Weigh w WHERE w.createdDateTime > :createdAfter AND " + WEIGH_SEARCH)
	Page<Weigh> getPageByCreatedDateTimeAfter(@Param("createdAfter") LocalDateTime createdAfter, @Param("search") String search, Pageable pageable);
}
