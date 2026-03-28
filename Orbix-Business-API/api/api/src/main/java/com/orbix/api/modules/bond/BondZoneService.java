package com.orbix.api.modules.bond;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.modules.adminunits.ShopResponseDTO;

public interface BondZoneService {
	List<BondZoneResponseDTO> getAllBondZones(HttpServletRequest request);
	List<BondZoneResponseDTO> getAllBranchActiveBondZones(HttpServletRequest request);	
	BondZoneResponseDTO get(Long id, HttpServletRequest request);
	BondZoneResponseDTO createBondZone(BondZoneRequestDTO bondZoneRequest, HttpServletRequest request);
	BondZoneResponseDTO updateBondZone(BondZoneRequestDTO bondZoneRequest, HttpServletRequest request);
	ApiCustomResponse activateBondZone(BondZoneRequestDTO bondZoneRequest, HttpServletRequest request);
	ApiCustomResponse deactivateBondZone(BondZoneRequestDTO bondZoneRequest, HttpServletRequest request);
	
	List<BondZoneResponseDTO> getBranchAvailableBondZonesByUser(HttpServletRequest request);
}
