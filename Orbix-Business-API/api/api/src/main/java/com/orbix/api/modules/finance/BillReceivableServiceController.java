package com.orbix.api.modules.finance;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.vehicleandequipmentparking.ParkingInvoiceReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRepository;
import com.orbix.api.exceptions.InvalidOperationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BillReceivableServiceController implements BillReceivableService {
	
	private final BillReceivableRepository billReceivableRepository;
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
			
			InvoiceReceivableDetail invoiceReceivableDetail = invoiceReceivableDetailRepository.findByBillReceivable(billReceivable);
			invoiceReceivableDetail.setPaid(bl.getAmount());
			invoiceReceivableDetail.setDue(0);
			
			invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);
		}
		if(total != totalAmount) throw new InvalidOperationException("Amounts do not match");	
	return null;
	}

}
