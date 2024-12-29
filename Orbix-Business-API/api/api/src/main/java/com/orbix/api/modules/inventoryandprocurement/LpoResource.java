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

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class LpoResource {

	private final LpoService lpoService;
	
	@GetMapping("/lpos/get_all_pending")
	public ResponseEntity<List<LpoResponseDTO>>getAllPendingByShop(
			@RequestParam(name = "shop_id") Long shopId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(lpoService.getAllPendingLpos(request));
	}
	
	@GetMapping("/lpos/get_all_visible_by_branch")
	public ResponseEntity<List<LpoResponseDTO>>getAllVisibleByBranch(
			HttpServletRequest request){
		return ResponseEntity.ok().body(lpoService.getAllVisibleLposByBranch(request));
	}
	
	@GetMapping("/lpos/get")
	public ResponseEntity<LpoResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(lpoService.get(id, request));		
	}
	
	@PostMapping("/lpos/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<LpoResponseDTO>create(
			@RequestBody LpoRequestDTO lpoRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/lpos/create").toUriString());
		return ResponseEntity.created(uri).body(lpoService.createLpo(lpoRequest, request));
	}
	
	@PostMapping("/lpos/create_detail")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public void createDetail(
			@RequestBody LpoDetailRequestDTO lpoDetailRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/lpos/create").toUriString());
		lpoService.createLpoDetail(lpoDetailRequest, request);
	}
	
	@GetMapping("/lpos/remove_detail")
	public void removeDetail(
			@RequestParam(name = "lpo_id") Long lpoId,
			@RequestParam(name = "lpo_detail_id") Long lpoDetailId,
			HttpServletRequest request){
		lpoService.removeLpoDetail(lpoDetailId, lpoId, request);		
	}
	
	@PostMapping("/lpos/approve")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean approve(
			@RequestParam(name = "lpo_id") Long lpoId,
			HttpServletRequest request){
		return lpoService.approveLpo(lpoId, request);
	}
	
	@PostMapping("/lpos/cancel")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean cancel(
			@RequestParam(name = "lpo_id") Long lpoId,
			HttpServletRequest request){
		return lpoService.cancelLpo(lpoId, request);
	}
}
