package com.orbix.api.modules.audit;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditSettingRepository extends JpaRepository<AuditSetting, Long> {

	// The single row of settings, if it has been saved yet
	Optional<AuditSetting> findFirstByOrderByIdAsc();
}
