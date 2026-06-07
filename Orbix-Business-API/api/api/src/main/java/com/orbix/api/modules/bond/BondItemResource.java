package com.orbix.api.modules.bond;

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

import com.orbix.api.api.vehicleandequipmentparking.MonthlyParkingStatusResponseDTO;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRequestDTO;
import com.orbix.api.api.vehicleandequipmentparking.ParkingResponseDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class BondItemResource {
	private final BondItemService bondItemService;
	private final BondItemBillReceivableService bondItemBillReceivableService;
	
	private final BondItemBillReceivableRepository bondItemBillReceivableRepository;
	
	private final BondItemRepository bondItemRepository;
	
	@GetMapping("/bond_items")
	public ResponseEntity<List<BondItemResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getAllBondItems(request));
	}
	
	@GetMapping("/bond_items/get_all_pending_or_checked_in")
	public ResponseEntity<List<BondItemResponseDTO>>getAllPendingAndCheckedIn(HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getAllPendingOrCheckedInBondItems(request));
	}
	
	@GetMapping("/bond_items/get_all_with_discounts")
	public ResponseEntity<List<BondItemResponseDTO>>getAllWithDiscounts(HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getAllWithDiscounts(request));
	}
	
	@GetMapping("/bond_items/get_all_pending_or_checked_in_by_bond_zone")
	public ResponseEntity<List<BondItemResponseDTO>>getAllPendingAndCheckedInByBondZone(
			@RequestParam(name = "bond_zone_id") Long bondZoneId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getAllPendingOrCheckedInBondItemsByBondZone(bondZoneId, request));
	}
	
	@GetMapping("/bond_items/get_all_checked_in_by_bond_zone")
	public ResponseEntity<List<BondItemResponseDTO>>getAllCheckedInByBondZone(
			@RequestParam(name = "bond_zone_id") Long bondZoneId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getAllCheckedInBondItemsByBondZone(bondZoneId, request));
	}
	
	@GetMapping("/bond_items/get_all_recent_checked_out_by_bond_zone")
	public ResponseEntity<List<BondItemResponseDTO>>getAllRecentCheckedOutByBondZone(
			@RequestParam(name = "bond_zone_id") Long bondZoneId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getAllRecentCheckedOutBondItemsByBondZone(bondZoneId, request));
	}
	
	@GetMapping("/bond_items/get_all_checked_in")
	public ResponseEntity<List<BondItemResponseDTO>>getAllCheckedIn(
			@RequestParam(name = "bond_zone_id") Long bondZoneId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getAllCheckedInBondItems(bondZoneId, request));
	}
	
	@GetMapping("/bond_items/get_all_cleared")
	public ResponseEntity<List<BondItemResponseDTO>>getAllCleared(HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getAllCleared(request));
	}
	
	@GetMapping("/bond_items/get_today_checked_out")
	public ResponseEntity<List<BondItemResponseDTO>>getTodayCheckedOut(HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getTodayCheckedOut(request));
	}
	
	
	@GetMapping("/bond_items/get")
	public ResponseEntity<BondItemResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(bondItemService.get(id, request));		
	}
	
	@GetMapping("/bond_items/get_bond_item_bill_receivables")
	public ResponseEntity<List<BondItemBillReceivableResponseDTO>>getBondItemBillReceivables(
			@RequestParam(name = "bond_item_id") Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(bondItemService.getBondItemBillReceivables(id, request));		
	}
	
	@PostMapping("/bond_items/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemResponseDTO>create(
			@RequestBody BondItemRequestDTO bondItemRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_items/create").toUriString());
		return ResponseEntity.created(uri).body(bondItemService.createBondItem(bondItemRequest, request));
	}
	
	@PostMapping("/bond_items/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemResponseDTO>update(
			@RequestBody BondItemRequestDTO bondItemRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_items/update").toUriString());
		return ResponseEntity.created(uri).body(bondItemService.updateBondItem(bondItemRequest, request));
	}
	
	@PostMapping("/bond_items/check_in")
	public ResponseEntity<BondItemResponseDTO>checkIn(
			@RequestBody BondItemRequestDTO bondItemRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(bondItemService.checkIn(bondItemRequest, request));		
	}
	
	@PostMapping("/bond_items/check_out")
	public ResponseEntity<BondItemResponseDTO>checkOut(
			@RequestBody BondItemRequestDTO bondItemRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(bondItemService.checkOut(bondItemRequest, request));		
	}
	
	@PostMapping("/bond_items/archive")
	public ResponseEntity<BondItemResponseDTO>archive(
			@RequestBody BondItemRequestDTO bondItemRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(bondItemService.archive(bondItemRequest, request));		
	}
	
	
	@GetMapping("/bond_items/get_last_bond_item_bill_date")
	public Model getLastBondItemBillDate(
			@RequestParam Long id,
			HttpServletRequest request){
		
		Model model = new Model();
		
		Optional<BondItem> p = bondItemRepository.findById(id);
		
		try {
			List<BondItemBillReceivable> rcs = bondItemBillReceivableRepository.findAllByBondItem(p.get());
			model.setStringData((rcs.get(rcs.size() - 1).getEndedAt().minusDays(1)).toString());
		}catch(Exception e) {
			model.setStringData("");
		}
		
		return model;	
	}
	
	@PostMapping("/bond_items/create_bond_item_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemBillReceivableResponseDTO>createBondItemBillReceivable(
			@RequestBody BondItemBillReceivableRequestDTO bondItemBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_items/create").toUriString());
		
		String dateString = bondItemBillReceivableRequest.getStartedAt() + " 00:00:00";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime startedAt = LocalDateTime.parse(dateString, formatter);
		dateString = bondItemBillReceivableRequest.getEndedAt() + " 00:00:00";
		LocalDateTime endedAt = LocalDateTime.parse(dateString, formatter);

		
		return ResponseEntity.created(uri).body(bondItemService.createBondItemBillReceivable(bondItemBillReceivableRequest.getBondItemId(), startedAt, endedAt, bondItemBillReceivableRequest.getBillingType(), bondItemBillReceivableRequest.getQty(), bondItemBillReceivableRequest.getPrice(), bondItemBillReceivableRequest.getDiscount(), bondItemBillReceivableRequest.getAutoBilling(), request));
	}
	
	@PostMapping("/bond_items/create_bond_item_custom_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemBillReceivableResponseDTO>createBondItemCustomBillReceivable(
			@RequestBody BondItemBillReceivableRequestDTO bondItemBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bondItems/create").toUriString());
		
//		String dateString = bondItemBillReceivableRequest.getStartedAt() + " 00:00:00";
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//		LocalDateTime startedAt = LocalDateTime.parse(dateString, formatter);
//		dateString = bondItemBillReceivableRequest.getEndedAt() + " 00:00:00";
//		LocalDateTime endedAt = LocalDateTime.parse(dateString, formatter);

		
		return ResponseEntity.created(uri).body(bondItemBillReceivableService.createBondItemCustomBillReceivable(bondItemBillReceivableRequest, request));
	}
	

	@GetMapping("/bond_items/get_custom_bill_item")
	public ResponseEntity<BondItemCustomBillDetail>getCustomBill(
			@RequestParam(name = "bond_item_id") Long bondItemId,
			HttpServletRequest request){
		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_items/create").toUriString());
		
		return ResponseEntity.created(uri).body(bondItemService.showBondItemCustomBillDetail(bondItemId, request));
	}
	
	@GetMapping("/bond_items/get_bond_item_summary")
	public ResponseEntity<List<MonthlyBondItemStatusResponseDTO>>getBondItemSummary(
			@RequestParam(name = "year") int year,
			HttpServletRequest request){
		return ResponseEntity.ok().body(bondItemService.getMonthlyStats(year, request));
	}
	
	@PostMapping("/bond_items/modify")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemResponseDTO>modify(
			@RequestBody BondItemRequestDTO bondItemRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_items/modify").toUriString());
		return ResponseEntity.created(uri).body(bondItemService.modifyBondItem(bondItemRequest, request));
	}
	
	
	
//	@PostMapping("/bond_items/activate")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ApiCustomResponse>activate(
//			@RequestBody BondItemRequestDTO bondItemRequest,
//			HttpServletRequest request){
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bondItems/activate").toUriString());
//		return ResponseEntity.created(uri).body(bondItemService.activateBondItem(bondItemRequest, request));
//	}
	
	
//	@PostMapping("/bond_items/deactivate")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ApiCustomResponse>deactivate(
//			@RequestBody BondItemRequestDTO bondItemRequest,
//			HttpServletRequest request){
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_items/deactivate").toUriString());
//		return ResponseEntity.created(uri).body(bondItemService.deactivateBondItem(bondItemRequest, request));
//	}
}

@Data
class Model{
	String stringData = "";
}
