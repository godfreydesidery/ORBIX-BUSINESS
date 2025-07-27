package com.orbix.api.modules.weighbridge;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface WeighService {
	List<WeighResponseDTO> getAllWeighs(HttpServletRequest request);	
	List<WeighResponseDTO> getAllPendingOrCheckedInWeighs(HttpServletRequest request);
	List<WeighResponseDTO> getAllPendingOrCheckedInWeighsByWarehouse(Long warehouseId, HttpServletRequest request);
	List<WeighResponseDTO> getAllWithDiscounts(HttpServletRequest request);
	List<WeighResponseDTO> getAllCheckedInWeighsByWarehouse(Long warehouseId, HttpServletRequest request);
	List<WeighResponseDTO> getAllRecentCheckedOutWeighsByWarehouse(Long warehouseId, HttpServletRequest request);
	List<WeighResponseDTO> getAllCleared(HttpServletRequest request);
	List<WeighResponseDTO> getTodayCheckedOut(HttpServletRequest request);
	List<WeighResponseDTO> getAllCheckedInWeighs(HttpServletRequest request);	
	WeighResponseDTO get(Long id, HttpServletRequest request);
	List<WeighBillReceivableResponseDTO> getWeighBillReceivables(Long id, HttpServletRequest request);
	WeighResponseDTO createWeigh(WeighRequestDTO weighRequest, HttpServletRequest request);
	WeighResponseDTO updateWeigh(WeighRequestDTO weighRequest, HttpServletRequest request);
	
	WeighResponseDTO checkIn(WeighRequestDTO weighRequest, HttpServletRequest request);
	WeighResponseDTO checkOut(WeighRequestDTO weighRequest, HttpServletRequest request);
	
	WeighBillReceivableResponseDTO createWeighBillReceivable(Long weighId, LocalDateTime startedAt, LocalDateTime endedAt, String billingType, double qty, double price, double discount, int autoBilling, HttpServletRequest request);

//	WeighCustomBillDetail showWeighCustomBillDetail(Long weighId, HttpServletRequest request);
	
//	List<MonthlyWeighStatusResponseDTO> getMonthlyStats(int year, HttpServletRequest request);
}
