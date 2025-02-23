package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MaintenanceJobCardIssueBillReceivableServiceController implements MaintenanceJobCardIssueBillReceivableService {
	
	private final UserService userService;
	
	private final MaintenanceJobCardIssueBillReceivableRepository maintenanceJobCardIssueBillReceivableRepository;
	private final MaintenanceRepository maintenanceRepository;
	private final BillReceivableRepository billReceivableRepository;
	private final DayService dayService;

	@Override
	public List<MaintenanceJobCardIssueBillReceivableResponseDTO> getAllByMaintenance(Long maintenanceId,
			HttpServletRequest request) {
		Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new NotFoundException("Maintenance with ID " + maintenanceId + " not found."));
		
		List<MaintenanceJobCardIssueBillReceivable> maintenanceJobCardIssueBillReceivables = 
			    maintenanceJobCardIssueBillReceivableRepository.findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(maintenance);
		
		List<MaintenanceJobCardIssueBillReceivableResponseDTO> maintenanceJobCardIssueBillReceivableResponses = new ArrayList<>();
		for(MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable : maintenanceJobCardIssueBillReceivables) {
			maintenanceJobCardIssueBillReceivableResponses.add(maintenanceJobCardIssueBillReceivableDTOMapper(maintenanceJobCardIssueBillReceivable));
		}
		
		return maintenanceJobCardIssueBillReceivableResponses;
	}

	@Override
	public boolean deleteMaintenanceJobCardIssueBillReceivable(
			MaintenanceJobCardIssueBillReceivableRequestDTO maintenanceJobCardIssueBillReceivableRequest, HttpServletRequest request) {
		MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable = maintenanceJobCardIssueBillReceivableRepository.findById(maintenanceJobCardIssueBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
				
		BillReceivable billReceivable = maintenanceJobCardIssueBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be deleted");
		
		maintenanceJobCardIssueBillReceivableRepository.delete(maintenanceJobCardIssueBillReceivable);
		
		billReceivableRepository.delete(billReceivable);

		return true;
	}

//	@Override
//	public MaintenanceJobCardIssueBillReceivableResponseDTO createMaintenanceJobCardIssueBillReceivable(
//			MaintenanceJobCardIssueBillReceivableRequestDTO maintenanceJobCardIssueBillReceivableRequest, HttpServletRequest request) {
//		Maintenance maintenance = maintenanceRepository.findById(maintenanceJobCardIssueBillReceivableRequest.getMaintenanceId())
//                .orElseThrow(() -> new NotFoundException("Maintenance not found."));
//		
//		// if(!validateMaintenanceJobCardIssueBill(maintenanceJobCardIssueBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
//		
//		// Check if is first bill
//				
//		double qty = 1;
//		
//		BillReceivable billReceivable = new BillReceivable();
//		billReceivable.setNo(String.valueOf(Math.random()));
//		billReceivable.setAmount((maintenanceJobCardIssueBillReceivableRequest.getPrice() * qty) - maintenanceJobCardIssueBillReceivableRequest.getDiscount());
//		billReceivable.setPaid(0);
//		billReceivable.setDue((maintenanceJobCardIssueBillReceivableRequest.getPrice() * qty) - maintenanceJobCardIssueBillReceivableRequest.getDiscount());
//		billReceivable.setBranch(maintenance.getBranch());
//		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
//		
//		billReceivable.setPayStatus(PayStatus.UNPAID);
//		billReceivable.setSummary("Maintenance bill for maintenance#: " + maintenance.getNo());
//		
//		billReceivable = billReceivableRepository.save(billReceivable);
//		billReceivable.setNo("BR" + billReceivable.getId().toString());
//		billReceivable = billReceivableRepository.save(billReceivable);
//		
//		MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable = new MaintenanceJobCardIssueBillReceivable();
//				
//		maintenanceJobCardIssueBillReceivable.setPrice(maintenanceJobCardIssueBillReceivableRequest.getPrice());
//		maintenanceJobCardIssueBillReceivable.setQty(qty);
//		maintenanceJobCardIssueBillReceivable.setDiscount(maintenanceJobCardIssueBillReceivableRequest.getDiscount());
//		maintenanceJobCardIssueBillReceivable.setBillReceivable(billReceivable);
//		maintenanceJobCardIssueBillReceivable.setMaintenance(maintenance);
//		
//		maintenanceJobCardIssueBillReceivable = maintenanceJobCardIssueBillReceivableRepository.save(maintenanceJobCardIssueBillReceivable);
//
//		return maintenanceJobCardIssueBillReceivableDTOMapper(maintenanceJobCardIssueBillReceivable);
//	}

	@Override
	public MaintenanceJobCardIssueBillReceivableResponseDTO updateMaintenanceJobCardIssueBillReceivable(
			MaintenanceJobCardIssueBillReceivableRequestDTO maintenanceJobCardIssueBillReceivableRequest, HttpServletRequest request) {
		MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable = maintenanceJobCardIssueBillReceivableRepository.findById(maintenanceJobCardIssueBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
		
		if(!validateMaintenanceJobCardIssueBill(maintenanceJobCardIssueBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		BillReceivable billReceivable = maintenanceJobCardIssueBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be edited");
		
		billReceivable.setAmount(maintenanceJobCardIssueBillReceivableRequest.getPrice() * maintenanceJobCardIssueBillReceivableRequest.getQty() - maintenanceJobCardIssueBillReceivableRequest.getDiscount());
		billReceivable.setDue(maintenanceJobCardIssueBillReceivableRequest.getPrice() * maintenanceJobCardIssueBillReceivableRequest.getQty() - maintenanceJobCardIssueBillReceivableRequest.getDiscount());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		maintenanceJobCardIssueBillReceivable.setPrice(maintenanceJobCardIssueBillReceivableRequest.getPrice());
		maintenanceJobCardIssueBillReceivable.setQty(maintenanceJobCardIssueBillReceivableRequest.getQty());
		maintenanceJobCardIssueBillReceivable.setDiscount(maintenanceJobCardIssueBillReceivableRequest.getDiscount());
		maintenanceJobCardIssueBillReceivable = maintenanceJobCardIssueBillReceivableRepository.save(maintenanceJobCardIssueBillReceivable);
		
		return maintenanceJobCardIssueBillReceivableDTOMapper(maintenanceJobCardIssueBillReceivable);
	}

	@Override
	public MaintenanceJobCardIssueBillReceivableResponseDTO getMaintenanceJobCardIssueBillReceivable(Long id, HttpServletRequest request) {
		MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable = maintenanceJobCardIssueBillReceivableRepository.findById(id)
		        .orElseThrow(() -> new NotFoundException("Maintenance bill with ID " + id + " not found."));
				
				return maintenanceJobCardIssueBillReceivableDTOMapper(maintenanceJobCardIssueBillReceivable);
	}
	
	private MaintenanceJobCardIssueBillReceivableResponseDTO maintenanceJobCardIssueBillReceivableDTOMapper(MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable) {
		
		MaintenanceJobCardIssueBillReceivableResponseDTO maintenanceJobCardIssueBillReceivableResponseDTO = new MaintenanceJobCardIssueBillReceivableResponseDTO();
		
		maintenanceJobCardIssueBillReceivableResponseDTO.setId(maintenanceJobCardIssueBillReceivable.getId().toString());
		maintenanceJobCardIssueBillReceivableResponseDTO.setDescription(maintenanceJobCardIssueBillReceivable.getMaintenanceJobCardIssue().getName());
		maintenanceJobCardIssueBillReceivableResponseDTO.setPrice(String.valueOf(maintenanceJobCardIssueBillReceivable.getPrice()));
		maintenanceJobCardIssueBillReceivableResponseDTO.setQty(String.valueOf(maintenanceJobCardIssueBillReceivable.getQty()));
		maintenanceJobCardIssueBillReceivableResponseDTO.setPayStatus(maintenanceJobCardIssueBillReceivable.getBillReceivable().getPayStatus().toString());
		maintenanceJobCardIssueBillReceivableResponseDTO.setMaintenanceId(String.valueOf(maintenanceJobCardIssueBillReceivable.getMaintenanceJobCardIssue().getMaintenanceJobCard().getMaintenance().getId()));
		maintenanceJobCardIssueBillReceivableResponseDTO.setDiscount(String.valueOf(maintenanceJobCardIssueBillReceivable.getDiscount()));
		maintenanceJobCardIssueBillReceivableResponseDTO.setAmount(String.valueOf(maintenanceJobCardIssueBillReceivable.getBillReceivable().getAmount()));
		
		return maintenanceJobCardIssueBillReceivableResponseDTO;
		
	}
	
	private boolean validateMaintenanceJobCardIssueBill(MaintenanceJobCardIssueBillReceivableRequestDTO maintenanceJobCardIssueBillReceivableRequest) {
		boolean valid = true;
		
		if(maintenanceJobCardIssueBillReceivableRequest.getPrice() < 0) throw new InvalidEntryException("Price must not be negative");
		if(maintenanceJobCardIssueBillReceivableRequest.getQty() <= 0) throw new InvalidEntryException("Qty must not be negative");
		if(maintenanceJobCardIssueBillReceivableRequest.getDiscount() < 0) throw new InvalidEntryException("Discount must not be negative");
		if((maintenanceJobCardIssueBillReceivableRequest.getQty() * maintenanceJobCardIssueBillReceivableRequest.getPrice() - maintenanceJobCardIssueBillReceivableRequest.getDiscount()) < 0) throw new InvalidEntryException("Discount must not exceed total amount");
		
		return valid;		
	}

}
