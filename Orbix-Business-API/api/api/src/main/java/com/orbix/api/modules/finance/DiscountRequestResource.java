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
	public ResponseEntity<List<DiscountRequestResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(discountRequestService.getRequests(request));
	}
	
	@GetMapping("/discount_requests/get_discount")
	public ResponseEntity<Double>getDiscount(
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
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/discount_requests/create").toUriString());
		return ResponseEntity.created(uri).body(discountRequestService.createDiscountRequest(serviceBillId, billAmount, discountAmount, serviceBillName, request));
	}
	
}
