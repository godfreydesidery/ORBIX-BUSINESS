package com.orbix.api.modules.finance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface DiscountRequestService {
	List<DiscountRequestResponseDTO> getRequests(HttpServletRequest request);
	double getDiscount(Long serviceBillId, double billAmount, double discountAmount, String serviceBillName, HttpServletRequest request);
	DiscountRequestResponseDTO createDiscountRequest(Long serviceBillId, double billAmount, double discountAmount, String serviceBillName, HttpServletRequest request);	
	boolean approveDiscountRequest(Long id, HttpServletRequest request);
}
