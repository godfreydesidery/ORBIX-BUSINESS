package com.orbix.api.modules.vehicleandequipmentmaintenance;

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
public class MaintenanceJobCardIssueBillReceivableResource {
	
	private final MaintenanceJobCardIssueBillReceivableService maintenanceJobCardIssueBillReceivableService;
	
	@GetMapping("/maintenance_bill_receivables/get_all_by_maintenance")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<List<MaintenanceJobCardIssueBillReceivableResponseDTO>> getAllByMaintenance(
			@RequestParam(name = "maintenance_id") Long maintenanceId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(maintenanceJobCardIssueBillReceivableService.getAllByMaintenance(maintenanceId, request));
	}
	
	@GetMapping("/maintenance_bill_receivables/get")
	public ResponseEntity<MaintenanceJobCardIssueBillReceivableResponseDTO> getMaintenanceJobCardIssueBill(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(maintenanceJobCardIssueBillReceivableService.getMaintenanceJobCardIssueBillReceivable(id, request));
	}
	
//	@PostMapping("/maintenance_bill_receivables/create_maintenance_bill_receivable")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<MaintenanceJobCardIssueBillReceivableResponseDTO>createMaintenanceJobCardIssueBill(
//			@RequestBody MaintenanceJobCardIssueBillReceivableRequestDTO maintenanceJobCardIssueBillReceivableRequest,
//			HttpServletRequest request){		
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_bill_receivables/create_maintenance_bill_receivable").toUriString());
//		return ResponseEntity.created(uri).body(maintenanceJobCardIssueBillReceivableService.createMaintenanceJobCardIssueBillReceivable(maintenanceJobCardIssueBillReceivableRequest, request));
//	}
	
	@PostMapping("/maintenance_bill_receivables/update_maintenance_bill_receivable")
	@Audited(category = "FINANCE", action = "BILL_UPDATED", entityType = "MaintenanceJobCardIssueBillReceivable", entityRef = "result.maintenanceId", summary = "Updated maintenance bill {result.id} for maintenance {result.maintenanceId}: {result.qty} x {result.price}, discount {result.discount}", details = {"result.maintenanceId", "result.qty", "result.price", "result.discount", "result.amount", "result.startedAt", "result.endedAt"}, changeOf = MaintenanceJobCardIssueBillReceivable.class, changeId = "maintenanceJobCardIssueBillReceivableRequest.id")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<MaintenanceJobCardIssueBillReceivableResponseDTO>updateMaintenanceJobCardIssueBill(
			@RequestBody MaintenanceJobCardIssueBillReceivableRequestDTO maintenanceJobCardIssueBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/maintenance_bill_receivables/update_maintenance_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(maintenanceJobCardIssueBillReceivableService.updateMaintenanceJobCardIssueBillReceivable(maintenanceJobCardIssueBillReceivableRequest, request));
	}
}
