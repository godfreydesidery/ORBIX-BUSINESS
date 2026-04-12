package com.orbix.api.modules.bond;

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
import com.orbix.api.modules.adminunits.CurrencyConversion;
import com.orbix.api.modules.adminunits.CurrencyConversionRepository;
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
public class BondItemBillReceivableServiceController implements BondItemBillReceivableService {
	
	private final UserService userService;
	
	private final BondItemBillReceivableRepository bondItemBillReceivableRepository;
	private final BondItemRepository bondItemRepository;
	private final BillReceivableRepository billReceivableRepository;
	private final DayService dayService;
	
	private final CurrencyConversionRepository currencyConversionRepository;

	@Override
	public List<BondItemBillReceivableResponseDTO> getAllByBondItem(Long bondItemId, HttpServletRequest request) {
		BondItem bondItem = bondItemRepository.findById(bondItemId)
                .orElseThrow(() -> new NotFoundException("BondItem with ID " + bondItemId + " not found."));
		
		List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository.findAllByBondItem(bondItem);
		
		List<BondItemBillReceivableResponseDTO> bondItemBillReceivableResponses = new ArrayList<>();
		for(BondItemBillReceivable bondItemBillReceivable : bondItemBillReceivables) {
			bondItemBillReceivableResponses.add(bondItemBillReceivableDTOMapper(bondItemBillReceivable));
		}
		
		return bondItemBillReceivableResponses;
	}
	
	@Override
	public BondItemBillReceivableResponseDTO getBondItemBillReceivable(Long id, HttpServletRequest request) {
		BondItemBillReceivable bondItemBillReceivable = bondItemBillReceivableRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("BondItem bill with ID " + id + " not found."));
		
		return bondItemBillReceivableDTOMapper(bondItemBillReceivable);
	}
	
//	@Override
//	public BondItemBillReceivableResponseDTO createBondItemBillReceivable(
//			BondItemBillReceivableRequestDTO bondItemBillReceivableRequest,
//			HttpServletRequest request) {
//		
//		BondItem bondItem = bondItemRepository.findById(bondItemBillReceivableRequest.getBondItemId())
//                .orElseThrow(() -> new NotFoundException("BondItem not found."));
//		
//		// if(!validateBondItemBill(bondItemBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
//		
//		// Check if is first bill
//		
//		List<BondItemBillReceivable> rcvs = bondItemBillReceivableRepository.findAllByBondItem(bondItem);
//		
//		LocalDateTime fromDate = null;
//		LocalDateTime toDate = null;
//		double qty = 0;
//		
//		if(bondItemBillReceivableRequest.getEndedAt() != null) {
//			String dateString = bondItemBillReceivableRequest.getEndedAt() + " 00:00:00";
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//			toDate = LocalDateTime.parse(dateString, formatter).plusDays(1).toLocalDate().atStartOfDay();
//		}
//		
//		if(rcvs.isEmpty()) {			
//			// Check for first billing date		
//			fromDate = bondItem.getStartBillingAt().toLocalDate().atStartOfDay();
//			
//			if(toDate == null) toDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();	
//			
//			if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is before bill starting date");
//			
//			long dayCount = ChronoUnit.DAYS.between(fromDate, toDate);
//			
//			qty = dayCount;
//			
//		}else {
//			// Take the last bill
//			fromDate = rcvs.get(rcvs.size() - 1).getEndedAt().plusDays(1).toLocalDate().atStartOfDay();
//			
//			if(toDate == null) toDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
//			
//			if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is invalid " + toDate.toString() + fromDate.toString());
//			
//			long dayCount = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
//			
//			qty = dayCount;
//			
//		}
//		
//		if(qty > 1) qty = qty - 1;
//			
//		BillReceivable billReceivable = new BillReceivable();
//		billReceivable.setNo(String.valueOf(Math.random()));
//		billReceivable.setAmount((bondItem.getBillingAmount() * qty) - bondItemBillReceivableRequest.getDiscount());
//		billReceivable.setPaid(0);
//		billReceivable.setDue((bondItem.getBillingAmount() * qty) - bondItemBillReceivableRequest.getDiscount());
//		billReceivable.setBranch(bondItem.getBranch());
//		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
//		
//		billReceivable.setPayStatus(PayStatus.UNPAID);
//		billReceivable.setSummary("BondItem bill for bondItem#: " + bondItem.getNo());
//		
//		billReceivable = billReceivableRepository.save(billReceivable);
//		billReceivable.setNo("BR" + billReceivable.getId().toString());
//		billReceivable = billReceivableRepository.save(billReceivable);
//		
//		BondItemBillReceivable bondItemBillReceivable = new BondItemBillReceivable();
//		
//		bondItemBillReceivable.setStartedAt(fromDate);
//		bondItemBillReceivable.setEndedAt(toDate);
//		
////		if(bondItemBillReceivableRequest.getStartedAt() == null) {
////			bondItemBillReceivable.setStartedAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this depending on user billing preferences
////		}else {
////			//String dateString = "2024-10-26 15:30:45" ;
////			String dateString = bondItemBillReceivableRequest.getStartedAt() + " 00:00:00";
////			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
////			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
////			bondItemBillReceivable.setStartedAt(dateTime);
////		}
//		
////		if(bondItemBillReceivableRequest.getEndedAt() == null) {
////			bondItemBillReceivable.setEndedAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this depending on user billing preferences
////		}else {
////			//String dateString = "2024-10-26 15:30:45" ;
////			String dateString = bondItemBillReceivableRequest.getEndedAt() + " 00:00:00";
////			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
////			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
////			bondItemBillReceivable.setEndedAt(dateTime);
////		}
//		
//		bondItemBillReceivable.setPrice(bondItem.getBillingAmount());
//		bondItemBillReceivable.setQty(qty);
//		bondItemBillReceivable.setDiscount(bondItemBillReceivableRequest.getDiscount());
//		bondItemBillReceivable.setBillReceivable(billReceivable);
//		bondItemBillReceivable.setBondItem(bondItem);
//		
//		bondItemBillReceivable = bondItemBillReceivableRepository.save(bondItemBillReceivable);
//
//		return bondItemBillReceivableDTOMapper(bondItemBillReceivable);
//	}
	
	@Override
	public BondItemBillReceivableResponseDTO createBondItemBillReceivable(
	        BondItemBillReceivableRequestDTO request, HttpServletRequest httpRequest) {

	    // Formatter for parsing String date (yyyy-MM-dd)
	    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	    // 1. Fetch bond item
	    BondItem bondItem = bondItemRepository.findById(request.getBondItemId())
	            .orElseThrow(() -> new NotFoundException("BondItem not found."));

	    // 2. Fetch previous receivables
	    List<BondItemBillReceivable> rcvs = bondItemBillReceivableRepository.findAllByBondItem(bondItem);

	    // 3. Resolve FROM date
	    LocalDateTime fromDate;
	    if (rcvs.isEmpty()) {
	        fromDate = bondItem.getStartBillingAt().toLocalDate().atStartOfDay();
	    } else {
	        fromDate = rcvs.get(rcvs.size() - 1)
	                .getEndedAt()
	                .plusDays(1)
	                .toLocalDate()
	                .atStartOfDay();
	    }

	    // 4. Resolve reference date (endedAt or now)
	    LocalDateTime referenceDate;
	    if (request.getEndedAt() != null && !request.getEndedAt().isEmpty()) {
	        referenceDate = LocalDate.parse(request.getEndedAt(), dateFormatter).atStartOfDay();
	    } else {
	        referenceDate = LocalDateTime.now();
	    }

	    // 5. Calculate elapsed days
//	    long elapsedDays = ChronoUnit.DAYS.between(fromDate, referenceDate);
//
//	    if (elapsedDays <= 0) {
//	        throw new InvalidOperationException(
//	                "Reference date must be after start date: from=" + fromDate + ", ref=" + referenceDate);
//	    }
	    
	    if (referenceDate.isBefore(fromDate)) {
	        throw new InvalidOperationException(
	            "Reference date must not be before start date: from=" + fromDate + ", ref=" + referenceDate);
	    }

	    long elapsedDays = ChronoUnit.DAYS.between(fromDate, referenceDate);

	    // Allow same-day billing → normalize to minimum 1 day
	    elapsedDays = Math.max(elapsedDays, 1);

	    // 6. Convert days → months (1–30 = 1, 31–60 = 2, etc.)
	    int months = (int) ((elapsedDays - 1) / 30) + 1;

	    // 7. Derive TO date (catch-up billing)
	    LocalDateTime toDate = fromDate.plusDays(months * 30L);
	    
	    java.util.Currency currency = bondItem.getCurrency();
	    double rate = 1;
	    if(currency != null) {
	    	CurrencyConversion conv = currencyConversionRepository.findBySourceCurrencyCodeAndFinalCurrencyCode(currency, java.util.Currency.getInstance("TZS"));
	    	if(conv == null) {
	    		throw new NotFoundException("No conversion rate found");
	    	}
	    	if(!conv.isActive() || conv.getSourceCurrencyValue() <= 0 || conv.getFinalCurrencyValue() <= 0) {
	    		throw new NotFoundException("Invalid or expited rate");
	    	}
	    	rate = Math.abs((conv.getFinalCurrencyValue()/conv.getSourceCurrencyValue()));
	    }
	    

	    // 8. Calculate billing amount
	    double totalAmount = (bondItem.getBillingAmount() * months * rate);

	    // 9. Create BillReceivable
	    BillReceivable billReceivable = new BillReceivable();
	    billReceivable.setNo(String.valueOf(Math.random())); // TODO: replace with proper generator
	    billReceivable.setAmount(totalAmount);
	    billReceivable.setPaid(0);
	    billReceivable.setDue(totalAmount);
	    billReceivable.setBranch(bondItem.getBranch());
	    billReceivable.setCreatedDateTime(dayService.getTimeStamp());
	    billReceivable.setPayStatus(PayStatus.UNPAID);
	    billReceivable.setSummary("Bond Item bill for bond Item#: " + bondItem.getNo());

	    billReceivable = billReceivableRepository.save(billReceivable);
	    billReceivable.setNo("BR" + billReceivable.getId());
	    billReceivable = billReceivableRepository.save(billReceivable);

	    // 10. Create BondItemBillReceivable
	    BondItemBillReceivable entity = new BondItemBillReceivable();
	    entity.setStartedAt(fromDate);
	    entity.setEndedAt(toDate);
	    entity.setPrice(bondItem.getBillingAmount());
	    entity.setQty((double) months);
	    entity.setNoOfDays(months * 30);
	    entity.setDiscount(request.getDiscount());
	    entity.setBillReceivable(billReceivable);
	    entity.setBondItem(bondItem);

	    entity = bondItemBillReceivableRepository.save(entity);

	    // 11. Return DTO
	    return bondItemBillReceivableDTOMapper(entity);
	}
	
	@Override
	public BondItemBillReceivableResponseDTO updateBondItemBillReceivable(
			BondItemBillReceivableRequestDTO bondItemBillReceivableRequest,
			HttpServletRequest request) {
		
		BondItemBillReceivable bondItemBillReceivable = bondItemBillReceivableRepository.findById(bondItemBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
		
		if(!validateBondItemBill(bondItemBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		BillReceivable billReceivable = bondItemBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be edited");
		
		billReceivable.setAmount(bondItemBillReceivableRequest.getPrice() * bondItemBillReceivableRequest.getQty() - bondItemBillReceivableRequest.getDiscount());
		billReceivable.setDue(bondItemBillReceivableRequest.getPrice() * bondItemBillReceivableRequest.getQty() - bondItemBillReceivableRequest.getDiscount());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		bondItemBillReceivable.setPrice(bondItemBillReceivableRequest.getPrice());
		bondItemBillReceivable.setQty(bondItemBillReceivableRequest.getQty());
		bondItemBillReceivable.setDiscount(bondItemBillReceivableRequest.getDiscount());
		bondItemBillReceivable = bondItemBillReceivableRepository.save(bondItemBillReceivable);
		
		return bondItemBillReceivableDTOMapper(bondItemBillReceivable);
	}
	
	@Override
	public BondItemBillReceivableResponseDTO createBondItemCustomBillReceivable(
			BondItemBillReceivableRequestDTO bondItemBillReceivableRequest,
			HttpServletRequest request) {
		
		BondItem bondItem = bondItemRepository.findById(bondItemBillReceivableRequest.getBondItemId())
                .orElseThrow(() -> new NotFoundException("BondItem not found."));
		
		LocalDateTime startBillingAt = bondItem.getStartBillingAt();
		double noOfDays = (long) Math.floor((double) Duration.between(startBillingAt, LocalDateTime.now()).toHours() / 24);
		if(noOfDays <=0 ) {
			noOfDays = 1;
		}
		if(bondItem.getBillingType().equals("FLAT-RATE")) {
			noOfDays = 1;
		}
				
		List<BondItemBillReceivable> rcvs = bondItemBillReceivableRepository.findAllByBondItem(bondItem);
		
		double billedQty = 0;
		
		for(BondItemBillReceivable sbr : rcvs) {
			billedQty = billedQty + sbr.getQty();
		}
		
		
		BillReceivable billReceivable = new BillReceivable();
		billReceivable.setNo(String.valueOf(Math.random()));
		billReceivable.setPaid(0);
		billReceivable.setQty(1);
		if(bondItem.getBillingType().equals("DAILY")) {
			billReceivable.setAmount((bondItem.getBillingAmount() * noOfDays) - bondItemBillReceivableRequest.getDiscount());
			billReceivable.setDue((bondItem.getBillingAmount() * noOfDays) - bondItemBillReceivableRequest.getDiscount());
		}else if(bondItem.getBillingType().equals("FLAT-RATE")) {
			billReceivable.setAmount((bondItem.getBillingAmount()) - bondItemBillReceivableRequest.getDiscount());
			billReceivable.setDue((bondItem.getBillingAmount()) - bondItemBillReceivableRequest.getDiscount());
		}else {
			throw new InvalidOperationException("Invalid Billing Type");
		}
		
		billReceivable.setBranch(bondItem.getBranch());
		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
		
		billReceivable.setPayStatus(PayStatus.UNPAID);
		billReceivable.setSummary(bondItem.getNo() + " " + bondItem.getBondItemName());
		
		billReceivable = billReceivableRepository.save(billReceivable);
		billReceivable.setNo("BR" + billReceivable.getId().toString());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		BondItemBillReceivable bondItemBillReceivable = new BondItemBillReceivable();
		
		LocalDate today = LocalDate.now();
		bondItemBillReceivable.setStartedAt(bondItem.getStartBillingAt());
		bondItemBillReceivable.setEndedAt(today.atTime(23, 59, 59));
		
		bondItemBillReceivable.setPrice(bondItem.getBillingAmount());
		bondItemBillReceivable.setQty(1);
		bondItemBillReceivable.setNoOfDays(noOfDays);
		bondItemBillReceivable.setDiscount(bondItemBillReceivableRequest.getDiscount());
		bondItemBillReceivable.setBillReceivable(billReceivable);
		bondItemBillReceivable.setBondItem(bondItem);
		
		bondItemBillReceivable = bondItemBillReceivableRepository.save(bondItemBillReceivable);

		return bondItemBillReceivableDTOMapper(bondItemBillReceivable);
	}
	
	@Override
	public BillViewResponseDTO getBillView(Long bondItemId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		Optional<BondItem> bondItem_ = bondItemRepository.findById(bondItemId);
		
		if(bondItem_.isEmpty()) {
			throw new NotFoundException("BondItem not found");
		}
		
		BillViewResponseDTO billResponse = new BillViewResponseDTO();
		billResponse.setBillPaid("0");
		billResponse.setBillGenerated("0");
		billResponse.setBillUngenerated("0");
		billResponse.setBillUnpaid("0");
		
		double totalPaid = 0;
		double totalGenerated = 0;
		double totalUngenerated = 0;
		
		List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository.findAllByBondItem(bondItem_.get());
		for(BondItemBillReceivable bondItemBillReceivable : bondItemBillReceivables) {
			if(bondItemBillReceivable.getBillReceivable().getPayStatus().toString().equals("PAID")) {
				totalPaid = totalPaid + bondItemBillReceivable.getBillReceivable().getAmount();
			}else if(bondItemBillReceivable.getBillReceivable().getPayStatus().toString().equals("UNPAID")){
				totalGenerated = totalGenerated + bondItemBillReceivable.getBillReceivable().getAmount();
			}
		}
		
		totalUngenerated = this.getUngeneratedBill(bondItem_.get());
		
		
		billResponse.setBillPaid(String.valueOf(totalPaid));
		billResponse.setBillGenerated(String.valueOf(totalGenerated));
		billResponse.setBillUngenerated(String.valueOf(totalUngenerated));
		billResponse.setBillUnpaid(String.valueOf(totalGenerated + totalUngenerated));
		
		
		return billResponse;
	}
	
//	private double getUngeneratedBill(BondItem bondItem) {
//		
//		double bill = 0;
//		
//		List<BondItemBillReceivable> rcvs = bondItemBillReceivableRepository.findAllByBondItem(bondItem);
//		
//		LocalDateTime fromDate = null;
//		LocalDateTime toDate = null;
//		double qty = 0;
//		
//		try {
//			if(rcvs.isEmpty()) {			
//				// Check for first billing date	// also check issue with timezone, this is temporary solution	
//				fromDate = bondItem.getStartBillingAt().toLocalDate().atStartOfDay();
//				
//				if(toDate == null) toDate = (LocalDateTime.now().plusHours(3)).plusDays(1).toLocalDate().atStartOfDay();	
//				
//				if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is before bill starting date");
//				
//				long dayCount = ChronoUnit.DAYS.between(fromDate, toDate);
//				
//				qty = dayCount;
//				
//			}else {
//				// Take the last bill
//				fromDate = rcvs.get(rcvs.size() - 1).getEndedAt().plusDays(1).toLocalDate().atStartOfDay();
//				
//				if(toDate == null) toDate = (LocalDateTime.now().plusHours(3)).plusDays(1).toLocalDate().atStartOfDay();
//				
//				if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is invalid" + toDate.toString() + fromDate.toString());
//				
//				long dayCount = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
//				
//				qty = dayCount;
//				
//			}
//			
//			if(qty > 1) qty = qty - 1;
//			
//			bill = qty * bondItem.getBillingAmount() * bondItem.getCurrentQty();
//			if(bondItem.getBillingType().equals("FLAT-RATE")) {
//				bill = bondItem.getCurrentQty() * bondItem.getBillingAmount();
//			}
//			
//		}catch(Exception e) {
//			// Do nothing
//		}
//		
//		return bill;
//	}
	
	private double getUngeneratedBill(BondItem bondItem) {

	    List<BondItemBillReceivable> rcvs = bondItemBillReceivableRepository.findAllByBondItem(bondItem);

	    // 1. Determine start point (last billed + 1 day OR initial start)
	    LocalDateTime fromDate;
	    if (rcvs.isEmpty()) {
	        fromDate = bondItem.getStartBillingAt().toLocalDate().atStartOfDay();
	    } else {
	        fromDate = rcvs.get(rcvs.size() - 1)
	                .getEndedAt()
	                .plusDays(1)
	                .toLocalDate()
	                .atStartOfDay();
	    }

	    // 2. Use today as reference
	    LocalDateTime today = LocalDate.now().atStartOfDay();

	    // 3. If nothing to bill yet
	    if (today.isBefore(fromDate)) {
	        return 0;
	    }

	    // 4. Calculate elapsed days
	    
	    long elapsedDays = ChronoUnit.DAYS.between(fromDate, today) + 1;

	    if (elapsedDays <= 0) {
	        return 0;
	    }

	    // 5. Convert to 30-day buckets
	    int months = (int) ((elapsedDays - 1) / 30) + 1;

	    // 6. Calculate bill
	    double bill;
	    if ("FLAT-RATE".equals(bondItem.getBillingType())) {
	        bill = bondItem.getBillingAmount() * bondItem.getCurrentQty();
	    } else {
	        bill = months * bondItem.getBillingAmount() * bondItem.getCurrentQty();
	    }

	    return bill;
	}
	
	private BondItemBillReceivableResponseDTO bondItemBillReceivableDTOMapper(BondItemBillReceivable bondItemBillReceivable) {
		
		BondItemBillReceivableResponseDTO bondItemBillReceivableResponseDTO = new BondItemBillReceivableResponseDTO();
		
		bondItemBillReceivableResponseDTO.setId(bondItemBillReceivable.getId().toString());
		bondItemBillReceivableResponseDTO.setDescription("BondItem bill " + bondItemBillReceivable.getStartedAt().toString() + " to "  + bondItemBillReceivable.getEndedAt().toString());
		bondItemBillReceivableResponseDTO.setPrice(String.valueOf(bondItemBillReceivable.getPrice()));
		bondItemBillReceivableResponseDTO.setQty(String.valueOf(bondItemBillReceivable.getQty()));
		bondItemBillReceivableResponseDTO.setNoOfDays(String.valueOf(bondItemBillReceivable.getNoOfDays()));
		bondItemBillReceivableResponseDTO.setStartedAt(String.valueOf(bondItemBillReceivable.getStartedAt()));
		bondItemBillReceivableResponseDTO.setEndedAt(String.valueOf(bondItemBillReceivable.getEndedAt()));
		bondItemBillReceivableResponseDTO.setPayStatus(bondItemBillReceivable.getBillReceivable().getPayStatus().toString());
		bondItemBillReceivableResponseDTO.setBondItemId(String.valueOf(bondItemBillReceivable.getBondItem().getId()));
		bondItemBillReceivableResponseDTO.setBillingType(bondItemBillReceivable.getBondItem().getBillingType());
		bondItemBillReceivableResponseDTO.setDiscount(String.valueOf(bondItemBillReceivable.getDiscount()));
		bondItemBillReceivableResponseDTO.setDiscountStatus(
				bondItemBillReceivable.getDiscountStatus() != null ? bondItemBillReceivable.getDiscountStatus() : ""
			);
		bondItemBillReceivableResponseDTO.setAmount(String.valueOf(bondItemBillReceivable.getBillReceivable().getAmount()));
		
		return bondItemBillReceivableResponseDTO;
		
	}
	
	private boolean validateBondItemBill(BondItemBillReceivableRequestDTO bondItemBillReceivableRequest) {
		boolean valid = true;
		
		if(bondItemBillReceivableRequest.getPrice() < 0) throw new InvalidEntryException("Price must not be negative");
		if(bondItemBillReceivableRequest.getQty() <= 0) throw new InvalidEntryException("Qty must not be negative");
		if(bondItemBillReceivableRequest.getDiscount() < 0) throw new InvalidEntryException("Discount must not be negative");
		if((bondItemBillReceivableRequest.getQty() * bondItemBillReceivableRequest.getPrice() - bondItemBillReceivableRequest.getDiscount()) < 0) throw new InvalidEntryException("Discount must not exceed total amount");
		
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
