package com.orbix.api.api.vehicleandequipmentparking;

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

import com.orbix.api.api.commons.ApiCustomResponse;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class ParkingResource {
	
	private final ParkingService parkingService;
	
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	
	private final ParkingRepository parkingRepository;
	
	@GetMapping("/parkings")
	public ResponseEntity<List<ParkingResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingService.getAllParkings(request));
	}
	
	@GetMapping("/parkings/get_all_pending_or_checked_in")
	public ResponseEntity<List<ParkingResponseDTO>>getAllPendingAndCheckedIn(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingService.getAllPendingOrCheckedInParkings(request));
	}
	
	@GetMapping("/parkings/get_all_checked_in")
	public ResponseEntity<List<ParkingResponseDTO>>getAllCheckedIn(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingService.getAllCheckedInParkings(request));
	}
	
	@GetMapping("/parkings/get_all_cleared")
	public ResponseEntity<List<ParkingResponseDTO>>getAllCleared(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingService.getAllCleared(request));
	}
	
	@GetMapping("/parkings/get_today_checked_out")
	public ResponseEntity<List<ParkingResponseDTO>>getTodayCheckedOut(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingService.getTodayCheckedOut(request));
	}
	
	@GetMapping("/parkings/get_recent_checked_out")
	public ResponseEntity<List<ParkingResponseDTO>>getRecentCheckedOut(HttpServletRequest request){
		return ResponseEntity.ok().body(parkingService.getRecentCheckedOut(request));
	}
	
	
	@GetMapping("/parkings/get")
	public ResponseEntity<ParkingResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(parkingService.get(id, request));		
	}
	
	@GetMapping("/parkings/get_parking_bill_receivables")
	public ResponseEntity<List<ParkingBillReceivableResponseDTO>>getParkingBillReceivables(
			@RequestParam(name = "parking_id") Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(parkingService.getParkingBillReceivables(id, request));		
	}
	
	@PostMapping("/parkings/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingResponseDTO>create(
			@RequestBody ParkingRequestDTO parkingRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/create").toUriString());
		return ResponseEntity.created(uri).body(parkingService.createParking(parkingRequest, request));
	}
	
	@PostMapping("/parkings/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingResponseDTO>update(
			@RequestBody ParkingRequestDTO parkingRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/update").toUriString());
		return ResponseEntity.created(uri).body(parkingService.updateParking(parkingRequest, request));
	}
	
	@PostMapping("/parkings/check_in")
	public ResponseEntity<ParkingResponseDTO>checkIn(
			@RequestBody ParkingRequestDTO parkingRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(parkingService.checkIn(parkingRequest, request));		
	}
	
	@PostMapping("/parkings/check_out")
	public ResponseEntity<ParkingResponseDTO>checkOut(
			@RequestBody ParkingRequestDTO parkingRequest,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(parkingService.checkOut(parkingRequest, request));		
	}
	
	
	@GetMapping("/parkings/get_last_parking_bill_date")
	public Model getLastParkingBillDate(
			@RequestParam Long id,
			HttpServletRequest request){
		
		Model model = new Model();
		
		Optional<Parking> p = parkingRepository.findById(id);
		
		try {
			List<ParkingBillReceivable> rcs = parkingBillReceivableRepository.findAllByParking(p.get());
			model.setStringData((rcs.get(rcs.size() - 1).getEndedAt().minusDays(1)).toString());
		}catch(Exception e) {
			model.setStringData("");
		}
		
		return model;	
	}
	
	@PostMapping("/parkings/create_parking_bill_receivable")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ParkingBillReceivableResponseDTO>createParkingBillReceivable(
			@RequestBody ParkingBillReceivableRequestDTO parkingBillReceivableRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/create").toUriString());
		
		String dateString = parkingBillReceivableRequest.getStartedAt() + " 00:00:00";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime startedAt = LocalDateTime.parse(dateString, formatter);
		dateString = parkingBillReceivableRequest.getEndedAt() + " 00:00:00";
		LocalDateTime endedAt = LocalDateTime.parse(dateString, formatter);

		
		return ResponseEntity.created(uri).body(parkingService.createParkingBillReceivable(parkingBillReceivableRequest.getParkingId(), startedAt, endedAt, parkingBillReceivableRequest.getBillingType(), parkingBillReceivableRequest.getQty(), parkingBillReceivableRequest.getPrice(), parkingBillReceivableRequest.getDiscount(), parkingBillReceivableRequest.getAutoBilling(), request));
	}
	

	
	
	
	
//	@PostMapping("/parkings/activate")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ApiCustomResponse>activate(
//			@RequestBody ParkingRequestDTO parkingRequest,
//			HttpServletRequest request){
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/activate").toUriString());
//		return ResponseEntity.created(uri).body(parkingService.activateParking(parkingRequest, request));
//	}
	
	
//	@PostMapping("/parkings/deactivate")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ApiCustomResponse>deactivate(
//			@RequestBody ParkingRequestDTO parkingRequest,
//			HttpServletRequest request){
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/parkings/deactivate").toUriString());
//		return ResponseEntity.created(uri).body(parkingService.deactivateParking(parkingRequest, request));
//	}
	
}

@Data
class Model{
	String stringData = "";
}
