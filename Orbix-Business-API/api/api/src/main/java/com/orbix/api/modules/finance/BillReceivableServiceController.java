package com.orbix.api.modules.finance;

import java.util.ArrayList;
import java.util.List;

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
	
	@Override
	public List<BillReceivableResponseDTO> confirmBillPayment(List<BillReceivableRequestDTO> billRequests,
			double totalAmount, HttpServletRequest request) {
	
		
		double total = 0;
		for(BillReceivableRequestDTO bl : billRequests) {
			BillReceivable billReceivable = billReceivableRepository.findById(bl.getId()).get();
			total = total + billReceivable.getDue();
			if(bl.getAmount() != billReceivable.getDue()) throw new InvalidOperationException("Can not accept partial bill payment");
			billReceivable.setPaid(bl.getAmount());
			billReceivable.setDue(0);
			billReceivable.setStatus("PAID");
			
			billReceivable = billReceivableRepository.save(billReceivable);
			
//			InvoiceReceivableDetail invoiceReceivableDetail = invoiceReceivableDetailRepository.findByBillReceivable(billReceivable);
//			invoiceReceivableDetail.setPaid(bl.getAmount());
//			invoiceReceivableDetail.setDue(0);
//			
//			invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);
		}
		if(total != totalAmount) throw new InvalidOperationException("Amounts do not match");	
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
