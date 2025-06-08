package com.orbix.api.modules.finance;

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

import com.orbix.api.api.vehicleandequipmentparking.ParkingRequestDTO;
import com.orbix.api.api.vehicleandequipmentparking.ParkingResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class DiscountRequestResource {
	
	private final DiscountRequestService discountRequestService;

	@GetMapping("/discount_requests")
	public ResponseEntity<List<DiscountRequestResponseDTO>>getAll(
			@RequestParam(name = "service_id") Long serviceId,
			@RequestParam(name = "service_name") String serviceName,
			HttpServletRequest request){
		return ResponseEntity.ok().body(discountRequestService.getRequests(serviceId, serviceName, request));
	}
	
	@GetMapping("/discount_requests/get")
	public ResponseEntity<DiscountRequestResponseDTO>getDiscount(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request){
		return ResponseEntity.ok().body(discountRequestService.get(id, request));
	}
	
	@GetMapping("/discount_requests/get_discount")
	public ResponseEntity<DiscountRequestResponseDTO>getDiscount(
			@RequestParam(name = "service_bill_id") Long serviceBillId,
			@RequestParam(name = "bill_amount") double billAmount,
			@RequestParam(name = "discount_amount") double discountAmount,
			@RequestParam(name = "service_bill_name") String serviceBillName,
			HttpServletRequest request){
		return ResponseEntity.ok().body(discountRequestService.getDiscount(serviceBillId, billAmount, discountAmount, serviceBillName, request));
	}
	
	@PostMapping("/discount_requests/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<DiscountRequestResponseDTO>create(
			@RequestParam(name = "service_bill_id") Long serviceBillId,
			@RequestParam(name = "bill_amount") double billAmount,
			@RequestParam(name = "discount_amount") double discountAmount,
			@RequestParam(name = "service_bill_name") String serviceBillName,
			@RequestBody DiscountRequestRequestDTO discountRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/discount_requests/create").toUriString());
		return ResponseEntity.created(uri).body(discountRequestService.createDiscountRequest(discountRequest, serviceBillId, billAmount, discountAmount, serviceBillName, request));
	}
	
	@PostMapping("/discount_requests/approve")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<Boolean>approve(
			@RequestBody DiscountRequestRequestDTO discountRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/discount_requests/approve").toUriString());
		return ResponseEntity.created(uri).body(discountRequestService.approve(discountRequest, request));
	}
	
	@PostMapping("/discount_requests/reject")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<Boolean>reject(
			@RequestBody DiscountRequestRequestDTO discountRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/discount_requests/approve").toUriString());
		return ResponseEntity.created(uri).body(discountRequestService.reject(discountRequest, request));
	}
	
}
