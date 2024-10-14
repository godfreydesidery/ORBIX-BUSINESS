package com.orbix.api.modules.finance;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.vehicleandequipmentparking.ParkingZoneRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingZoneServiceController;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InvoiceReceivableServiceController implements InvoiceReceivableService {
	
	private final InvoiceReceivableRepository invoiceReceivableRepository;
	
	public List<InvoiceReceivableResponseDTO> getAllInvoiceReceivables(HttpServletRequest request) {
		List<InvoiceReceivable> invoiceReceivables = invoiceReceivableRepository.findAll();
		List<InvoiceReceivableResponseDTO> invoiceReceivableResponses = new ArrayList<>();
	
		for(InvoiceReceivable invoiceReceivable : invoiceReceivables) {
			invoiceReceivableResponses.add(invoiceReceivableResponseDTOMapper(invoiceReceivable));					
		}		
		return invoiceReceivableResponses;
	}

	private InvoiceReceivableResponseDTO invoiceReceivableResponseDTOMapper(InvoiceReceivable invoiceReceivable) {
		InvoiceReceivableResponseDTO invoiceReceivableResponse = new InvoiceReceivableResponseDTO();
		
		invoiceReceivableResponse.setId(String.valueOf(invoiceReceivable.getId()));
		invoiceReceivableResponse.setNo(invoiceReceivable.getNo());
		invoiceReceivableResponse.setCompanyName(invoiceReceivable.getCompany().getName());
		invoiceReceivableResponse.setBranchName(invoiceReceivable.getBranch().getName());
		invoiceReceivableResponse.setStatus(invoiceReceivable.getStatus());
		invoiceReceivableResponse.setSummary(invoiceReceivable.getSummary());
		
		
		return invoiceReceivableResponse;
	}



}
