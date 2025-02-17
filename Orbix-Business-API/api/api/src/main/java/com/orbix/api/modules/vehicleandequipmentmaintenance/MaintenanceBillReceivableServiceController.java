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
public class MaintenanceBillReceivableServiceController implements MaintenanceBillReceivableService {
	
	private final UserService userService;
	
	private final MaintenanceBillReceivableRepository maintenanceBillReceivableRepository;
	private final MaintenanceRepository maintenanceRepository;
	private final BillReceivableRepository billReceivableRepository;
	private final DayService dayService;

	@Override
	public List<MaintenanceBillReceivableResponseDTO> getAllByMaintenance(Long maintenanceId,
			HttpServletRequest request) {
		Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new NotFoundException("Maintenance with ID " + maintenanceId + " not found."));
		
		List<MaintenanceBillReceivable> maintenanceBillReceivables = maintenanceBillReceivableRepository.findAllByMaintenance(maintenance);
		
		List<MaintenanceBillReceivableResponseDTO> maintenanceBillReceivableResponses = new ArrayList<>();
		for(MaintenanceBillReceivable maintenanceBillReceivable : maintenanceBillReceivables) {
			maintenanceBillReceivableResponses.add(maintenanceBillReceivableDTOMapper(maintenanceBillReceivable));
		}
		
		return maintenanceBillReceivableResponses;
	}

	@Override
	public boolean deleteMaintenanceBillReceivable(
			MaintenanceBillReceivableRequestDTO maintenanceBillReceivableRequest, HttpServletRequest request) {
		MaintenanceBillReceivable maintenanceBillReceivable = maintenanceBillReceivableRepository.findById(maintenanceBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
				
		BillReceivable billReceivable = maintenanceBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be deleted");
		
		maintenanceBillReceivableRepository.delete(maintenanceBillReceivable);
		
		billReceivableRepository.delete(billReceivable);

		return true;
	}

	@Override
	public MaintenanceBillReceivableResponseDTO createMaintenanceBillReceivable(
			MaintenanceBillReceivableRequestDTO maintenanceBillReceivableRequest, HttpServletRequest request) {
		Maintenance maintenance = maintenanceRepository.findById(maintenanceBillReceivableRequest.getMaintenanceId())
                .orElseThrow(() -> new NotFoundException("Maintenance not found."));
		
		// if(!validateMaintenanceBill(maintenanceBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		// Check if is first bill
				
		double qty = 1;
		
		BillReceivable billReceivable = new BillReceivable();
		billReceivable.setNo(String.valueOf(Math.random()));
		billReceivable.setAmount((maintenanceBillReceivableRequest.getPrice() * qty) - maintenanceBillReceivableRequest.getDiscount());
		billReceivable.setPaid(0);
		billReceivable.setDue((maintenanceBillReceivableRequest.getPrice() * qty) - maintenanceBillReceivableRequest.getDiscount());
		billReceivable.setBranch(maintenance.getBranch());
		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
		
		billReceivable.setPayStatus(PayStatus.UNPAID);
		billReceivable.setSummary("Maintenance bill for maintenance#: " + maintenance.getNo());
		
		billReceivable = billReceivableRepository.save(billReceivable);
		billReceivable.setNo("BR" + billReceivable.getId().toString());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		MaintenanceBillReceivable maintenanceBillReceivable = new MaintenanceBillReceivable();
				
		maintenanceBillReceivable.setPrice(maintenanceBillReceivableRequest.getPrice());
		maintenanceBillReceivable.setQty(qty);
		maintenanceBillReceivable.setDiscount(maintenanceBillReceivableRequest.getDiscount());
		maintenanceBillReceivable.setBillReceivable(billReceivable);
		maintenanceBillReceivable.setMaintenance(maintenance);
		
		maintenanceBillReceivable = maintenanceBillReceivableRepository.save(maintenanceBillReceivable);

		return maintenanceBillReceivableDTOMapper(maintenanceBillReceivable);
	}

	@Override
	public MaintenanceBillReceivableResponseDTO updateMaintenanceBillReceivable(
			MaintenanceBillReceivableRequestDTO maintenanceBillReceivableRequest, HttpServletRequest request) {
		MaintenanceBillReceivable maintenanceBillReceivable = maintenanceBillReceivableRepository.findById(maintenanceBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
		
		if(!validateMaintenanceBill(maintenanceBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		BillReceivable billReceivable = maintenanceBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be edited");
		
		billReceivable.setAmount(maintenanceBillReceivableRequest.getPrice() * maintenanceBillReceivableRequest.getQty() - maintenanceBillReceivableRequest.getDiscount());
		billReceivable.setDue(maintenanceBillReceivableRequest.getPrice() * maintenanceBillReceivableRequest.getQty() - maintenanceBillReceivableRequest.getDiscount());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		maintenanceBillReceivable.setPrice(maintenanceBillReceivableRequest.getPrice());
		maintenanceBillReceivable.setQty(maintenanceBillReceivableRequest.getQty());
		maintenanceBillReceivable.setDiscount(maintenanceBillReceivableRequest.getDiscount());
		maintenanceBillReceivable = maintenanceBillReceivableRepository.save(maintenanceBillReceivable);
		
		return maintenanceBillReceivableDTOMapper(maintenanceBillReceivable);
	}

	@Override
	public MaintenanceBillReceivableResponseDTO getMaintenanceBillReceivable(Long id, HttpServletRequest request) {
		MaintenanceBillReceivable maintenanceBillReceivable = maintenanceBillReceivableRepository.findById(id)
		        .orElseThrow(() -> new NotFoundException("Maintenance bill with ID " + id + " not found."));
				
				return maintenanceBillReceivableDTOMapper(maintenanceBillReceivable);
	}
	
	private MaintenanceBillReceivableResponseDTO maintenanceBillReceivableDTOMapper(MaintenanceBillReceivable maintenanceBillReceivable) {
		
		MaintenanceBillReceivableResponseDTO maintenanceBillReceivableResponseDTO = new MaintenanceBillReceivableResponseDTO();
		
		maintenanceBillReceivableResponseDTO.setId(maintenanceBillReceivable.getId().toString());
		maintenanceBillReceivableResponseDTO.setDescription("Maintenance no: " + maintenanceBillReceivable.getMaintenance().getNo());
		maintenanceBillReceivableResponseDTO.setPrice(String.valueOf(maintenanceBillReceivable.getPrice()));
		maintenanceBillReceivableResponseDTO.setQty(String.valueOf(maintenanceBillReceivable.getQty()));
		maintenanceBillReceivableResponseDTO.setPayStatus(maintenanceBillReceivable.getBillReceivable().getPayStatus().toString());
		maintenanceBillReceivableResponseDTO.setMaintenanceId(String.valueOf(maintenanceBillReceivable.getMaintenance().getId()));
		maintenanceBillReceivableResponseDTO.setDiscount(String.valueOf(maintenanceBillReceivable.getDiscount()));
		maintenanceBillReceivableResponseDTO.setAmount(String.valueOf(maintenanceBillReceivable.getBillReceivable().getAmount()));
		
		return maintenanceBillReceivableResponseDTO;
		
	}
	
	private boolean validateMaintenanceBill(MaintenanceBillReceivableRequestDTO maintenanceBillReceivableRequest) {
		boolean valid = true;
		
		if(maintenanceBillReceivableRequest.getPrice() < 0) throw new InvalidEntryException("Price must not be negative");
		if(maintenanceBillReceivableRequest.getQty() <= 0) throw new InvalidEntryException("Qty must not be negative");
		if(maintenanceBillReceivableRequest.getDiscount() < 0) throw new InvalidEntryException("Discount must not be negative");
		if((maintenanceBillReceivableRequest.getQty() * maintenanceBillReceivableRequest.getPrice() - maintenanceBillReceivableRequest.getDiscount()) < 0) throw new InvalidEntryException("Discount must not exceed total amount");
		
		return valid;		
	}

}
