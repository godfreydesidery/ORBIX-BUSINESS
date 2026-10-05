package com.orbix.api.modules.audit;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Clears audit log entries older than 90 days, in the background. Entries are deleted, not archived.
 * This is the only code that deletes from the audit log; each clearing is itself recorded as an entry.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogCleanup {

	// How long entries are kept
	public static final int RETENTION_DAYS = 90;
	// Entries deleted per transaction, so the table is never locked for long
	private static final int BATCH_SIZE = 1000;

	private final AuditLogRepository auditLogRepository;
	private final PlatformTransactionManager transactionManager;

	// Ten minutes after start-up, then every six hours, so a server switched off at night still clears its log
	@Scheduled(initialDelay = 10 * 60 * 1000L, fixedDelay = 6 * 60 * 60 * 1000L)
	public void clearOldEntries() {
		LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC).minusDays(RETENTION_DAYS);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		int cleared = 0;
		try {
			int deleted;
			do {
				deleted = transactionTemplate.execute(status -> {
					List<Long> ids = auditLogRepository.getIdsBefore(before, PageRequest.of(0, BATCH_SIZE));
					return ids.isEmpty() ? 0 : auditLogRepository.deleteByIds(ids);
				});
				cleared += deleted;
			}while(deleted > 0);
		}catch(Exception e) {
			log.error("Could not clear old audit log entries: {}", e.getMessage());
		}
		if(cleared > 0) {
			recordClearing(cleared, before);
		}
	}

	// Records the clearing, so the gap it leaves is explained
	private void recordClearing(int cleared, LocalDateTime before) {
		try {
			AuditLog auditLog = new AuditLog();
			auditLog.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC));
			auditLog.setCategory("SECURITY");
			auditLog.setAction("AUDIT_LOG_CLEARED");
			auditLog.setOutcome(AuditLogServiceController.SUCCESS);
			auditLog.setEntityType("AuditLog");
			auditLog.setSummary("Cleared " + cleared + " audit log entries older than " + RETENTION_DAYS + " days");
			auditLog.setDetails("{\"cleared\":" + cleared + ",\"before\":\"" + before + "Z\"}");
			new TransactionTemplate(transactionManager).executeWithoutResult(status -> auditLogRepository.save(auditLog));
		}catch(Exception e) {
			log.error("Could not record the clearing of {} audit log entries: {}", cleared, e.getMessage());
		}
	}
}
