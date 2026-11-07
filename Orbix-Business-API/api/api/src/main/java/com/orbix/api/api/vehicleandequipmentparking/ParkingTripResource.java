package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.api.commons.PageResponseDTO;
import com.orbix.api.modules.audit.Audited;

import lombok.RequiredArgsConstructor;

/**
 * Temporary trips of checked-in vehicles out of the yard: the cashier sends a vehicle out with a
 * temporary gate pass, reception sends it back in when it returns.
 */
@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class ParkingTripResource {

	private final ParkingTripService parkingTripService;

	@PostMapping("/parkings/send_out")
	@Audited(category = "OPERATIONS", action = "VEHICLE_SENT_OUT", entityType = "Parking", entityId = "result.parkingId", entityRef = "result.parkingNo", summary = "Sent out parking {result.parkingNo} ({result.chasisNo}) on gate pass {result.no}: {result.reason}, driver {result.driverName}", details = {"result.no", "result.reason", "result.driverName", "result.driverIdNo", "result.documentsLeft", "result.expectedReturnAt"})
	@PreAuthorize("hasAnyAuthority('PKNG-SENDOUT')")
	public ResponseEntity<ParkingTripResponseDTO> sendOut(
			@RequestBody ParkingTripRequestDTO tripRequest,
			HttpServletRequest request){
		return ResponseEntity.ok().body(parkingTripService.sendOut(tripRequest, request));
	}

	@PostMapping("/parkings/send_in")
	@Audited(category = "OPERATIONS", action = "VEHICLE_SENT_IN", entityType = "Parking", entityId = "result.parkingId", entityRef = "result.parkingNo", summary = "Received back parking {result.parkingNo} ({result.chasisNo}) on gate pass {result.no}", details = {"result.no", "result.documentsReturned", "result.returnNotes"})
	@PreAuthorize("hasAnyAuthority('PKNG-SENDIN')")
	public ResponseEntity<ParkingTripResponseDTO> sendIn(
			@RequestBody ParkingTripRequestDTO tripRequest,
			HttpServletRequest request){
		return ResponseEntity.ok().body(parkingTripService.sendIn(tripRequest, request));
	}

	@GetMapping("/parkings/get_trip")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<ParkingTripResponseDTO> getTrip(
			@RequestParam(name = "id") Long id,
			HttpServletRequest request){
		return ResponseEntity.ok().body(parkingTripService.get(id, request));
	}

	@GetMapping("/parkings/get_open_trip")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<ParkingTripResponseDTO> getOpenTrip(
			@RequestParam(name = "parking_id") Long parkingId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(parkingTripService.getOpenTrip(parkingId, request));
	}

	@GetMapping("/parkings/get_trips_by_parking")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<List<ParkingTripResponseDTO>> getTripsByParking(
			@RequestParam(name = "parking_id") Long parkingId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(parkingTripService.getTripsByParking(parkingId, request));
	}

	@GetMapping("/parkings/get_all_outside_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<ParkingTripResponseDTO>> getOutsidePage(
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(parkingTripService.getOutsidePage(page, size, search, request));
	}

	@PostMapping("/parking_reports/get_trip_report")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	@PreAuthorize("hasAnyAuthority('PKNG-TRIPS','MNGNT-ACCESS','ADMIN-ACCESS')")
	public ResponseEntity<List<ParkingTripResponseDTO>> getTripReport(
			@RequestBody DateRange dateRange,
			HttpServletRequest request){
		return ResponseEntity.ok().body(parkingTripService.getReport(dateRange.getFrom(), dateRange.getTo(), request));
	}
}
