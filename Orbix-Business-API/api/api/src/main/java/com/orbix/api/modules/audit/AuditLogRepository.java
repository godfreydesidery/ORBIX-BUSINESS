package com.orbix.api.modules.audit;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Insert and read only: the audit log is never updated or deleted through the application,
 * so this repository deliberately offers no update or delete methods.
 */
public interface AuditLogRepository extends Repository<AuditLog, Long> {

	// Same signature as CrudRepository.save, so Spring Data serves it from its standard implementation
	<S extends AuditLog> S save(S auditLog);

	Optional<AuditLog> findById(Long id);

	// One page of entries in a period, filtered by the optional fields ('' or null means any) and searched on
	// the username, record reference, summary and IP address
	@Query("SELECT a FROM AuditLog a WHERE a.occurredAt >= :from AND a.occurredAt < :to"
			+ " AND (:category = '' OR a.category = :category) AND (:action = '' OR a.action = :action)"
			+ " AND (:outcome = '' OR a.outcome = :outcome) AND (:userId IS NULL OR a.userId = :userId)"
			+ " AND (:branchId IS NULL OR a.branchId = :branchId)"
			+ " AND (:search = '%%' OR LOWER(a.username) LIKE :search OR LOWER(a.entityRef) LIKE :search"
			+ " OR LOWER(a.summary) LIKE :search OR LOWER(a.ipAddress) LIKE :search)")
	Page<AuditLog> getPage(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("category") String category,
			@Param("action") String action, @Param("outcome") String outcome, @Param("userId") Long userId,
			@Param("branchId") Long branchId, @Param("search") String search, Pageable pageable);
}
