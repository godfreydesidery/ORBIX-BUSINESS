package com.orbix.api.modules.warehouse;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StorageBillReceivableServiceController implements StorageBillReceivableService {
	
	private final UserService userService;
	
	private final StorageBillReceivableRepository storageBillReceivableRepository;
	private final StorageRepository storageRepository;
	private final BillReceivableRepository billReceivableRepository;
	private final StorageGoodReleaseRepository storageGoodReleaseRepository;
	private final DayService dayService;

	@Override
	public List<StorageBillReceivableResponseDTO> getAllByStorage(Long storageId, HttpServletRequest request) {
		Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new NotFoundException("Storage with ID " + storageId + " not found."));
		
		List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findAllByStorage(storage);
		
		List<StorageBillReceivableResponseDTO> storageBillReceivableResponses = new ArrayList<>();
		for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
			storageBillReceivableResponses.add(storageBillReceivableDTOMapper(storageBillReceivable));
		}
		
		return storageBillReceivableResponses;
	}
	
	@Override
	public StorageBillReceivableResponseDTO getStorageBillReceivable(Long id, HttpServletRequest request) {
		StorageBillReceivable storageBillReceivable = storageBillReceivableRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Storage bill with ID " + id + " not found."));
		
		return storageBillReceivableDTOMapper(storageBillReceivable);
	}
	
	@Override
	public StorageBillReceivableResponseDTO createStorageBillReceivable(
			StorageBillReceivableRequestDTO storageBillReceivableRequest,
			HttpServletRequest request) {
		
		Storage storage = storageRepository.findById(storageBillReceivableRequest.getStorageId())
                .orElseThrow(() -> new NotFoundException("Storage not found."));
		
		// if(!validateStorageBill(storageBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		// Check if is first bill
		
		List<StorageBillReceivable> rcvs = storageBillReceivableRepository.findAllByStorage(storage);
		
		LocalDateTime fromDate = null;
		LocalDateTime toDate = null;
		double qty = 0;
		
		if(storageBillReceivableRequest.getEndedAt() != null) {
			String dateString = storageBillReceivableRequest.getEndedAt() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			toDate = LocalDateTime.parse(dateString, formatter).plusDays(1).toLocalDate().atStartOfDay();
		}
		
		if(rcvs.isEmpty()) {			
			// Check for first billing date		
			fromDate = storage.getStartBillingAt().toLocalDate().atStartOfDay();
			
			if(toDate == null) toDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();	
			
			if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is before bill starting date");
			
			long dayCount = ChronoUnit.DAYS.between(fromDate, toDate);
			
			qty = dayCount;
			
		}else {
			// Take the last bill
			fromDate = rcvs.get(rcvs.size() - 1).getEndedAt().plusDays(1).toLocalDate().atStartOfDay();
			
			if(toDate == null) toDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
			
			if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is invalid " + toDate.toString() + fromDate.toString());
			
			long dayCount = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
			
			qty = dayCount;
			
		}
		
		if(qty > 1) qty = qty - 1;
			
		BillReceivable billReceivable = new BillReceivable();
		billReceivable.setNo(String.valueOf(Math.random()));
		billReceivable.setAmount((storage.getBillingAmount() * qty) - storageBillReceivableRequest.getDiscount());
		billReceivable.setPaid(0);
		billReceivable.setDue((storage.getBillingAmount() * qty) - storageBillReceivableRequest.getDiscount());
		billReceivable.setBranch(storage.getBranch());
		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
		
		billReceivable.setPayStatus(PayStatus.UNPAID);
		billReceivable.setSummary("Storage bill for storage#: " + storage.getNo());
		
		billReceivable = billReceivableRepository.save(billReceivable);
		billReceivable.setNo("BR" + billReceivable.getId().toString());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		StorageBillReceivable storageBillReceivable = new StorageBillReceivable();
		
		storageBillReceivable.setStartedAt(fromDate);
		storageBillReceivable.setEndedAt(toDate);
		
//		if(storageBillReceivableRequest.getStartedAt() == null) {
//			storageBillReceivable.setStartedAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this depending on user billing preferences
//		}else {
//			//String dateString = "2024-10-26 15:30:45" ;
//			String dateString = storageBillReceivableRequest.getStartedAt() + " 00:00:00";
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
//			storageBillReceivable.setStartedAt(dateTime);
//		}
		
//		if(storageBillReceivableRequest.getEndedAt() == null) {
//			storageBillReceivable.setEndedAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this depending on user billing preferences
//		}else {
//			//String dateString = "2024-10-26 15:30:45" ;
//			String dateString = storageBillReceivableRequest.getEndedAt() + " 00:00:00";
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
//			storageBillReceivable.setEndedAt(dateTime);
//		}
		
		storageBillReceivable.setPrice(storage.getBillingAmount());
		storageBillReceivable.setQty(qty);
		storageBillReceivable.setDiscount(storageBillReceivableRequest.getDiscount());
		storageBillReceivable.setBillReceivable(billReceivable);
		storageBillReceivable.setStorage(storage);
		
		storageBillReceivable = storageBillReceivableRepository.save(storageBillReceivable);

		return storageBillReceivableDTOMapper(storageBillReceivable);
	}
	
	@Override
	public StorageBillReceivableResponseDTO updateStorageBillReceivable(
			StorageBillReceivableRequestDTO storageBillReceivableRequest,
			HttpServletRequest request) {
		
		StorageBillReceivable storageBillReceivable = storageBillReceivableRepository.findById(storageBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
		
		if(!validateStorageBill(storageBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		BillReceivable billReceivable = storageBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be edited");
		
		billReceivable.setAmount(storageBillReceivableRequest.getPrice() * storageBillReceivableRequest.getQty() - storageBillReceivableRequest.getDiscount());
		billReceivable.setDue(storageBillReceivableRequest.getPrice() * storageBillReceivableRequest.getQty() - storageBillReceivableRequest.getDiscount());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		storageBillReceivable.setPrice(storageBillReceivableRequest.getPrice());
		storageBillReceivable.setQty(storageBillReceivableRequest.getQty());
		storageBillReceivable.setDiscount(storageBillReceivableRequest.getDiscount());
		storageBillReceivable = storageBillReceivableRepository.save(storageBillReceivable);
		
		return storageBillReceivableDTOMapper(storageBillReceivable);
	}
	
	@Override
	public StorageBillReceivableResponseDTO createStorageCustomBillReceivable(
			StorageBillReceivableRequestDTO storageBillReceivableRequest,
			HttpServletRequest request) {
		
		Storage storage = storageRepository.findById(storageBillReceivableRequest.getStorageId())
                .orElseThrow(() -> new NotFoundException("Storage not found."));
		
		LocalDateTime startBillingAt = storage.getStartBillingAt();
		double noOfDays = (long) Math.floor((double) Duration.between(startBillingAt, LocalDateTime.now()).toHours() / 24);
		if(noOfDays <=0 ) {
			noOfDays = 1;
		}
		if(storage.getBillingType().equals("FLAT-RATE")) {
			noOfDays = 1;
		}
				
		List<StorageBillReceivable> rcvs = storageBillReceivableRepository.findAllByStorage(storage);
		
		double billedQty = 0;
		
		for(StorageBillReceivable sbr : rcvs) {
			billedQty = billedQty + sbr.getQty();
		}
		
		List<StorageGoodRelease> sgrs = storageGoodReleaseRepository.findAllByStorage(storage);
		
		double releasedQty = 0;
		
		for(StorageGoodRelease sgr : sgrs) {
			releasedQty = releasedQty + sgr.getQty();
		}
		
		if(billedQty != releasedQty) {
			throw new InvalidOperationException("Can not process bill, billed items must be cleared and released first");
		}
		
		double qty = storageBillReceivableRequest.getQty();
		
		if(qty > (storage.getCurrentQty())) {
			throw new InvalidOperationException("Qty to be paid must not be more than available qty");
		}
		
		BillReceivable billReceivable = new BillReceivable();
		billReceivable.setNo(String.valueOf(Math.random()));
		billReceivable.setPaid(0);
		billReceivable.setQty(qty);
		if(storage.getBillingType().equals("DAILY")) {
			billReceivable.setAmount((storage.getBillingAmount() * qty * noOfDays) - storageBillReceivableRequest.getDiscount());
			billReceivable.setDue((storage.getBillingAmount() * qty * noOfDays) - storageBillReceivableRequest.getDiscount());
		}else if(storage.getBillingType().equals("FLAT-RATE")) {
			billReceivable.setAmount((storage.getBillingAmount() * qty) - storageBillReceivableRequest.getDiscount());
			billReceivable.setDue((storage.getBillingAmount() * qty) - storageBillReceivableRequest.getDiscount());
		}else {
			throw new InvalidOperationException("Invalid Billing Type");
		}
		
		billReceivable.setBranch(storage.getBranch());
		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
		
		billReceivable.setPayStatus(PayStatus.UNPAID);
		billReceivable.setSummary("Storage bill for storage#: " + storage.getNo());
		
		billReceivable = billReceivableRepository.save(billReceivable);
		billReceivable.setNo("BR" + billReceivable.getId().toString());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		StorageBillReceivable storageBillReceivable = new StorageBillReceivable();
		
		LocalDate today = LocalDate.now();
		storageBillReceivable.setStartedAt(today.atStartOfDay());
		storageBillReceivable.setEndedAt(today.atTime(23, 59, 59));
		
		storageBillReceivable.setPrice(storage.getBillingAmount());
		storageBillReceivable.setQty(qty);
		storageBillReceivable.setNoOfDays(noOfDays);
		storageBillReceivable.setDiscount(storageBillReceivableRequest.getDiscount());
		storageBillReceivable.setBillReceivable(billReceivable);
		storageBillReceivable.setStorage(storage);
		
		storageBillReceivable = storageBillReceivableRepository.save(storageBillReceivable);

		return storageBillReceivableDTOMapper(storageBillReceivable);
	}
	
	@Override
	public BillViewResponseDTO getBillView(Long storageId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		Optional<Storage> storage_ = storageRepository.findById(storageId);
		
		if(storage_.isEmpty()) {
			throw new NotFoundException("Storage not found");
		}
		
		BillViewResponseDTO billResponse = new BillViewResponseDTO();
		billResponse.setBillPaid("0");
		billResponse.setBillGenerated("0");
		billResponse.setBillUngenerated("0");
		billResponse.setBillUnpaid("0");
		
		//////////////////////
		
//		billResponse.setBillPaid("10000");
//		billResponse.setBillGenerated("10000");
//		billResponse.setBillUngenerated("10000");
//		billResponse.setBillUnpaid("10000");
		
		double totalPaid = 0;
		double totalGenerated = 0;
		double totalUngenerated = 0;
		
		List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findAllByStorage(storage_.get());
		for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
			if(storageBillReceivable.getBillReceivable().getPayStatus().toString().equals("PAID")) {
				totalPaid = totalPaid + storageBillReceivable.getBillReceivable().getAmount();
			}else if(storageBillReceivable.getBillReceivable().getPayStatus().toString().equals("UNPAID")){
				totalGenerated = totalGenerated + storageBillReceivable.getBillReceivable().getAmount();
			}
		}
		
		totalUngenerated = this.getUngeneratedBill(storage_.get());
		
		
		billResponse.setBillPaid(String.valueOf(totalPaid));
		billResponse.setBillGenerated(String.valueOf(totalGenerated));
		billResponse.setBillUngenerated(String.valueOf(totalUngenerated));
		billResponse.setBillUnpaid(String.valueOf(totalGenerated + totalUngenerated));
		
		
		return billResponse;
	}
	
	private double getUngeneratedBill(Storage storage) {
		
		double bill = 0;
		
		List<StorageBillReceivable> rcvs = storageBillReceivableRepository.findAllByStorage(storage);
		
		LocalDateTime fromDate = null;
		LocalDateTime toDate = null;
		double qty = 0;
		
		try {
			if(rcvs.isEmpty()) {			
				// Check for first billing date	// also check issue with timezone, this is temporary solution	
				fromDate = storage.getStartBillingAt().toLocalDate().atStartOfDay();
				
				if(toDate == null) toDate = (LocalDateTime.now().plusHours(3)).plusDays(1).toLocalDate().atStartOfDay();	
				
				if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is before bill starting date");
				
				long dayCount = ChronoUnit.DAYS.between(fromDate, toDate);
				
				qty = dayCount;
				
			}else {
				// Take the last bill
				fromDate = rcvs.get(rcvs.size() - 1).getEndedAt().plusDays(1).toLocalDate().atStartOfDay();
				
				if(toDate == null) toDate = (LocalDateTime.now().plusHours(3)).plusDays(1).toLocalDate().atStartOfDay();
				
				if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is invalid" + toDate.toString() + fromDate.toString());
				
				long dayCount = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
				
				qty = dayCount;
				
			}
			
			if(qty > 1) qty = qty - 1;
			
			bill = qty * storage.getBillingAmount() * storage.getCurrentQty();
			if(storage.getBillingType().equals("FLAT-RATE")) {
				bill = qty * storage.getBillingAmount();
			}
			
		}catch(Exception e) {
			// Do nothing
		}
		
		return bill;
	}
	
	private StorageBillReceivableResponseDTO storageBillReceivableDTOMapper(StorageBillReceivable storageBillReceivable) {
		
		StorageBillReceivableResponseDTO storageBillReceivableResponseDTO = new StorageBillReceivableResponseDTO();
		
		storageBillReceivableResponseDTO.setId(storageBillReceivable.getId().toString());
		storageBillReceivableResponseDTO.setDescription("Storage bill " + storageBillReceivable.getStartedAt().toString() + " to "  + storageBillReceivable.getEndedAt().toString());
		storageBillReceivableResponseDTO.setPrice(String.valueOf(storageBillReceivable.getPrice()));
		storageBillReceivableResponseDTO.setQty(String.valueOf(storageBillReceivable.getQty()));
		storageBillReceivableResponseDTO.setNoOfDays(String.valueOf(storageBillReceivable.getNoOfDays()));
		storageBillReceivableResponseDTO.setStartedAt(String.valueOf(storageBillReceivable.getStartedAt()));
		storageBillReceivableResponseDTO.setEndedAt(String.valueOf(storageBillReceivable.getEndedAt()));
		storageBillReceivableResponseDTO.setPayStatus(storageBillReceivable.getBillReceivable().getPayStatus().toString());
		storageBillReceivableResponseDTO.setStorageId(String.valueOf(storageBillReceivable.getStorage().getId()));
		storageBillReceivableResponseDTO.setDiscount(String.valueOf(storageBillReceivable.getDiscount()));
		storageBillReceivableResponseDTO.setDiscountStatus(
				storageBillReceivable.getDiscountStatus() != null ? storageBillReceivable.getDiscountStatus() : ""
			);
		storageBillReceivableResponseDTO.setAmount(String.valueOf(storageBillReceivable.getBillReceivable().getAmount()));
		
		return storageBillReceivableResponseDTO;
		
	}
	
	private boolean validateStorageBill(StorageBillReceivableRequestDTO storageBillReceivableRequest) {
		boolean valid = true;
		
		if(storageBillReceivableRequest.getPrice() < 0) throw new InvalidEntryException("Price must not be negative");
		if(storageBillReceivableRequest.getQty() <= 0) throw new InvalidEntryException("Qty must not be negative");
		if(storageBillReceivableRequest.getDiscount() < 0) throw new InvalidEntryException("Discount must not be negative");
		if((storageBillReceivableRequest.getQty() * storageBillReceivableRequest.getPrice() - storageBillReceivableRequest.getDiscount()) < 0) throw new InvalidEntryException("Discount must not exceed total amount");
		
		return valid;		
	}
}

@Data
class BillViewResponseDTO {
	String billPaid;
	String billGenerated;
	String billUngenerated;
	String billUnpaid;
}
