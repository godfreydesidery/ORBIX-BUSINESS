package com.orbix.api.modules.inventoryandprocurement;

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
import com.orbix.api.api.commons.PageResponseDTO;
import com.orbix.api.modules.audit.Audited;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class GrnResource {
	
	private final GrnService grnService;
	
	@GetMapping("/grns/get_all_pending")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<List<GrnResponseDTO>>getAllPendingByShop(
			@RequestParam(name = "shop_id") Long shopId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(grnService.getAllPendingGrns(request));
	}
	
	@GetMapping("/grns/get_all_visible_by_branch")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<List<GrnResponseDTO>>getAllVisibleByBranch(
			HttpServletRequest request){
		return ResponseEntity.ok().body(grnService.getAllVisibleGrnsByBranch(request));
	}
	
	@GetMapping("/grns/get_all_visible_by_shop")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<List<GrnResponseDTO>>getAllVisibleByShop(
			@RequestParam(name = "shop_id") Long shopId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(grnService.getAllVisibleGrnsByShop(shopId, request));
	}

	@GetMapping("/grns/get_all_visible_by_branch_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<GrnResponseDTO>>getAllVisibleByBranchPage(
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(grnService.getVisibleGrnPageByBranch(page, size, search, request));
	}

	@GetMapping("/grns/get_all_visible_by_shop_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<GrnResponseDTO>>getAllVisibleByShopPage(
			@RequestParam(name = "shop_id") Long shopId,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(grnService.getVisibleGrnPageByShop(shopId, page, size, search, request));
	}
	
	@GetMapping("/grns/get")
	public ResponseEntity<GrnResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(grnService.get(id, request));		
	}
	
	@PostMapping("/grns/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<GrnResponseDTO>create(
			@RequestBody GrnRequestDTO grnRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/grns/create").toUriString());
		return ResponseEntity.created(uri).body(grnService.createGrn(grnRequest, request));
	}
	
	@PostMapping("/grns/create_by_lpo_no")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<GrnResponseDTO>createByLpoNo(
			@RequestParam(name = "lpo_no") String lpoNo,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/grns/create").toUriString());
		return ResponseEntity.created(uri).body(grnService.createGrnByLpoNo(lpoNo, request));
	}
	
	@PostMapping("/grns/create_detail")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public void createDetail(
			@RequestBody GrnDetailRequestDTO grnDetailRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/grns/create").toUriString());
		grnService.createGrnDetail(grnDetailRequest, request);
	}
	
	@GetMapping("/grns/remove_detail")
	public void removeDetail(
			@RequestParam(name = "grn_id") Long grnId,
			@RequestParam(name = "grn_detail_id") Long grnDetailId,
			HttpServletRequest request){
		grnService.removeGrnDetail(grnDetailId, grnId, request);		
	}
	
	@GetMapping("/grns/add_received")
	public void addReceived(
			@RequestParam(name = "grn_id") Long grnId,
			@RequestParam(name = "grn_detail_id") Long grnDetailId,
			@RequestParam(name = "qty") double qty,
			HttpServletRequest request){
		grnService.addReceived(grnDetailId, grnId, qty, request);		
	}
	
	@PostMapping("/grns/approve")
	@Audited(category = "PROCUREMENT", action = "GRN_APPROVED", entityType = "Grn", entityId = "grnId", summary = "Approved GRN {ref}", changeOf = Grn.class, changeId = "grnId")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean approve(
			@RequestParam(name = "grn_id") Long grnId,
			HttpServletRequest request){
		return grnService.approveGrn(grnId, request);
	}
	
	@PostMapping("/grns/cancel")
	@Audited(category = "PROCUREMENT", action = "GRN_CANCELLED", entityType = "Grn", entityId = "grnId", summary = "Cancelled GRN {ref}", changeOf = Grn.class, changeId = "grnId")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean cancel(
			@RequestParam(name = "grn_id") Long grnId,
			HttpServletRequest request){
		return grnService.cancelGrn(grnId, request);
	}
	
	@PostMapping("/grns/archive")
	@Audited(category = "PROCUREMENT", action = "GRN_ARCHIVED", entityType = "Grn", entityId = "grnId", summary = "Archived GRN {ref}", changeOf = Grn.class, changeId = "grnId")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean archive(
			@RequestParam(name = "grn_id") Long grnId,
			HttpServletRequest request){
		return grnService.archiveGrn(grnId, request);
	}
}
