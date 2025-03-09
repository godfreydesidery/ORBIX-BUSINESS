package com.orbix.api.modules.warehouse;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class StorageResource {
	private final StorageService storageService;
	
	private final StorageBillReceivableRepository storageBillReceivableRepository;
	
	private final StorageRepository storageRepository;
	
	@GetMapping("/storages")
	public ResponseEntity<List<StorageResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(storageService.getAllStorages(request));
	}
	
	@GetMapping("/storages/get_all_pending_or_checked_in")
	public ResponseEntity<List<StorageResponseDTO>>getAllPendingAndCheckedIn(HttpServletRequest request){
		return ResponseEntity.ok().body(storageService.getAllPendingOrCheckedInStorages(request));
	}
	
	@GetMapping("/storages/get_all_pending_or_checked_in_by_warehouse")
	public ResponseEntity<List<StorageResponseDTO>>getAllPendingAndCheckedInByWarehouse(
			@RequestParam(name = "warehouse_id") Long warehouseId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(storageService.getAllPendingOrCheckedInStoragesByWarehouse(warehouseId, request));
	}
	
	@GetMapping("/storages/get_all_recent_checked_out_by_warehouse")
	public ResponseEntity<List<StorageResponseDTO>>getAllRecentCheckedOutByWarehouse(
			@RequestParam(name = "warehouse_id") Long warehouseId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(storageService.getAllRecentCheckedOutStoragesByWarehouse(warehouseId, request));
	}
	
	@GetMapping("/storages/get_all_checked_in")
	public ResponseEntity<List<StorageResponseDTO>>getAllCheckedIn(HttpServletRequest request){
		return ResponseEntity.ok().body(storageService.getAllCheckedInStorages(request));
	}
	
	@GetMapping("/storages/get_all_cleared")
	public ResponseEntity<List<StorageResponseDTO>>getAllCleared(HttpServletRequest request){
		return ResponseEntity.ok().body(storageService.getAllCleared(request));
	}
	
	@GetMapping("/storages/get_today_checked_out")
	public ResponseEntity<List<StorageResponseDTO>>getTodayCheckedOut(HttpServletRequest request){
		return ResponseEntity.ok().body(storageService.getTodayCheckedOut(request));
	}
	
	
	@GetMapping("/storages/get")
	public ResponseEntity<StorageResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(storageService.get(id, request));		
	}
	
	@GetMapping("/storages/get_storage_bill_receivables")
	public ResponseEntity<List<StorageBillReceivableResponseDTO>>getStorageBillReceivables(
			@RequestParam(name = "storage_id") Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(storageService.getStorageBillReceivables(id, request));		
	}
	
	@PostMapping("/storages/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<StorageResponseDTO>create(
			@RequestBody StorageRequestDTO storageRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/create").toUriString());
		return ResponseEntity.created(uri).body(storageService.createStorage(storageRequest, request));
	}
	
	@PostMapping("/storages/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<StorageResponseDTO>update(
			@RequestBody StorageRequestDTO storageRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/update").toUriString());
		return ResponseEntity.created(uri).body(storageService.updateStorage(storageRequest, request));
	}
	
	@PostMapping("/storages/check_in")
	public ResponseEntity<StorageResponseDTO>checkIn(
			@RequestBody StorageRequestDTO storageRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(storageService.checkIn(storageRequest, request));		
	}
	
	@PostMapping("/storages/check_out")
	public ResponseEntity<StorageResponseDTO>checkOut(
			@RequestBody StorageRequestDTO storageRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(storageService.checkOut(storageRequest, request));		
	}
	
	
	@GetMapping("/storages/get_last_storage_bill_date")
	public Model getLastStorageBillDate(
			@RequestParam Long id,
			HttpServletRequest request){
		
		Model model = new Model();
		
		Optional<Storage> p = storageRepository.findById(id);
		
		try {
			List<StorageBillReceivable> rcs = storageBillReceivableRepository.findAllByStorage(p.get());
			model.setStringData((rcs.get(rcs.size() - 1).getEndedAt().minusDays(1)).toString());
		}catch(Exception e) {
			model.setStringData("");
		}
		
		return model;	
	}
	
	@PostMapping("/storages/create_storage_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<StorageBillReceivableResponseDTO>createStorageBillReceivable(
			@RequestBody StorageBillReceivableRequestDTO storageBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/create").toUriString());
		
		String dateString = storageBillReceivableRequest.getStartedAt() + " 00:00:00";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime startedAt = LocalDateTime.parse(dateString, formatter);
		dateString = storageBillReceivableRequest.getEndedAt() + " 00:00:00";
		LocalDateTime endedAt = LocalDateTime.parse(dateString, formatter);

		
		return ResponseEntity.created(uri).body(storageService.createStorageBillReceivable(storageBillReceivableRequest.getStorageId(), startedAt, endedAt, storageBillReceivableRequest.getBillingType(), storageBillReceivableRequest.getQty(), storageBillReceivableRequest.getPrice(), storageBillReceivableRequest.getDiscount(), storageBillReceivableRequest.getAutoBilling(), request));
	}
	

	
	
	
	
//	@PostMapping("/storages/activate")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ApiCustomResponse>activate(
//			@RequestBody StorageRequestDTO storageRequest,
//			HttpServletRequest request){
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/activate").toUriString());
//		return ResponseEntity.created(uri).body(storageService.activateStorage(storageRequest, request));
//	}
	
	
//	@PostMapping("/storages/deactivate")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ApiCustomResponse>deactivate(
//			@RequestBody StorageRequestDTO storageRequest,
//			HttpServletRequest request){
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/deactivate").toUriString());
//		return ResponseEntity.created(uri).body(storageService.deactivateStorage(storageRequest, request));
//	}
}

@Data
class Model{
	String stringData = "";
}
