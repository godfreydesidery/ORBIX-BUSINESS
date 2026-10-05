package com.orbix.api.modules.audit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbix.api.api.commons.PageRequests;
import com.orbix.api.api.commons.PageResponseDTO;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Deliberately not transactional at class level: recording never takes part in the action's transaction,
 * so nothing it does can roll the action back. Entries are written in transactions of their own.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceController implements AuditLogService {

	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";

	private final AuditLogRepository auditLogRepository;
	private final UserRepository userRepository;
	private final PlatformTransactionManager transactionManager;
	private final ObjectMapper objectMapper;

	@Override
	public void recordAction(AuditLog auditLog, Consumer<AuditLog> afterCommit) {
		// Who and where are read now, while the request is still being handled; the user's record is looked up when saving
		HttpServletRequest request = currentRequest();
		String username = (request != null && request.getUserPrincipal() != null) ? request.getUserPrincipal().getName() : null;
		auditLog.setUsername(AuditRequests.truncate(username, 100));
		auditLog.setIpAddress(AuditRequests.ipAddress(request));
		auditLog.setUserAgent(AuditRequests.userAgent(request));
		String forwardedFor = AuditRequests.forwardedFor(request);
		auditLog.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC));
		if(auditLog.getOutcome() == null) {
			auditLog.setOutcome(SUCCESS);
		}

		// Written only once the action has committed, so a rolled back action leaves no entry
		if(TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					finishAndSave(auditLog, username, forwardedFor, afterCommit);
				}
			});
		}else {
			finishAndSave(auditLog, username, forwardedFor, afterCommit);
		}
	}

	@Override
	@Async("auditExecutor")
	public void recordAuth(String action, String outcome, String username, String reason, String ipAddress, String forwardedFor, String userAgent) {
		AuditLog auditLog = new AuditLog();
		auditLog.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC));
		auditLog.setCategory("AUTH");
		auditLog.setAction(action);
		auditLog.setOutcome(outcome);
		auditLog.setUsername(AuditRequests.truncate(username, 100));
		auditLog.setEntityType("User");
		auditLog.setEntityRef(auditLog.getUsername());
		String summary = authSummary(action, username);
		auditLog.setSummary(AuditRequests.truncate(reason == null ? summary : summary + ": " + reason, 255));
		auditLog.setIpAddress(ipAddress);
		auditLog.setUserAgent(userAgent);
		finishAndSave(auditLog, username, forwardedFor, entry -> entry.setEntityId(entry.getUserId() == null ? null : entry.getUserId().toString()));
	}

	@Override
	@Async("auditExecutor")
	public void recordAccessDenied(String username, String path, String ipAddress, String forwardedFor, String userAgent) {
		AuditLog auditLog = new AuditLog();
		auditLog.setOccurredAt(LocalDateTime.now(ZoneOffset.UTC));
		auditLog.setCategory("SECURITY");
		auditLog.setAction("ACCESS_DENIED");
		auditLog.setOutcome(FAILURE);
		auditLog.setUsername(AuditRequests.truncate(username, 100));
		auditLog.setSummary(AuditRequests.truncate("Access denied to " + path, 255));
		auditLog.setIpAddress(ipAddress);
		auditLog.setUserAgent(userAgent);
		finishAndSave(auditLog, username, forwardedFor, null);
	}

	@Override
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public PageResponseDTO<AuditLogResponseDTO> getAuditLogPage(String from, String to, String category, String action, String outcome,
			Long userId, Long branchId, int page, int size, String search, HttpServletRequest request) {
		// The period is sent as UTC instants (ISO-8601); without one, the last seven days are shown
		LocalDateTime toTime = (to == null || to.isBlank()) ? LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1) : LocalDateTime.ofInstant(Instant.parse(to), ZoneOffset.UTC);
		LocalDateTime fromTime = (from == null || from.isBlank()) ? toTime.minusDays(7) : LocalDateTime.ofInstant(Instant.parse(from), ZoneOffset.UTC);

		// One page, newest first, searched on the user, reference, summary and address
		Page<AuditLog> auditLogs = auditLogRepository.getPage(fromTime, toTime, category == null ? "" : category, action == null ? "" : action,
				outcome == null ? "" : outcome, userId, branchId, PageRequests.searchPattern(search),
				PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
		List<AuditLogResponseDTO> auditLogResponses = auditLogs.getContent().stream()
				.map(this::auditLogResponseDTOMapper)
				.collect(Collectors.toList());
		return new PageResponseDTO<>(auditLogResponses, auditLogs.getTotalElements());
	}

	@Override
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public AuditLogResponseDTO get(Long id, HttpServletRequest request) {
		Optional<AuditLog> auditLog_ = auditLogRepository.findById(id);
		if(auditLog_.isEmpty()) {
			throw new NotFoundException("Audit log entry not found");
		}
		return auditLogResponseDTOMapper(auditLog_.get());
	}

	/**
	 * Finishes the entry and saves it, in a transaction of its own: looks up the user, then lets the caller complete
	 * the entry (e.g. with values read after the commit). A failure is logged and never reaches the action or sign-in.
	 */
	private void finishAndSave(AuditLog auditLog, String username, String forwardedFor, Consumer<AuditLog> finish) {
		try {
			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			transactionTemplate.executeWithoutResult(status -> {
				if(username != null) {
					userRepository.findByUsername(username).ifPresent(user -> setUser(auditLog, user));
				}
				if(finish != null) {
					try {
						finish.accept(auditLog);
					}catch(Exception e) {
						log.error("Could not complete audit log entry {}: {}", auditLog.getAction(), e.getMessage());
					}
				}
				auditLog.setDetails(withForwardedFor(auditLog.getDetails(), forwardedFor));
				auditLogRepository.save(auditLog);
			});
		}catch(Exception e) {
			log.error("Could not write audit log entry {} {} for {}: {}", auditLog.getCategory(), auditLog.getAction(), auditLog.getUsername(), e.getMessage());
		}
	}

	private void setUser(AuditLog auditLog, User user) {
		if(user == null) {
			return;
		}
		auditLog.setUserId(user.getId());
		auditLog.setUsername(AuditRequests.truncate(user.getUsername(), 100));
		auditLog.setCompanyId(user.getCompany() == null ? null : user.getCompany().getId());
		auditLog.setBranchId(user.getBranch() == null ? null : user.getBranch().getId());
	}

	// Adds the X-Forwarded-For header to the details, when the request carried one
	private String withForwardedFor(String details, String forwardedFor) {
		if(forwardedFor == null) {
			return details;
		}
		try {
			com.fasterxml.jackson.databind.node.ObjectNode node = (details == null || details.isEmpty())
					? objectMapper.createObjectNode()
					: (com.fasterxml.jackson.databind.node.ObjectNode) objectMapper.readTree(details);
			node.put("forwardedFor", forwardedFor);
			return objectMapper.writeValueAsString(node);
		}catch(Exception e) {
			return details;
		}
	}

	private String authSummary(String action, String username) {
		String name = username == null ? "(no username given)" : username;
		switch(action) {
			case "LOGIN_SUCCESS": return "Signed in as " + name;
			case "LOGIN_FAILED": return "Failed sign-in as " + name;
			case "TOKEN_REFRESHED": return "Session renewed for " + name;
			case "LOGOUT": return "Signed out " + name;
			default: return action + " " + name;
		}
	}

	private static HttpServletRequest currentRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return requestAttributes instanceof ServletRequestAttributes ? ((ServletRequestAttributes) requestAttributes).getRequest() : null;
	}

	private AuditLogResponseDTO auditLogResponseDTOMapper(AuditLog auditLog) {
		AuditLogResponseDTO auditLogResponse = new AuditLogResponseDTO();
		auditLogResponse.setId(auditLog.getId());
		auditLogResponse.setOccurredAt(auditLog.getOccurredAt().toString() + "Z");
		auditLogResponse.setCategory(auditLog.getCategory());
		auditLogResponse.setAction(auditLog.getAction());
		auditLogResponse.setOutcome(auditLog.getOutcome());
		auditLogResponse.setUserId(auditLog.getUserId());
		auditLogResponse.setUsername(auditLog.getUsername());
		auditLogResponse.setCompanyId(auditLog.getCompanyId());
		auditLogResponse.setBranchId(auditLog.getBranchId());
		auditLogResponse.setEntityType(auditLog.getEntityType());
		auditLogResponse.setEntityId(auditLog.getEntityId());
		auditLogResponse.setEntityRef(auditLog.getEntityRef());
		auditLogResponse.setSummary(auditLog.getSummary());
		auditLogResponse.setDetails(auditLog.getDetails());
		auditLogResponse.setIpAddress(auditLog.getIpAddress());
		auditLogResponse.setUserAgent(auditLog.getUserAgent());
		return auditLogResponse;
	}
}
