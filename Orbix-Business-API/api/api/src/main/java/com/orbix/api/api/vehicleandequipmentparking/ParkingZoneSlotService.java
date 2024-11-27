package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;

public interface ParkingZoneSlotService {
	List<ParkingZoneSlotResponseDTO> getAllParkingZoneSlots(HttpServletRequest request);	
	ParkingZoneSlotResponseDTO get(Long id, HttpServletRequest request);
	ParkingZoneSlotResponseDTO createParkingZoneSlot(ParkingZoneSlotRequestDTO parkingZoneSlotRequest, HttpServletRequest request);
	ParkingZoneSlotResponseDTO updateParkingZoneSlot(ParkingZoneSlotRequestDTO parkingZoneSlotRequest, HttpServletRequest request);
	ApiCustomResponse activateParkingZoneSlot(ParkingZoneSlotRequestDTO parkingZoneSlotRequest, HttpServletRequest request);
	ApiCustomResponse deactivateParkingZoneSlot(ParkingZoneSlotRequestDTO parkingZoneSlotRequest, HttpServletRequest request);

}
