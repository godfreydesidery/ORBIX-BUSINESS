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

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class StorageGoodReleaseResource {
	
	private final StorageGoodReleaseService storageGoodReleaseService;
	
	@GetMapping("/storage_good_releases/get")
	public ResponseEntity<StorageGoodReleaseResponseDTO>get(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request){
		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/create").toUriString());
		
		return ResponseEntity.created(uri).body(storageGoodReleaseService.get(id, request));
	}
	
	@GetMapping("/storage_good_releases/get_by_storage")
	public ResponseEntity<List<StorageGoodReleaseResponseDTO>>getByStorageId(
			@RequestParam(name = "storage_id") Long storageId,
			HttpServletRequest request){
		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/create").toUriString());
		
		return ResponseEntity.created(uri).body(storageGoodReleaseService.getStorageGoodReleases(storageId, request));
	}
	
	@GetMapping("/storage_good_releases/get_storage_good_release_detail")
	public ResponseEntity<StorageGoodReleaseDetail>getStorageGoodReleaseDetail(
			@RequestParam(name = "storage_id") Long storageId,
			HttpServletRequest request){
		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/create").toUriString());
		
		return ResponseEntity.created(uri).body(storageGoodReleaseService.showStorageGoodReleaseDetail(storageId, request));
	}
	
	@PostMapping("/storage_good_releases/create_storage_good_release")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<StorageGoodReleaseResponseDTO>createStorageCustomBillReceivable(
			@RequestBody StorageGoodReleaseRequestDTO storageGoodReleaseRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/storages/create").toUriString());
		
		return ResponseEntity.created(uri).body(storageGoodReleaseService.createStorageGoodRelease(storageGoodReleaseRequest, request));
	}
}
