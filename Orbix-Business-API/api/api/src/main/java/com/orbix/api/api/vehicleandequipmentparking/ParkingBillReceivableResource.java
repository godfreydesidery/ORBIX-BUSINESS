package com.orbix.api.api.vehicleandequipmentparking;

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
public class ParkingBillReceivableResource {
	private final ParkingBillReceivableService parkingBillReceivableService;
	
	@GetMapping("/parking_bill_receivables/get_all_by_parking")
	public ResponseEntity<List<ParkingBillReceivableResponseDTO>> getAllByParking(
			@RequestParam(name = "parking_id") Long parkingId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(parkingBillReceivableService.getAllByParking(parkingId, request));
	}
	
	@GetMapping("/parking_bill_receivables/get")
	public ResponseEntity<ParkingBillReceivableResponseDTO> getParkingBill(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(parkingBillReceivableService.getParkingBillReceivable(id, request));
	}
	
	@GetMapping("/parking_service_bill_receivables/get")
	public ResponseEntity<ParkingServiceBillReceivableResponseDTO> getParkingServiceBill(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(parkingBillReceivableService.getServiceBillReceivable(id, request));
	}
	
	@GetMapping("/service_bill_receivables/get_all_by_parking")
	public ResponseEntity<List<ParkingServiceBillReceivableResponseDTO>> getAllServiceByParking(
			@RequestParam(name = "parking_id") Long parkingId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(parkingBillReceivableService.getAllServiceByParking(parkingId, request));
	}
	
	@PostMapping("/parking_bill_receivables/create_parking_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingBillReceivableResponseDTO>createParkingBill(
			@RequestBody ParkingBillReceivableRequestDTO parkingBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_bill_receivables/create_parking_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(parkingBillReceivableService.createParkingBillReceivable(parkingBillReceivableRequest, request));
	}
	
	@PostMapping("/parking_bill_receivables/update_parking_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingBillReceivableResponseDTO>updateParkingBill(
			@RequestBody ParkingBillReceivableRequestDTO parkingBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_bill_receivables/update_parking_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(parkingBillReceivableService.updateParkingBillReceivable(parkingBillReceivableRequest, request));
	}
	
	@PostMapping("/parking_bill_receivables/create_service_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingServiceBillReceivableResponseDTO>createServiceBill(
			@RequestBody ParkingServiceBillReceivableRequestDTO parkingServiceBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_bill_receivables/create_service_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(parkingBillReceivableService.createServiceBillReceivable(parkingServiceBillReceivableRequest, request));
	}
	
	@PostMapping("/parking_bill_receivables/update_service_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingServiceBillReceivableResponseDTO>updateServiceBill(
			@RequestBody ParkingServiceBillReceivableRequestDTO parkingServiceBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_bill_receivables/update_service_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(parkingBillReceivableService.updateServiceBillReceivable(parkingServiceBillReceivableRequest, request));
	}
	
	@PostMapping("/parking_bill_receivables/delete_service_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<Boolean> deleteServiceBill(
			@RequestBody ParkingServiceBillReceivableRequestDTO parkingServiceBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parking_bill_receivables/delete_service_bill_receivable").toUriString());
		return ResponseEntity.created(uri).body(parkingBillReceivableService.deleteServiceBillReceivable(parkingServiceBillReceivableRequest, request));
	}
	
	
	
}
