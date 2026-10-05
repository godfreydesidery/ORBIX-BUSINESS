package com.orbix.api.modules.audit;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.api.commons.PageResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class AuditLogResource {

	private final AuditLogService auditLogService;

	@GetMapping("/audit_logs/get_page")
	@PreAuthorize("hasAnyAuthority('AUDIT-ACCESS','AUDIT-READ')")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<AuditLogResponseDTO>>getPage(
			@RequestParam(name = "from", defaultValue = "") String from,
			@RequestParam(name = "to", defaultValue = "") String to,
			@RequestParam(name = "category", defaultValue = "") String category,
			@RequestParam(name = "action", defaultValue = "") String action,
			@RequestParam(name = "outcome", defaultValue = "") String outcome,
			@RequestParam(name = "user_id", required = false) Long userId,
			@RequestParam(name = "branch_id", required = false) Long branchId,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(auditLogService.getAuditLogPage(from, to, category, action, outcome, userId, branchId, page, size, search, request));
	}

	@GetMapping("/audit_logs/get")
	@PreAuthorize("hasAnyAuthority('AUDIT-ACCESS','AUDIT-READ')")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<AuditLogResponseDTO>get(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request){
		return ResponseEntity.ok().body(auditLogService.get(id, request));
	}
}
