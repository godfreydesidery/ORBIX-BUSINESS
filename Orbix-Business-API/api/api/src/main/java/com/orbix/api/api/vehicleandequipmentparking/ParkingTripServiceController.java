package com.orbix.api.api.vehicleandequipmentparking;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PageRequests;
import com.orbix.api.api.commons.PageResponseDTO;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ParkingTripServiceController implements ParkingTripService {

	private static final Set<String> REASONS = Set.of("SERVICE_REPAIR", "TESTING", "LOADING", "OTHER");

	private final ParkingRepository parkingRepository;
	private final ParkingTripRepository parkingTripRepository;
	private final UserService userService;
	private final DayService dayService;

	@Override
	public ParkingTripResponseDTO sendOut(ParkingTripRequestDTO tripRequest, HttpServletRequest request) {
		// Locked, so two cashiers cannot send the same vehicle out at once
		Parking parking = parkingRepository.findByIdForUpdate(tripRequest.getParkingId())
				.orElseThrow(() -> new NotFoundException("Parking not found"));
		User user = userService.getUser(request);
		checkBranch(parking, user);

		if(!"CHECKED-IN".equals(parking.getStatus()))
			throw new InvalidOperationException("Only a checked in vehicle can be sent out");
		if(parking.isOutside())
			throw new InvalidOperationException("The vehicle is already outside the yard");

		String reason = text(tripRequest.getReason()).toUpperCase();
		if(!REASONS.contains(reason)) throw new InvalidEntryException("Select the reason for sending the vehicle out");
		String driverName = required(tripRequest.getDriverName(), "Driver name is required");
		String driverPhoneNo = required(tripRequest.getDriverPhoneNo(), "Driver phone number is required");
		String driverIdType = required(tripRequest.getDriverIdType(), "Driver ID type is required");
		String driverIdNo = required(tripRequest.getDriverIdNo(), "Driver ID number is required");

		LocalDateTime now = dayService.getTimeStamp();
		LocalDateTime expectedReturnAt;
		try {
			expectedReturnAt = LocalDateTime.parse(required(tripRequest.getExpectedReturnAt(), "Expected return date and time is required"));
		}catch(DateTimeParseException e) {
			throw new InvalidEntryException("Invalid expected return date and time");
		}
		if(!expectedReturnAt.isAfter(now)) throw new InvalidEntryException("Expected return must be later than now");

		ParkingTrip trip = new ParkingTrip();
		trip.setNo(String.valueOf(Math.random()));
		trip.setParking(parking);
		trip.setBranch(parking.getBranch());
		trip.setReason(reason);
		trip.setReasonDetails(text(tripRequest.getReasonDetails()));
		trip.setDriverName(driverName);
		trip.setDriverPhoneNo(driverPhoneNo);
		trip.setDriverIdType(driverIdType);
		trip.setDriverIdNo(driverIdNo);
		trip.setDocumentsLeft(tripRequest.isDocumentsLeft());
		trip.setExpectedReturnAt(expectedReturnAt);
		trip.setStatus(ParkingTrip.OUT);
		trip.setSentOutByUser(user);
		trip.setSentOutAt(now);
		trip = parkingTripRepository.save(trip);
		/** Gate pass number */
		trip.setNo("TGP/" + trip.getId());
		trip = parkingTripRepository.save(trip);

		parking.setCustody(Parking.OUTSIDE);
		parkingRepository.save(parking);

		return map(trip, now);
	}

	@Override
	public ParkingTripResponseDTO sendIn(ParkingTripRequestDTO tripRequest, HttpServletRequest request) {
		Parking parking = parkingRepository.findByIdForUpdate(tripRequest.getParkingId())
				.orElseThrow(() -> new NotFoundException("Parking not found"));
		User user = userService.getUser(request);
		checkBranch(parking, user);

		if(!parking.isOutside()) throw new InvalidOperationException("The vehicle is not outside the yard");

		ParkingTrip trip = parkingTripRepository.findFirstByParkingAndStatusOrderByIdDesc(parking, ParkingTrip.OUT)
				.orElseThrow(() -> new NotFoundException("No open trip found for this vehicle"));

		LocalDateTime now = dayService.getTimeStamp();
		trip.setStatus(ParkingTrip.RETURNED);
		trip.setReceivedByUser(user);
		trip.setReceivedAt(now);
		trip.setDocumentsReturned(trip.isDocumentsLeft() && tripRequest.isDocumentsReturned());
		trip.setReturnNotes(text(tripRequest.getReturnNotes()));
		trip = parkingTripRepository.save(trip);

		parking.setCustody(Parking.INSIDE);
		parkingRepository.save(parking);

		return map(trip, now);
	}

	@Override
	public ParkingTripResponseDTO get(Long id, HttpServletRequest request) {
		ParkingTrip trip = parkingTripRepository.findById(id).orElseThrow(() -> new NotFoundException("Trip not found"));
		return map(trip, dayService.getTimeStamp());
	}

	@Override
	public ParkingTripResponseDTO getOpenTrip(Long parkingId, HttpServletRequest request) {
		Parking parking = parkingRepository.findById(parkingId).orElseThrow(() -> new NotFoundException("Parking not found"));
		ParkingTrip trip = parkingTripRepository.findFirstByParkingAndStatusOrderByIdDesc(parking, ParkingTrip.OUT)
				.orElseThrow(() -> new NotFoundException("The vehicle is not outside the yard"));
		return map(trip, dayService.getTimeStamp());
	}

	@Override
	public List<ParkingTripResponseDTO> getTripsByParking(Long parkingId, HttpServletRequest request) {
		Parking parking = parkingRepository.findById(parkingId).orElseThrow(() -> new NotFoundException("Parking not found"));
		return mapAll(parkingTripRepository.findAllByParkingOrderByIdDesc(parking), 1);
	}

	@Override
	public PageResponseDTO<ParkingTripResponseDTO> getOutsidePage(int page, int size, String search, HttpServletRequest request) {
		// Longest away first
		Page<ParkingTrip> trips = parkingTripRepository.getPageByStatus(ParkingTrip.OUT, PageRequests.searchPattern(search),
				PageRequests.of(page, size, Sort.by(Sort.Direction.ASC, "expectedReturnAt")));
		int sn = trips.getNumber() * trips.getSize() + 1;
		return new PageResponseDTO<>(mapAll(trips.getContent(), sn), trips.getTotalElements());
	}

	@Override
	public List<ParkingTripResponseDTO> getReport(LocalDate from, LocalDate to, HttpServletRequest request) {
		if(from == null || to == null) throw new InvalidEntryException("Select the period");
		return mapAll(parkingTripRepository.getReport(from.atStartOfDay(), to.atTime(LocalTime.MAX)), 1);
	}

	private List<ParkingTripResponseDTO> mapAll(List<ParkingTrip> trips, int firstSn) {
		LocalDateTime now = dayService.getTimeStamp();
		List<ParkingTripResponseDTO> responses = new ArrayList<>();
		int sn = firstSn;
		for(ParkingTrip trip : trips) {
			ParkingTripResponseDTO response = map(trip, now);
			response.setSn(sn++);
			responses.add(response);
		}
		return responses;
	}

	private ParkingTripResponseDTO map(ParkingTrip trip, LocalDateTime now) {
		Parking parking = trip.getParking();
		ParkingTripResponseDTO response = new ParkingTripResponseDTO();
		response.setId(trip.getId().toString());
		response.setSn(1);
		response.setNo(trip.getNo());

		response.setParkingId(parking.getId().toString());
		response.setParkingNo(parking.getNo());
		response.setChasisNo(text(parking.getChasisNo()));
		response.setRegistrationNo(text(parking.getRegistrationNo()));
		response.setVehicleEquipmentTypeName(parking.getVehicleEquipmentType() != null ? parking.getVehicleEquipmentType().getName() : "");
		response.setVehicleEquipmentColor(text(parking.getVehicleEquipmentColor()));
		response.setCardNo(text(parking.getCardNo()));
		response.setOwnerName((text(parking.getOwnerFirstName()) + " " + text(parking.getOwnerLastName())).trim());
		response.setOwnerPhoneNo(text(parking.getOwnerPhoneNo()));

		response.setReason(trip.getReason());
		response.setReasonDetails(text(trip.getReasonDetails()));
		response.setDriverName(trip.getDriverName());
		response.setDriverPhoneNo(trip.getDriverPhoneNo());
		response.setDriverIdType(trip.getDriverIdType());
		response.setDriverIdNo(trip.getDriverIdNo());
		response.setDocumentsLeft(trip.isDocumentsLeft());
		response.setExpectedReturnAt(trip.getExpectedReturnAt().toString());

		response.setStatus(trip.getStatus());
		boolean out = ParkingTrip.OUT.equals(trip.getStatus());
		response.setOverdue(out && now.isAfter(trip.getExpectedReturnAt()));

		response.setSentOutAt(trip.getSentOutAt().toString());
		response.setSentOutBy(trip.getSentOutByUser() != null ? text(trip.getSentOutByUser().getNickname()) : "");
		response.setReceivedAt(trip.getReceivedAt() != null ? trip.getReceivedAt().toString() : "");
		response.setReceivedBy(trip.getReceivedByUser() != null ? text(trip.getReceivedByUser().getNickname()) : "");
		response.setDocumentsReturned(trip.isDocumentsReturned());
		response.setReturnNotes(text(trip.getReturnNotes()));

		LocalDateTime end = trip.getReceivedAt() != null ? trip.getReceivedAt() : now;
		response.setMinutesOut(Math.max(0, Duration.between(trip.getSentOutAt(), end).toMinutes()));
		return response;
	}

	private void checkBranch(Parking parking, User user) {
		if(user.getBranch() == null || !parking.getBranch().getId().equals(user.getBranch().getId()))
			throw new InvalidOperationException("Only a user of the vehicle's branch can do this");
	}

	private static String text(String value) {
		return value == null ? "" : value.trim();
	}

	private static String required(String value, String message) {
		String v = text(value);
		if(v.isEmpty()) throw new InvalidEntryException(message);
		return v;
	}
}
