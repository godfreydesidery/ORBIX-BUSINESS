package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.PageResponseDTO;

public interface ParkingTripService {
	ParkingTripResponseDTO sendOut(ParkingTripRequestDTO tripRequest, HttpServletRequest request);
	ParkingTripResponseDTO sendIn(ParkingTripRequestDTO tripRequest, HttpServletRequest request);
	ParkingTripResponseDTO get(Long id, HttpServletRequest request);
	ParkingTripResponseDTO getOpenTrip(Long parkingId, HttpServletRequest request);
	List<ParkingTripResponseDTO> getTripsByParking(Long parkingId, HttpServletRequest request);
	PageResponseDTO<ParkingTripResponseDTO> getOutsidePage(int page, int size, String search, HttpServletRequest request);
	List<ParkingTripResponseDTO> getReport(LocalDate from, LocalDate to, HttpServletRequest request);
}
