package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface ParkingService {
	List<ParkingResponseDTO> getAllParkings(HttpServletRequest request);	
	List<ParkingResponseDTO> getAllPendingOrCheckedInParkings(HttpServletRequest request);	
	List<ParkingResponseDTO> getAllCleared(HttpServletRequest request);
	List<ParkingResponseDTO> getAllCheckedInParkings(HttpServletRequest request);	
	ParkingResponseDTO get(Long id, HttpServletRequest request);
	List<ParkingBillReceivableResponseDTO> getParkingBillReceivables(Long id, HttpServletRequest request);
	ParkingResponseDTO createParking(ParkingRequestDTO parkingRequest, HttpServletRequest request);
	ParkingResponseDTO updateParking(ParkingRequestDTO parkingRequest, HttpServletRequest request);
	
	ParkingResponseDTO checkIn(ParkingRequestDTO parkingRequest, HttpServletRequest request);
	ParkingResponseDTO checkOut(ParkingRequestDTO parkingRequest, HttpServletRequest request);
//	ApiCustomResponse activateParking(ParkingRequestDTO parkingRequest, HttpServletRequest request);
//	ApiCustomResponse deactivateParking(ParkingRequestDTO parkingRequest, HttpServletRequest request);
	
	ParkingBillReceivableResponseDTO createParkingBillReceivable(Long parkingId, LocalDateTime startedAt, LocalDateTime endedAt, String billingType, double qty, double price, double discount, int autoBilling, HttpServletRequest request);
}
