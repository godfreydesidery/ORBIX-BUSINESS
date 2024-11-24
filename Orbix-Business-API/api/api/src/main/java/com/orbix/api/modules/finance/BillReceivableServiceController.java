package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingInvoiceReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingServiceBillReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingServiceBillReceivableRepository;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BillReceivableServiceController implements BillReceivableService {
	
	private final BillReceivableRepository billReceivableRepository;
	private final ParkingRepository parkingRepository;
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	private final ParkingServiceBillReceivableRepository parkingServiceBillReceivableRepository;
	private final InvoiceReceivableDetailRepository invoiceReceivableDetailRepository;
	
	private final BillReceivableCollectionRepository billReceivableCollectionRepository;
	
	private final CashCollectionRepository cashCollectionRepository;
	private final UserService userService;
	
	private final DayService dayService;
	
	@Override
	public List<BillReceivableResponseDTO> confirmBillPayment(BillReceivableSummaryDTO billReceivableSummary,
			double totalAmount, HttpServletRequest request) {
		 double t = 0;
		for( BillReceivableCollectionRequestDTO c : billReceivableSummary.getBillReceivableCollections()) {
			t = t + c.getAmount();
		}
		if(t != totalAmount) {
			throw new InvalidOperationException("Could not process. Total amount mismatch");
		}
	
		
		double total = 0;
		LocalDateTime dateTime = dayService.getTimeStamp();
		List<BillReceivable> billsToConsider = new ArrayList<>();
		for(BillReceivableRequestDTO bl : billReceivableSummary.getBillReceivables()) {
			BillReceivable billReceivable = billReceivableRepository.findById(bl.getId()).get();
			total = total + billReceivable.getDue();
			if(bl.getAmount() != billReceivable.getDue()) throw new InvalidOperationException("Can not accept partial bill payment");
			billReceivable.setPaid(bl.getAmount());
			billReceivable.setDue(0);
			billReceivable.setStatus("PAID");
			billReceivable.setPaidDateTime(dateTime);
			
			billReceivable = billReceivableRepository.save(billReceivable);
			billsToConsider.add(billReceivable);
			
			CashCollection cashCollection = new CashCollection();
			
			cashCollection.setAmount(bl.getAmount());
			cashCollection.setPaymentType("CASH");
			cashCollection.setReason("General Payment");
			cashCollection.setCollectionDateTime(dateTime);
			cashCollection.setCollectedByUser(userService.getUser(request));
			cashCollection.setBillReceivable(billReceivable);
			
			Optional<ParkingBillReceivable> parkingBillReceivable = parkingBillReceivableRepository.findByBillReceivable(billReceivable);
			if(parkingBillReceivable.isPresent()) cashCollection.setReason("Vehicle and Equipment/Parking");
			Optional<ParkingServiceBillReceivable> parkingServiceBillReceivable = parkingServiceBillReceivableRepository.findByBillReceivable(billReceivable);
			if(parkingServiceBillReceivable.isPresent()) cashCollection.setReason("Vehicle and Equipment/Service");
			
			cashCollectionRepository.save(cashCollection);
			
//			InvoiceReceivableDetail invoiceReceivableDetail = invoiceReceivableDetailRepository.findByBillReceivable(billReceivable);
//			invoiceReceivableDetail.setPaid(bl.getAmount());
//			invoiceReceivableDetail.setDue(0);
//			
//			invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);
		}
		if(total != totalAmount) throw new InvalidOperationException("Amounts do not match");	
		
		
			List<BillReceivableCollectionRequestDTO> billReceivableCollections = billReceivableSummary.getBillReceivableCollections();

			double billNewAmount = 0;
			for (BillReceivableCollectionRequestDTO billReceivableCollectionRequest : billReceivableCollections) {
			    
				
				
			double summaryAmount = billReceivableCollectionRequest.getAmount();
		    
		    Iterator<BillReceivable> iterator = billsToConsider.iterator();
		    while (iterator.hasNext()) {
		        BillReceivable billReceivable = iterator.next();
		        double billAmount;
		        if(billNewAmount <= 0) {
		        	billAmount  = billReceivable.getAmount();
		        }else {
		        	billAmount = billNewAmount;
		        }
		        
		        
		        if (summaryAmount >= billAmount) {
		            BillReceivableCollection billReceivableCollection = new BillReceivableCollection();
		            double amountToClear = billAmount;
		            billReceivableCollection.setAmount(amountToClear);
		            summaryAmount = summaryAmount - billAmount;
		            billNewAmount = 0;
		            
		            billReceivableCollection.setPayCode(billReceivableCollectionRequest.getPayCode());
		            billReceivableCollection.setReason("General Payment");
		            billReceivableCollection.setPartial(false);
		            billReceivableCollection.setRefNo(billReceivableCollectionRequest.getRefNo());
		            billReceivableCollection.setCollectionDateTime(dateTime);
		            billReceivableCollection.setCollectedByUser(userService.getUser(request));
		            billReceivableCollection.setBillReceivable(billReceivable);
		            billReceivableCollectionRepository.save(billReceivableCollection);

		            iterator.remove(); // Safe removal
		        } else if (summaryAmount < billAmount && summaryAmount > 0) {
		            BillReceivableCollection billReceivableCollection = new BillReceivableCollection();
		            double amountToClear = summaryAmount;
		            billReceivableCollection.setAmount(amountToClear);
		            
		            billNewAmount = billAmount - summaryAmount;
		            
        			summaryAmount = 0;
        			
		            billReceivableCollection.setPayCode(billReceivableCollectionRequest.getPayCode());
		            billReceivableCollection.setReason("General Payment");
		            billReceivableCollection.setPartial(true);
		            billReceivableCollection.setRefNo(billReceivableCollectionRequest.getRefNo());
		            billReceivableCollection.setCollectionDateTime(dateTime);
		            billReceivableCollection.setCollectedByUser(userService.getUser(request));
		            billReceivableCollection.setBillReceivable(billReceivable);
		            billReceivableCollectionRepository.save(billReceivableCollection);
		        }
		    }
		}
	return null;
	}

	@Override
	public List<BillReceivableResponseDTO> getAllByParking(Long parkingId, HttpServletRequest request) {
		Parking parking = parkingRepository.findById(parkingId)
			    .orElseThrow(() -> new NotFoundException("Parking with ID " + parkingId + " not found"));
		
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParking(parking);
		List<ParkingServiceBillReceivable> parkingServiceBillReceivables = parkingServiceBillReceivableRepository.findAllByParking(parking);
		List<BillReceivableResponseDTO> billReceivableResponses = new ArrayList<>();
		for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(parkingBillReceivable.getBillReceivable()));
		}	
		for(ParkingServiceBillReceivable parkingServiceBillReceivable : parkingServiceBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(parkingServiceBillReceivable.getBillReceivable()));
		}
		return billReceivableResponses;
	}
	
	private BillReceivableResponseDTO billReceivableResponseDTOMapper(BillReceivable billReceivable) {
		BillReceivableResponseDTO billReceivableResponse = new BillReceivableResponseDTO();
		billReceivableResponse.setId(billReceivable.getId().toString());
		billReceivableResponse.setAmount(String.valueOf(billReceivable.getAmount()));
		billReceivableResponse.setBranchId(billReceivable.getBranch().getId().toString());
		billReceivableResponse.setCompanyId("");
		billReceivableResponse.setBranchName(billReceivable.getBranch().getName());
		billReceivableResponse.setCreatedDateTime(billReceivable.getCreatedDateTime().toString());
		billReceivableResponse.setNo(billReceivable.getNo());
		billReceivableResponse.setQty(String.valueOf(billReceivable.getQty()));
		billReceivableResponse.setStatus(billReceivable.getStatus());
		billReceivableResponse.setSummary(billReceivable.getSummary());
		billReceivableResponse.setDue(String.valueOf(billReceivable.getDue()));
		billReceivableResponse.setPaid(String.valueOf(billReceivable.getPaid()));
		
		return billReceivableResponse;
		
		
		
	}

}
