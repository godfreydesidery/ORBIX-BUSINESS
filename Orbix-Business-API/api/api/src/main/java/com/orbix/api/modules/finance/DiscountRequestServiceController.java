package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRepository;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.bond.BondItem;
import com.orbix.api.modules.bond.BondItemBillReceivable;
import com.orbix.api.modules.bond.BondItemBillReceivableRepository;
import com.orbix.api.modules.bond.BondItemRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.warehouse.Storage;
import com.orbix.api.modules.warehouse.StorageBillReceivable;
import com.orbix.api.modules.warehouse.StorageBillReceivableRepository;
import com.orbix.api.modules.warehouse.StorageRepository;

import lombok.Data;
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
	private final BondItemBillReceivableRepository bondItemBillReceivableRepository;
	private final ParkingRepository parkingRepository;
	private final StorageRepository storageRepository;
	private final BondItemRepository bondItemRepository;
	private final BillReceivableRepository billReceivableRepository;
	
	@Override
	public List<DiscountRequestResponseDTO> getRequests(Long serviceId, String serviceName, HttpServletRequest request) {
		List<Long> ids = new ArrayList<>();
		if(serviceName.equals("Parking")) {
			Optional<Parking> parking_ = parkingRepository.findById(serviceId);
			List<ParkingBillReceivable> pbrs = parkingBillReceivableRepository.findByParking(parking_.get());
			for(ParkingBillReceivable pbr : pbrs) {
				ids.add(pbr.getId());
			}
		}
		if(serviceName.equals("Storage")) {
			Optional<Storage> storage_ = storageRepository.findById(serviceId);
			List<StorageBillReceivable> sbrs = storageBillReceivableRepository.findByStorage(storage_.get());
			for(StorageBillReceivable sbr : sbrs) {
				ids.add(sbr.getId());
			}
		}
		if(serviceName.equals("Bond")) {
			Optional<BondItem> bondItem_ = bondItemRepository.findById(serviceId);
			List<BondItemBillReceivable> sbrs = bondItemBillReceivableRepository.findByBondItem(bondItem_.get());
			for(BondItemBillReceivable sbr : sbrs) {
				ids.add(sbr.getId());
			}
		}
		List<DiscountRequestResponseDTO> responses = new ArrayList<>();		
		List<DiscountRequest> requests = discountRequestRepository.findAllByServiceBillIdInAndServiceBillName(ids, serviceName);	
		for(DiscountRequest req : requests) {
			if(req.getStatus().toString().equals("PENDING")) {
				DiscountRequestResponseDTO requestDTO = discountRequestDTOMapper(req);		
				responses.add(requestDTO);
			} else if (
				    (req.getApprovedDateTime() != null && req.getApprovedDateTime().isAfter(LocalDateTime.now().minusHours(48))) ||
				    (req.getRejectedDateTime() != null && req.getRejectedDateTime().isAfter(LocalDateTime.now().minusHours(48)))
				) {
				    // Approved or rejected within last 48 hours
				    DiscountRequestResponseDTO requestDTO = discountRequestDTOMapper(req);
				    responses.add(requestDTO);
				}		
		}	
		return responses;
	}
	
	@Override
	public DiscountRequestResponseDTO get(Long id,
			HttpServletRequest request) {
		Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findById(id);
		
		if(!discountRequest_.isEmpty()) {
			return discountRequestDTOMapper(discountRequest_.get());
		}else {
			throw new NotFoundException("Discount Request not found");
		}
		
	}
	
	@Override
	public DiscountRequestResponseDTO getDiscount(Long serviceBillId, double billAmount, double discountAmount, String serviceBillName,
			HttpServletRequest request) {
		Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findByServiceBillIdAndServiceBillName(serviceBillId, serviceBillName);
		
		DiscountRequestResponseDTO response = new DiscountRequestResponseDTO();
		
		if(!discountRequest_.isEmpty()) {
			return discountRequestDTOMapper(discountRequest_.get());
		}
		response.setDiscountAmount("0");
		response.setReason("");
		return response;
	}

	@Override
	public DiscountRequestResponseDTO createDiscountRequest(DiscountRequestRequestDTO dRequest, Long serviceBillId, double billAmount1,
			double discountAmount1, String serviceBillName1, HttpServletRequest request) {
		if(dRequest.getDiscountAmount() <= 0) {
			throw new InvalidOperationException("Less than zero not allowed");
		}
		if(dRequest.getDiscountAmount() > dRequest.getBillAmount()) throw new InvalidOperationException("Discount can not be more than bill amount");
		
		DiscountRequest discountRequest = null;
		
		if(dRequest.getServiceBillName().equals("Parking")) {
			Optional<ParkingBillReceivable> parkingBillReceivable_ = parkingBillReceivableRepository.findById(dRequest.getServiceBillId());
			if(parkingBillReceivable_.isEmpty()) throw new NotFoundException("Bill not found");
			
			Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findByServiceBillIdAndServiceBillName(serviceBillId, dRequest.getServiceBillName());
			if(!discountRequest_.isEmpty()) {
				discountRequest = discountRequest_.get();
				if(discountRequest.getStatus().toString().equals("REJECTED")) {
					discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
					discountRequest.setReason(dRequest.getReason());
					discountRequest.setStatus(WorkFlowStatus.PENDING);
					discountRequest.setCreatedByUser(userService.getUser(request));
					discountRequest.setCreatedDateTime(dayService.getTimeStamp());
					discountRequest.setRejectedByUser(null);
					discountRequest.setRejectedDateTime(null);
					discountRequest = discountRequestRepository.save(discountRequest);
					
					ParkingBillReceivable p = parkingBillReceivable_.get();
					p.setDiscountStatus("Requested");
					parkingBillReceivableRepository.save(p);
					
				}else if(discountRequest.getStatus().toString().equals("PENDING")) {
					discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
					discountRequest.setReason(dRequest.getReason());
					discountRequest = discountRequestRepository.save(discountRequest);
				}else {
					throw new InvalidOperationException("Must be pending or Rejected");
				}
				
			}else {
				discountRequest = new DiscountRequest();
				discountRequest.setServiceBillId(serviceBillId);
				discountRequest.setBillAmount(dRequest.getBillAmount());
				discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
				discountRequest.setReason(dRequest.getReason());
				discountRequest.setServiceBillName(dRequest.getServiceBillName());
				discountRequest.setDescription("Parking Bill " + parkingBillReceivable_.get().getStartedAt() + " " + parkingBillReceivable_.get().getEndedAt());
				discountRequest.setStatus(WorkFlowStatus.PENDING);
				discountRequest.setCreatedByUser(userService.getUser(request));
				discountRequest.setCreatedDateTime(dayService.getTimeStamp());
				discountRequest.setBranch(userService.getUserBranch(request));
				discountRequest = discountRequestRepository.save(discountRequest);
				
				ParkingBillReceivable p = parkingBillReceivable_.get();
				p.setDiscountStatus("Requested");
				parkingBillReceivableRepository.save(p);
			}
		}else if(dRequest.getServiceBillName().equals("Storage")) {
			Optional<StorageBillReceivable> storageBillReceivable_ = storageBillReceivableRepository.findById(serviceBillId);
			if(storageBillReceivable_.isEmpty()) {
				throw new NotFoundException("Bill not found");
			}
			Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findByServiceBillIdAndServiceBillName(serviceBillId, dRequest.getServiceBillName());
			if(!discountRequest_.isEmpty()) {
				discountRequest = discountRequest_.get();
				if(discountRequest.getStatus().toString().equals("REJECTED")) {
					discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
					discountRequest.setReason(dRequest.getReason());
					discountRequest.setStatus(WorkFlowStatus.PENDING);
					discountRequest.setCreatedByUser(userService.getUser(request));
					discountRequest.setCreatedDateTime(dayService.getTimeStamp());
					discountRequest.setRejectedByUser(null);
					discountRequest.setRejectedDateTime(null);
					discountRequest = discountRequestRepository.save(discountRequest);
					
					StorageBillReceivable s = storageBillReceivable_.get();
					s.setDiscountStatus("Requested");
					storageBillReceivableRepository.save(s);
					
				}else if(discountRequest.getStatus().toString().equals("PENDING")) {
					discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
					discountRequest.setReason(dRequest.getReason());
					discountRequest = discountRequestRepository.save(discountRequest);
				}else {
					throw new InvalidOperationException("Must be pending or Rejected");
				}
				
			}else {
				discountRequest = new DiscountRequest();
				discountRequest.setServiceBillId(serviceBillId);
				discountRequest.setBillAmount(dRequest.getBillAmount());
				discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
				discountRequest.setReason(dRequest.getReason());
				discountRequest.setServiceBillName(dRequest.getServiceBillName());
				discountRequest.setDescription("Storage Bill " + storageBillReceivable_.get().getStartedAt() + " " + storageBillReceivable_.get().getEndedAt());
				discountRequest.setStatus(WorkFlowStatus.PENDING);
				discountRequest.setCreatedByUser(userService.getUser(request));
				discountRequest.setCreatedDateTime(dayService.getTimeStamp());
				discountRequest.setBranch(userService.getUserBranch(request));
				discountRequest = discountRequestRepository.save(discountRequest);
				
				StorageBillReceivable s = storageBillReceivable_.get();
				s.setDiscountStatus("Requested");
				storageBillReceivableRepository.save(s);
			}
		}else if(dRequest.getServiceBillName().equals("Bond")) {
			Optional<BondItemBillReceivable> bondItemBillReceivable_ = bondItemBillReceivableRepository.findById(serviceBillId);
			if(bondItemBillReceivable_.isEmpty()) {
				throw new NotFoundException("Bill not found");
			}
			Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findByServiceBillIdAndServiceBillName(serviceBillId, dRequest.getServiceBillName());
			if(!discountRequest_.isEmpty()) {
				discountRequest = discountRequest_.get();
				if(discountRequest.getStatus().toString().equals("REJECTED")) {
					discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
					discountRequest.setReason(dRequest.getReason());
					discountRequest.setStatus(WorkFlowStatus.PENDING);
					discountRequest.setCreatedByUser(userService.getUser(request));
					discountRequest.setCreatedDateTime(dayService.getTimeStamp());
					discountRequest.setRejectedByUser(null);
					discountRequest.setRejectedDateTime(null);
					discountRequest = discountRequestRepository.save(discountRequest);
					
					BondItemBillReceivable s = bondItemBillReceivable_.get();
					s.setDiscountStatus("Requested");
					bondItemBillReceivableRepository.save(s);
					
				}else if(discountRequest.getStatus().toString().equals("PENDING")) {
					discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
					discountRequest.setReason(dRequest.getReason());
					discountRequest = discountRequestRepository.save(discountRequest);
				}else {
					throw new InvalidOperationException("Must be pending or Rejected");
				}
				
			}else {
				discountRequest = new DiscountRequest();
				discountRequest.setServiceBillId(serviceBillId);
				discountRequest.setBillAmount(dRequest.getBillAmount());
				discountRequest.setDiscountAmount(dRequest.getDiscountAmount());
				discountRequest.setReason(dRequest.getReason());
				discountRequest.setServiceBillName(dRequest.getServiceBillName());
				discountRequest.setDescription("Bond Bill " + bondItemBillReceivable_.get().getStartedAt() + " " + bondItemBillReceivable_.get().getEndedAt());
				discountRequest.setStatus(WorkFlowStatus.PENDING);
				discountRequest.setCreatedByUser(userService.getUser(request));
				discountRequest.setCreatedDateTime(dayService.getTimeStamp());
				discountRequest.setBranch(userService.getUserBranch(request));
				discountRequest = discountRequestRepository.save(discountRequest);
				
				BondItemBillReceivable s = bondItemBillReceivable_.get();
				s.setDiscountStatus("Requested");
				bondItemBillReceivableRepository.save(s);
			}
		}else {
			throw new InvalidOperationException("Invalid Service selected");
		}
		
		return discountRequestDTOMapper(discountRequest);
	}

	@Override
	public boolean approve(DiscountRequestRequestDTO discountRequestDTO, HttpServletRequest request) {
		Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findById(discountRequestDTO.getId());
		
		if(!discountRequest_.get().getStatus().toString().equals("PENDING")) {
			throw new InvalidOperationException("Not a pending request");
		}
		
		DiscountRequest discountRequest = discountRequest_.get();
		if(discountRequest.getServiceBillName().equals("Parking")) {			
			ParkingBillReceivable parkingBillReceivable = parkingBillReceivableRepository
				    .findById(discountRequest.getServiceBillId())
				    .orElseThrow(() -> new NotFoundException("Bill not found"));
			parkingBillReceivable.setDiscount(discountRequest.getDiscountAmount());
			parkingBillReceivable.setDiscountApprovedByUser(userService.getUser(request));
			parkingBillReceivable.setDiscountApprovedDateTime(dayService.getTimeStamp());
			parkingBillReceivable.setDiscountStatus("Approved");
			parkingBillReceivable = parkingBillReceivableRepository.save(parkingBillReceivable);
			discountRequest.setStatus(WorkFlowStatus.APPROVED);
			discountRequest.setApprovedByUser(userService.getUser(request));
			discountRequest.setApprovedDateTime(dayService.getTimeStamp());
			discountRequest.setComments(discountRequestDTO.getComments());
			discountRequestRepository.save(discountRequest);
			
			BillReceivable billReceivable = parkingBillReceivable.getBillReceivable();
			
			if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be edited");
			
			billReceivable.setAmount(parkingBillReceivable.getPrice() * parkingBillReceivable.getQty() - discountRequest.getDiscountAmount());
			billReceivable.setDue(parkingBillReceivable.getPrice() * parkingBillReceivable.getQty() - discountRequest.getDiscountAmount());
			billReceivable = billReceivableRepository.save(billReceivable);
		}else if(discountRequest.getServiceBillName().equals("Storage")) {
			StorageBillReceivable storageBillReceivable = storageBillReceivableRepository
				    .findById(discountRequest.getServiceBillId())
				    .orElseThrow(() -> new NotFoundException("Bill not found"));
			storageBillReceivable.setDiscount(discountRequest.getDiscountAmount());
			storageBillReceivable.setDiscountApprovedByUser(userService.getUser(request));
			storageBillReceivable.setDiscountApprovedDateTime(dayService.getTimeStamp());
			storageBillReceivable.setDiscountStatus("Approved");
			storageBillReceivable = storageBillReceivableRepository.save(storageBillReceivable);
			discountRequest.setStatus(WorkFlowStatus.APPROVED);
			discountRequest.setApprovedByUser(userService.getUser(request));
			discountRequest.setApprovedDateTime(dayService.getTimeStamp());
			discountRequest.setComments(discountRequestDTO.getComments());
			discountRequestRepository.save(discountRequest);
			
			BillReceivable billReceivable = storageBillReceivable.getBillReceivable();
			
			if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be edited");
			
			billReceivable.setAmount(storageBillReceivable.getPrice() * storageBillReceivable.getQty() * storageBillReceivable.getNoOfDays() - discountRequest.getDiscountAmount());
			billReceivable.setDue(storageBillReceivable.getPrice() * storageBillReceivable.getQty() * storageBillReceivable.getNoOfDays() - discountRequest.getDiscountAmount());
			billReceivable = billReceivableRepository.save(billReceivable);
		}
		
		return true;
	}
	
	@Override
	public boolean reject(DiscountRequestRequestDTO discountRequestDTO, HttpServletRequest request) {
		Optional<DiscountRequest> discountRequest_ = discountRequestRepository.findById(discountRequestDTO.getId());
		
		if(!discountRequest_.get().getStatus().toString().equals("PENDING")) {
			throw new InvalidOperationException("Not a pending request");
		}
		
		DiscountRequest discountRequest = discountRequest_.get();
		if(discountRequest.getServiceBillName().equals("Parking")) {			
			ParkingBillReceivable parkingBillReceivable = parkingBillReceivableRepository
				    .findById(discountRequest.getServiceBillId())
				    .orElseThrow(() -> new NotFoundException("Bill not found"));
			parkingBillReceivable.setDiscountStatus("Rejected");
			parkingBillReceivableRepository.save(parkingBillReceivable);
			discountRequest.setStatus(WorkFlowStatus.REJECTED);
			discountRequest.setRejectedByUser(userService.getUser(request));
			discountRequest.setRejectedDateTime(dayService.getTimeStamp());
			discountRequest.setComments(discountRequestDTO.getComments());
			discountRequestRepository.save(discountRequest);
		}else if(discountRequest.getServiceBillName().equals("Storage")) {
			StorageBillReceivable storageBillReceivable = storageBillReceivableRepository
				    .findById(discountRequest.getServiceBillId())
				    .orElseThrow(() -> new NotFoundException("Bill not found"));
			storageBillReceivable.setDiscountStatus("Rejected");
			storageBillReceivableRepository.save(storageBillReceivable);
			discountRequest.setStatus(WorkFlowStatus.REJECTED);
			discountRequest.setRejectedByUser(userService.getUser(request));
			discountRequest.setRejectedDateTime(dayService.getTimeStamp());
			discountRequest.setComments(discountRequestDTO.getComments());
			discountRequestRepository.save(discountRequest);
		}
		return true;
	}

	private DiscountRequestResponseDTO discountRequestDTOMapper(DiscountRequest discountRequest) {
		DiscountRequestResponseDTO response = new DiscountRequestResponseDTO();
		response.setId(discountRequest.getId().toString());
		response.setBillAmount(String.valueOf(discountRequest.getBillAmount()));
		response.setDiscountAmount(String.valueOf(discountRequest.getDiscountAmount()));
		response.setReason(discountRequest.getReason());
		response.setApproved("");
		response.setBranch("");
		response.setServiceBillId("");
		response.setServiceBillName(discountRequest.getServiceBillName());
		response.setStatus(discountRequest.getStatus().toString());
		response.setCreated("");
		response.setDescription(discountRequest.getDescription());
		response.setComments(discountRequest.getComments());
		return response;
	}
}
