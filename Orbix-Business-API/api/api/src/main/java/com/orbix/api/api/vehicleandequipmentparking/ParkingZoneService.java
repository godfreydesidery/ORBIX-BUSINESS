package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;


public interface ParkingZoneService {
	List<ParkingZoneResponseDTO> getAllParkingZones(HttpServletRequest request);	
	ParkingZoneResponseDTO get(Long id, HttpServletRequest request);
	ParkingZoneResponseDTO createParkingZone(ParkingZoneRequestDTO parkingZoneRequest, HttpServletRequest request);
	ParkingZoneResponseDTO updateParkingZone(ParkingZoneRequestDTO parkingZoneRequest, HttpServletRequest request);
	ApiCustomResponse activateParkingZone(ParkingZoneRequestDTO parkingZoneRequest, HttpServletRequest request);
	ApiCustomResponse deactivateParkingZone(ParkingZoneRequestDTO parkingZoneRequest, HttpServletRequest request);
}
