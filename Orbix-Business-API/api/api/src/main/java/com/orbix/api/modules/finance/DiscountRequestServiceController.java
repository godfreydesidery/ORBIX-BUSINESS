package com.orbix.api.modules.finance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivableRepository;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.warehouse.StorageBillReceivable;
import com.orbix.api.modules.warehouse.StorageBillReceivableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiscountRequestServiceController implements DiscountRequestService {
	
	private final DiscountRequestRepository discountRequestRepository;
	private final UserService userService;
	private final DayService dayService;	
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	private final StorageBillReceivableRepository storageBillReceivableRepository;
	
	@Override
	public List<DiscountRequestResponseDTO> getRequests(HttpServletRequest request) {		
		List<DiscountRequestResponseDTO> responses = new ArrayList<>();		
		List<DiscountRequest> requests = discountRequestRepository.findAll();	
		for(DiscountRequest req : requests) {
			DiscountRequestResponseDTO requestDTO = discountRequestDTOMapper(req);		
			responses.add(requestDTO);
		}	
		return responses;
	}
	
	@Override
	public double getDiscount(Long serviceBillId, double billAmount, double discountAmount, String serviceBillName,
			HttpServletRequest request) {
		Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findByServiceBillIdAndServiceBillName(serviceBillId, serviceBillName);
		
		if(!discountRequest_.isEmpty()) {
			return discountRequest_.get().getDiscountAmount();
		}
		return 0;
	}

	@Override
	public DiscountRequestResponseDTO createDiscountRequest(Long serviceBillId, double billAmount,
			double discountAmount, String serviceBillName, HttpServletRequest request) {
		
		if(discountAmount > billAmount) throw new InvalidOperationException("Discount can not be more than bill amount");
		
		DiscountRequest discountRequest = null;
		
		if(serviceBillName.equals("Parking")) {
			Optional<ParkingBillReceivable> parkingBillReceivable_ = parkingBillReceivableRepository.findById(serviceBillId);
			if(parkingBillReceivable_.isEmpty()) {
				throw new NotFoundException("Bill not found");
			}
			
			Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findByServiceBillIdAndServiceBillName(serviceBillId, serviceBillName);
			if(!discountRequest_.isEmpty()) {
				discountRequest = discountRequest_.get();
				if(!discountRequest.getStatus().toString().equals("PENDING")) throw new InvalidOperationException("Request is not pending");
				discountRequest.setDiscountAmount(discountAmount);
				discountRequest = discountRequestRepository.save(discountRequest);
			}else {
				discountRequest = new DiscountRequest();
				discountRequest.setServiceBillId(serviceBillId);
				discountRequest.setBillAmount(billAmount);
				discountRequest.setDiscountAmount(discountAmount);
				discountRequest.setServiceBillName(serviceBillName);
				discountRequest.setDescription("Parking Bill " + parkingBillReceivable_.get().getStartedAt() + " " + parkingBillReceivable_.get().getEndedAt());
				discountRequest.setStatus(WorkFlowStatus.PENDING);
				discountRequest.setCreatedByUser(userService.getUser(request));
				discountRequest.setCreatedDateTime(dayService.getTimeStamp());
				discountRequest.setBranch(userService.getUserBranch(request));
				discountRequest = discountRequestRepository.save(discountRequest);
			}
			
		}else if(serviceBillName.equals("Storage")) {
			Optional<StorageBillReceivable> storageBillReceivable_ = storageBillReceivableRepository.findById(serviceBillId);
			if(storageBillReceivable_.isEmpty()) {
				throw new NotFoundException("Bill not found");
			}
			Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findByServiceBillIdAndServiceBillName(serviceBillId, serviceBillName);
			if(!discountRequest_.isEmpty()) {
				discountRequest = discountRequest_.get();
				if(!discountRequest.getStatus().toString().equals("PENDING")) throw new InvalidOperationException("Request is not pending");
				discountRequest.setDiscountAmount(discountAmount);
				discountRequest = discountRequestRepository.save(discountRequest);
			}else {
				discountRequest = new DiscountRequest();
				discountRequest.setServiceBillId(serviceBillId);
				discountRequest.setBillAmount(billAmount);
				discountRequest.setDiscountAmount(discountAmount);
				discountRequest.setServiceBillName(serviceBillName);
				discountRequest.setDescription("Storage Bill " + storageBillReceivable_.get().getStartedAt() + " " + storageBillReceivable_.get().getEndedAt());
				discountRequest.setStatus(WorkFlowStatus.PENDING);
				discountRequest.setCreatedByUser(userService.getUser(request));
				discountRequest.setCreatedDateTime(dayService.getTimeStamp());
				discountRequest.setBranch(userService.getUserBranch(request));
				discountRequest = discountRequestRepository.save(discountRequest);
			}
		}else {
			throw new InvalidOperationException("Invalid Service selected");
		}
		
		return discountRequestDTOMapper(discountRequest);
	}

	@Override
	public boolean approveDiscountRequest(Long id, HttpServletRequest request) {
		Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findById(id);
		
		if(!discountRequest_.get().getStatus().toString().equals("PENDING")) {
			throw new InvalidOperationException("Not a pending request");
		}
		
		return true;
	}

	private DiscountRequestResponseDTO discountRequestDTOMapper(DiscountRequest discountRequest) {
		DiscountRequestResponseDTO response = new DiscountRequestResponseDTO();
		response.setId(discountRequest.getId().toString());
		response.setBillAmount(String.valueOf(discountRequest.getBillAmount()));
		response.setDiscountAmount(String.valueOf(discountRequest.getDiscountAmount()));
		response.setApproved("");
		response.setBranch("");
		response.setServiceBillId("");
		response.setServiceBillName("");
		response.setStatus("");
		response.setCreated("");
		return response;
	}

	
	

	
	

}
