package com.orbix.api.modules.bond;

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

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class BondItemBillReceivableResource {
	
	private final BondItemBillReceivableService bondItemBillReceivableService;
	
	@GetMapping("/bond_item_bill_receivables/get_all_by_bond_item")
	public ResponseEntity<List<BondItemBillReceivableResponseDTO>> getAllByBondItem(
			@RequestParam(name = "bond_item_id") Long bondItemId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(bondItemBillReceivableService.getAllByBondItem(bondItemId, request));
	}
	
	@GetMapping("/bond_item_bill_receivables/get")
	public ResponseEntity<BondItemBillReceivableResponseDTO> getBondItemBill(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(bondItemBillReceivableService.getBondItemBillReceivable(id, request));
	}
	
	@PostMapping("/bond_item_bill_receivables/create_bond_item_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemBillReceivableResponseDTO>createBondItemBill(
			@RequestBody BondItemBillReceivableRequestDTO bondItemBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_item_bill_receivables/create_bond_item_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(bondItemBillReceivableService.createBondItemBillReceivable(bondItemBillReceivableRequest, request));
	}
	
	@PostMapping("/bond_item_bill_receivables/update_bond_item_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<BondItemBillReceivableResponseDTO>updateBondItemBill(
			@RequestBody BondItemBillReceivableRequestDTO bondItemBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/bond_item_bill_receivables/update_bond_item_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(bondItemBillReceivableService.updateBondItemBillReceivable(bondItemBillReceivableRequest, request));
	}
	
	@GetMapping("/bond_item_bill_receivables/get_bill_view")
	public ResponseEntity<BillViewResponseDTO> getBillView(
			@RequestParam(name = "bond_item_id") Long bondItemId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(bondItemBillReceivableService.getBillView(bondItemId, request));
	}
}
