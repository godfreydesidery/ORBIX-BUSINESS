package com.orbix.api.modules.finance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.ParkingInvoiceReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingInvoiceReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingZoneRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingZoneServiceController;
import com.orbix.api.exceptions.NotFoundException;
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
	private final ParkingInvoiceReceivableRepository parkingInvoiceReceivableRepository;
	private final ParkingRepository parkingRepository;
	
	public List<InvoiceReceivableResponseDTO> getAllInvoiceReceivables(HttpServletRequest request) {
		List<InvoiceReceivable> invoiceReceivables = invoiceReceivableRepository.findAll();
		List<InvoiceReceivableResponseDTO> invoiceReceivableResponses = new ArrayList<>();
	
		for(InvoiceReceivable invoiceReceivable : invoiceReceivables) {
			invoiceReceivableResponses.add(invoiceReceivableResponseDTOMapper(invoiceReceivable));					
		}		
		return invoiceReceivableResponses;
	}
	
	public InvoiceReceivableResponseDTO get(Long id, HttpServletRequest request) {
		
		Optional<InvoiceReceivable> invoiceReceivable_ = invoiceReceivableRepository.findById(id);
		
		InvoiceReceivable invoiceReceivable = invoiceReceivable_.orElseThrow(() -> 
	    new NotFoundException("Invoice Receivable not found with id: " + id));
		
		return invoiceReceivableResponseDTOMapper(invoiceReceivable);		
	}
	
	@Override
	public List<InvoiceReceivableResponseDTO> getPendingParkingInvoiceReceivables(HttpServletRequest request) {
		List<Parking> parkings = parkingRepository.findAll(); // Later change to find all by status, open, pending etc to avoid loading completed or canceled invoices
		List<ParkingInvoiceReceivable> parkingInvoiceReceivables = parkingInvoiceReceivableRepository.findAllByParkingIn(parkings);
		
		List<InvoiceReceivable> invoiceReceivables = new ArrayList<>();
		
		for(ParkingInvoiceReceivable parkingInvoiceReceivable : parkingInvoiceReceivables) {
			invoiceReceivables.add(parkingInvoiceReceivable.getInvoiceReceivable());
		}
		
		List<InvoiceReceivableResponseDTO> invoiceReceivableResponses = new ArrayList<>();
		
		for(InvoiceReceivable invoiceReceivable : invoiceReceivables) {
			InvoiceReceivableResponseDTO invoiceReceivableResponse = new InvoiceReceivableResponseDTO();
			invoiceReceivableResponse.setId(invoiceReceivable.getId().toString());
			invoiceReceivableResponse.setNo(invoiceReceivable.getNo());
			ParkingInvoiceReceivable parkingInvoiceReceivable = parkingInvoiceReceivableRepository.findByInvoiceReceivable(invoiceReceivable);
			invoiceReceivableResponse.setOwnerName(
				    parkingInvoiceReceivable.getParking().getOwnerFirstName() + " " +
				    (parkingInvoiceReceivable.getParking().getOwnerMiddleName() == null ? "" : (parkingInvoiceReceivable.getParking().getOwnerMiddleName()) + " ") + 
				    parkingInvoiceReceivable.getParking().getOwnerLastName()
				);
			invoiceReceivableResponse.setOwnerPhoneNo(parkingInvoiceReceivable.getParking().getOwnerPhoneNo());
			invoiceReceivableResponse.setCardNo(parkingInvoiceReceivable.getParking().getCardNo());
			invoiceReceivableResponse.setModel(parkingInvoiceReceivable.getParking().getVehicleEquipmentType().getName());
			invoiceReceivableResponse.setChasisNo(parkingInvoiceReceivable.getParking().getChasisNo());
			invoiceReceivableResponse.setStatus(parkingInvoiceReceivable.getInvoiceReceivable().getStatus());
			
			invoiceReceivableResponses.add(invoiceReceivableResponse);
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
		
		List<InvoiceReceivableDetailResponseDTO> invoiceReceivableDetails = new ArrayList<>();
		
		for(InvoiceReceivableDetail detail : invoiceReceivable.getInvoiceReceivableDetails()) {
			BillReceivableResponseDTO billReceivableResponse = new BillReceivableResponseDTO();
			InvoiceReceivableDetailResponseDTO invoiceReceivableDetailResponse = new InvoiceReceivableDetailResponseDTO();
			invoiceReceivableDetailResponse.setId(detail.getId().toString());
			
			invoiceReceivableDetailResponse.setAmount(String.valueOf(detail.getAmount()));
			
			billReceivableResponse.setId(detail.getBillReceivable().getId().toString());
			billReceivableResponse.setAmount(String.valueOf(detail.getBillReceivable().getAmount()));
			billReceivableResponse.setPaid(String.valueOf(detail.getBillReceivable().getPaid()));
			billReceivableResponse.setDue(String.valueOf(detail.getBillReceivable().getDue()));
			billReceivableResponse.setCreatedDateTime(String.valueOf(detail.getBillReceivable().getCreatedDateTime()));
			billReceivableResponse.setSummary(detail.getBillReceivable().getSummary());
			billReceivableResponse.setQty(String.valueOf(detail.getBillReceivable().getQty()));
			billReceivableResponse.setStatus(detail.getBillReceivable().getStatus());
			
			invoiceReceivableDetailResponse.setBillReceivable(billReceivableResponse);
			
			invoiceReceivableDetails.add(invoiceReceivableDetailResponse);
		}
		
		invoiceReceivableResponse.setInvoiceReceivableDetails(invoiceReceivableDetails);
		
		return invoiceReceivableResponse;
	}

	



}
