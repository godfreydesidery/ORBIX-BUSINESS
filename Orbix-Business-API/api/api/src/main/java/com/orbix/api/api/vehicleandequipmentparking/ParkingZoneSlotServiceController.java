package com.orbix.api.api.vehicleandequipmentparking;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.SystemProfileRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ParkingZoneSlotServiceController implements ParkingZoneSlotService {
	private final ParkingZoneSlotRepository parkingZoneSlotRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final ParkingZoneRepository parkingZoneRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public List<ParkingZoneSlotResponseDTO> getAllParkingZoneSlots(HttpServletRequest request) {
		List<ParkingZoneSlot> parkingZoneSlots = parkingZoneSlotRepository.findAll();
		List<ParkingZoneSlotResponseDTO> parkingZoneSlotResponses = new ArrayList<>();

		for(ParkingZoneSlot parkingZoneSlot : parkingZoneSlots) {
			//parkingZoneSlotResponses.add(parkingZoneSlotResponseDTOMapper(parkingZoneSlot));					
		}		
		return parkingZoneSlotResponses;
	}

	@Override
	public ParkingZoneSlotResponseDTO get(Long id, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParkingZoneSlotResponseDTO createParkingZoneSlot(ParkingZoneSlotRequestDTO parkingZoneSlotRequest,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParkingZoneSlotResponseDTO updateParkingZoneSlot(ParkingZoneSlotRequestDTO parkingZoneSlotRequest,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiCustomResponse activateParkingZoneSlot(ParkingZoneSlotRequestDTO parkingZoneSlotRequest,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiCustomResponse deactivateParkingZoneSlot(ParkingZoneSlotRequestDTO parkingZoneSlotRequest,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
