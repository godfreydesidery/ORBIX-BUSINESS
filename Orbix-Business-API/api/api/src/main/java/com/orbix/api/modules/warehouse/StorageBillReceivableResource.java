package com.orbix.api.modules.warehouse;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import com.orbix.api.modules.audit.Audited;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class StorageBillReceivableResource {
	
	private final StorageBillReceivableService storageBillReceivableService;
	
	@GetMapping("/storage_bill_receivables/get_all_by_storage")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<List<StorageBillReceivableResponseDTO>> getAllByStorage(
			@RequestParam(name = "storage_id") Long storageId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(storageBillReceivableService.getAllByStorage(storageId, request));
	}
	
	@GetMapping("/storage_bill_receivables/get")
	public ResponseEntity<StorageBillReceivableResponseDTO> getStorageBill(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(storageBillReceivableService.getStorageBillReceivable(id, request));
	}
	
	@PostMapping("/storage_bill_receivables/create_storage_bill_receivable")
	@Audited(category = "FINANCE", action = "BILL_CREATED", entityType = "StorageBillReceivable", entityRef = "result.storageId", summary = "Created storage bill {result.id} for storage {result.storageId}: {result.qty} x {result.price}, discount {result.discount}", details = {"result.storageId", "result.qty", "result.price", "result.discount", "result.amount", "result.startedAt", "result.endedAt"})
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<StorageBillReceivableResponseDTO>createStorageBill(
			@RequestBody StorageBillReceivableRequestDTO storageBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storage_bill_receivables/create_storage_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(storageBillReceivableService.createStorageBillReceivable(storageBillReceivableRequest, request));
	}
	
	@PostMapping("/storage_bill_receivables/update_storage_bill_receivable")
	@Audited(category = "FINANCE", action = "BILL_UPDATED", entityType = "StorageBillReceivable", entityRef = "result.storageId", summary = "Updated storage bill {result.id} for storage {result.storageId}: {result.qty} x {result.price}, discount {result.discount}", details = {"result.storageId", "result.qty", "result.price", "result.discount", "result.amount", "result.startedAt", "result.endedAt"}, changeOf = StorageBillReceivable.class, changeId = "storageBillReceivableRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<StorageBillReceivableResponseDTO>updateStorageBill(
			@RequestBody StorageBillReceivableRequestDTO storageBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storage_bill_receivables/update_storage_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(storageBillReceivableService.updateStorageBillReceivable(storageBillReceivableRequest, request));
	}
	
	@GetMapping("/storage_bill_receivables/get_bill_view")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<BillViewResponseDTO> getBillView(
			@RequestParam(name = "storage_id") Long storageId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(storageBillReceivableService.getBillView(storageId, request));
	}
}
