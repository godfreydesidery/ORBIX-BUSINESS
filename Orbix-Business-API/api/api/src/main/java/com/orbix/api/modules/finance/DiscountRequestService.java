package com.orbix.api.modules.finance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface DiscountRequestService {
	List<DiscountRequestResponseDTO> getRequests(Long serviceId, String serviceName, HttpServletRequest request);
	DiscountRequestResponseDTO get(Long id, HttpServletRequest request);
	DiscountRequestResponseDTO getDiscount(Long serviceBillId, double billAmount, double discountAmount, String serviceBillName, HttpServletRequest request);
	DiscountRequestResponseDTO createDiscountRequest(DiscountRequestRequestDTO discountRequest, Long serviceBillId, double billAmount, double discountAmount, String serviceBillName, HttpServletRequest request);	
	boolean approve(DiscountRequestRequestDTO discountRequestDTO, HttpServletRequest request);
	boolean reject(DiscountRequestRequestDTO discountRequestDTO, HttpServletRequest request);
}
