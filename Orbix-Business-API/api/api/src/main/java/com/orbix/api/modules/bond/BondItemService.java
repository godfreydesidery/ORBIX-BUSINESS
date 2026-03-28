package com.orbix.api.modules.bond;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.vehicleandequipmentparking.MonthlyParkingStatusResponseDTO;
import com.orbix.api.api.vehicleandequipmentparking.ParkingResponseDTO;



public interface BondItemService {
	List<BondItemResponseDTO> getAllBondItems(HttpServletRequest request);	
	List<BondItemResponseDTO> getAllPendingOrCheckedInBondItems(HttpServletRequest request);
	List<BondItemResponseDTO> getAllPendingOrCheckedInBondItemsByBondZone(Long bondZoneId, HttpServletRequest request);
	List<BondItemResponseDTO> getAllWithDiscounts(HttpServletRequest request);
	List<BondItemResponseDTO> getAllCheckedInBondItemsByBondZone(Long bondZoneId, HttpServletRequest request);
	List<BondItemResponseDTO> getAllRecentCheckedOutBondItemsByBondZone(Long bondZoneId, HttpServletRequest request);
	List<BondItemResponseDTO> getAllCleared(HttpServletRequest request);
	List<BondItemResponseDTO> getTodayCheckedOut(HttpServletRequest request);
	List<BondItemResponseDTO> getAllCheckedInBondItems(HttpServletRequest request);	
	BondItemResponseDTO get(Long id, HttpServletRequest request);
	List<BondItemBillReceivableResponseDTO> getBondItemBillReceivables(Long id, HttpServletRequest request);
	BondItemResponseDTO createBondItem(BondItemRequestDTO bondItemRequest, HttpServletRequest request);
	BondItemResponseDTO updateBondItem(BondItemRequestDTO bondItemRequest, HttpServletRequest request);
	
	BondItemResponseDTO checkIn(BondItemRequestDTO bondItemRequest, HttpServletRequest request);
	BondItemResponseDTO checkOut(BondItemRequestDTO bondItemRequest, HttpServletRequest request);

	BondItemBillReceivableResponseDTO createBondItemBillReceivable(Long bondItemId, LocalDateTime startedAt, LocalDateTime endedAt, String billingType, double qty, double price, double discount, int autoBilling, HttpServletRequest request);

	BondItemCustomBillDetail showBondItemCustomBillDetail(Long bondItemId, HttpServletRequest request);
	
	List<MonthlyBondItemStatusResponseDTO> getMonthlyStats(int year, HttpServletRequest request);
	
}
