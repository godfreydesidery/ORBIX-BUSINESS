package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MaintenanceJobCardServiceController implements MaintenanceJobCardService {
	
	private final MaintenanceJobCardRepository maintenanceJobCardRepository;
	private final MaintenanceRepository maintenanceRepository;
	
	private final MaintenanceJobCardIssueBillReceivableRepository maintenanceJobCardIssueBillReceivableRepository;
	
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public MaintenanceJobCardResponseDTO showMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard, User filterByUser) {
		return maintenanceJobCardResponseDTOMapper(maintenanceJobCard, filterByUser);
	}
	
	@Override
	public MaintenanceJobCardResponseDTO createMaintenanceJobCard(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request) {
		
		Maintenance maintenance = maintenanceRepository.findById(maintenanceRequest.getId())
				.orElseThrow(() -> new NotFoundException("Maintenance not found"));
		if(!maintenance.getStatus().equals("CHECKED-IN")) {
			throw new InvalidOperationException("Maintenance must be checked in");
		}
		
		MaintenanceJobCard maintenanceJobCard = new MaintenanceJobCard();
		
		maintenanceJobCard.setNo("MJC/TEMP-" + UUID.randomUUID());
		
		maintenanceJobCard.setMaintenance(maintenance);
		maintenanceJobCard.setCreatedByUser(userService.getUser(request));
		maintenanceJobCard.setCreatedDateTime(dayService.getTimeStamp());
		maintenanceJobCard.setStatus("PENDING");
		
		maintenanceJobCard = maintenanceJobCardRepository.save(maintenanceJobCard);
		maintenanceJobCard.setNo("MJC/" + maintenanceJobCard.getId());
		return maintenanceJobCardResponseDTOMapper(maintenanceJobCardRepository.save(maintenanceJobCard), null);
	}

	@Override
	public MaintenanceJobCardResponseDTO openMaintenanceJobCard(MaintenanceJobCard jobCard,
			HttpServletRequest request) {
		MaintenanceJobCard maintenanceJobCard = maintenanceJobCardRepository.findById(jobCard.getId())
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCard not found with ID: " + jobCard.getId()));
		if(maintenanceJobCard.getStatus().equals("PENDING")) {
			maintenanceJobCard.setStatus("OPEN");
			maintenanceJobCard.setOpenedByUser(userService.getUser(request));
			maintenanceJobCard.setOpenedDateTime(dayService.getTimeStamp());
			maintenanceJobCard = maintenanceJobCardRepository.save(maintenanceJobCard);
			return maintenanceJobCardResponseDTOMapper(maintenanceJobCard, null);
		}else {
			throw new InvalidOperationException("Can only open a pending Job card");
		}
	}

	@Override
	public MaintenanceJobCardResponseDTO closeMaintenanceJobCard(MaintenanceJobCard jobCard,
			HttpServletRequest request) {
		MaintenanceJobCard maintenanceJobCard = maintenanceJobCardRepository.findById(jobCard.getId())
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCard not found with ID: " + jobCard.getId()));
		if(maintenanceJobCard.getStatus().equals("OPEN")) {
			maintenanceJobCard.setStatus("CLOSED");
			maintenanceJobCard.setClosedByUser(userService.getUser(request));
			maintenanceJobCard.setClosedDateTime(dayService.getTimeStamp());
			maintenanceJobCard = maintenanceJobCardRepository.save(maintenanceJobCard);
			return maintenanceJobCardResponseDTOMapper(maintenanceJobCard, null);
		}else {
			throw new InvalidOperationException("Can only open a pending Job card");
		}
	}
	@Override
	public MaintenanceJobCardResponseDTO reopenMaintenanceJobCard(MaintenanceJobCard jobCard,
			HttpServletRequest request) {
		MaintenanceJobCard maintenanceJobCard = maintenanceJobCardRepository.findById(jobCard.getId())
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCard not found with ID: " + jobCard.getId()));
		if(maintenanceJobCard.getStatus().equals("CLOSED")) {
			maintenanceJobCard.setStatus("OPEN");
			maintenanceJobCard = maintenanceJobCardRepository.save(maintenanceJobCard);
			return maintenanceJobCardResponseDTOMapper(maintenanceJobCard, null);
		}else {
			throw new InvalidOperationException("Can only re-open a closed Job card");
		}
	}
	
	private MaintenanceJobCardResponseDTO maintenanceJobCardResponseDTOMapper(MaintenanceJobCard maintenanceJobCard, User filterUser) {
		MaintenanceJobCardResponseDTO maintenanceJobCardResponseDTO = new MaintenanceJobCardResponseDTO();
		
		maintenanceJobCardResponseDTO.setId(maintenanceJobCard.getId().toString());
		maintenanceJobCardResponseDTO.setNo(maintenanceJobCard.getNo());
		maintenanceJobCardResponseDTO.setStatus(maintenanceJobCard.getStatus());
		maintenanceJobCardResponseDTO.setMaintenanceId(maintenanceJobCard.getMaintenance().getId().toString());
		maintenanceJobCardResponseDTO.setMaintenanceNo(maintenanceJobCard.getMaintenance().getNo());
		maintenanceJobCardResponseDTO.setCreatedBy(maintenanceJobCard.getCreatedByUser() != null ? maintenanceJobCard.getCreatedByUser().getNickname() : "");
		maintenanceJobCardResponseDTO.setCreatedDateTime(maintenanceJobCard.getCreatedDateTime() != null ? maintenanceJobCard.getCreatedDateTime().toString() : "");
		
		maintenanceJobCardResponseDTO.setOpenedBy(maintenanceJobCard.getOpenedByUser() != null ? maintenanceJobCard.getOpenedByUser().getNickname() : "");
		maintenanceJobCardResponseDTO.setOpenedDateTime(maintenanceJobCard.getOpenedDateTime() != null ? maintenanceJobCard.getOpenedDateTime().toString() : "");
		
		maintenanceJobCardResponseDTO.setClosedBy(maintenanceJobCard.getClosedByUser() != null ? maintenanceJobCard.getClosedByUser().getNickname() : "");
		maintenanceJobCardResponseDTO.setClosedDateTime(maintenanceJobCard.getClosedDateTime() != null ? maintenanceJobCard.getClosedDateTime().toString() : "");
		
		maintenanceJobCardResponseDTO.setOwnerName(maintenanceJobCard.getMaintenance().getOwnerFirstName() + " " + maintenanceJobCard.getMaintenance().getOwnerLastName());
		maintenanceJobCardResponseDTO.setVehicleEquipmentTypeName(maintenanceJobCard.getMaintenance().getVehicleEquipmentType().getName());
		maintenanceJobCardResponseDTO.setVehicleEquipmentName(maintenanceJobCard.getMaintenance().getVehicleEquipmentName());
		maintenanceJobCardResponseDTO.setChasisNo(
			    Optional.ofNullable(maintenanceJobCard)
			        .map(mjc -> mjc.getMaintenance())
			        .map(maintenance -> maintenance.getChasisNo())
			        .orElse("")
			);
		maintenanceJobCardResponseDTO.setOwnerPhoneNo(maintenanceJobCard.getMaintenance().getOwnerPhoneNo());
		maintenanceJobCardResponseDTO.setHasKeys(
			    (maintenanceJobCard != null && maintenanceJobCard.getMaintenance() != null && maintenanceJobCard.getMaintenance().isHasKeys()) 
			    ? "Yes" 
			    : "No"
			);
		// add others, on conditional
		
		List<MaintenanceJobCardIssueResponseDTO> maintenanceJobCardIssues = new ArrayList<>();
		if(maintenanceJobCard.getMaintenanceJobCardIssues() != null) {
			for(MaintenanceJobCardIssue maintenanceJobCardIssue : maintenanceJobCard.getMaintenanceJobCardIssues()) {
				List<MaintenanceJobCardIssueBillReceivable> bls = maintenanceJobCardIssueBillReceivableRepository.findAllByMaintenanceJobCardIssue(maintenanceJobCardIssue);
				String payStatus = "NA";
				for(MaintenanceJobCardIssueBillReceivable bl : bls) {
					payStatus = bl.getBillReceivable().getPayStatus().toString();
				}
				MaintenanceJobCardIssueResponseDTO issue = maintenanceJobCardIssueResponseDTOMapper(maintenanceJobCardIssue);
				issue.setPayStatus(payStatus);
				if(filterUser != null && maintenanceJobCardIssue.getServiceSpecialistUser() == filterUser) {
					maintenanceJobCardIssues.add(issue);
				}
				if(filterUser == null) {
					maintenanceJobCardIssues.add(issue);
				}			
			}
			maintenanceJobCardResponseDTO.setMaintenanceJobCardIssues(maintenanceJobCardIssues);
		}
		
		return maintenanceJobCardResponseDTO;
	}
	
	private MaintenanceJobCardIssueResponseDTO maintenanceJobCardIssueResponseDTOMapper(MaintenanceJobCardIssue maintenanceJobCardIssue) {
		MaintenanceJobCardIssueResponseDTO maintenanceJobCardIssueResponseDTO = new MaintenanceJobCardIssueResponseDTO();
		
		maintenanceJobCardIssueResponseDTO.setId(maintenanceJobCardIssue.getId().toString());
		maintenanceJobCardIssueResponseDTO.setNo(maintenanceJobCardIssue.getNo());
		maintenanceJobCardIssueResponseDTO.setStatus(maintenanceJobCardIssue.getStatus());
		maintenanceJobCardIssueResponseDTO.setMaintenanceJobCardId(maintenanceJobCardIssue.getMaintenanceJobCard().getId().toString());
		maintenanceJobCardIssueResponseDTO.setMaintenanceIssueTypeName(maintenanceJobCardIssue.getMaintenanceIssueType().getName());
		maintenanceJobCardIssueResponseDTO.setName(maintenanceJobCardIssue.getName());
		maintenanceJobCardIssueResponseDTO.setDescription(maintenanceJobCardIssue.getDescription());
		maintenanceJobCardIssueResponseDTO.setStatus(maintenanceJobCardIssue.getStatus());
		maintenanceJobCardIssueResponseDTO.setPrice(String.valueOf(maintenanceJobCardIssue.getPrice()));
		maintenanceJobCardIssueResponseDTO.setNoOfDays(String.valueOf(maintenanceJobCardIssue.getNoOfDays()));
		maintenanceJobCardIssueResponseDTO.setServiceSpecialist(maintenanceJobCardIssue.getServiceSpecialistUser().getNickname());
		
//		maintenanceJobCardIssueResponseDTO.setMaintenanceNo(maintenanceJobCard.getMaintenance().getNo());
//		maintenanceJobCardIssueResponseDTO.setCreatedBy(maintenanceJobCard.getCreatedByUser() != null ? maintenanceJobCard.getCreatedByUser().getNickname() : "");
//		maintenanceJobCardIssueResponseDTO.setCreatedDateTime(maintenanceJobCard.getCreatedDateTime() != null ? maintenanceJobCard.getCreatedDateTime().toString() : "");
//		
//		maintenanceJobCardIssueResponseDTO.setOpenedBy(maintenanceJobCard.getOpenedByUser() != null ? maintenanceJobCard.getOpenedByUser().getNickname() : "");
//		maintenanceJobCardIssueResponseDTO.setOpenedDateTime(maintenanceJobCard.getOpenedDateTime() != null ? maintenanceJobCard.getOpenedDateTime().toString() : "");
//		
//		maintenanceJobCardIssueResponseDTO.setClosedBy(maintenanceJobCard.getClosedByUser() != null ? maintenanceJobCard.getClosedByUser().getNickname() : "");
//		maintenanceJobCardIssueResponseDTO.setClosedDateTime(maintenanceJobCard.getClosedDateTime() != null ? maintenanceJobCard.getClosedDateTime().toString() : "");
//		
//		maintenanceJobCardIssueResponseDTO.setOwnerName(maintenanceJobCard.getMaintenance().getOwnerFirstName() + " " + maintenanceJobCard.getMaintenance().getOwnerLastName());
//		maintenanceJobCardIssueResponseDTO.setVehicleEquipmentTypeName(maintenanceJobCard.getMaintenance().getVehicleEquipmentType().getName());
//		maintenanceJobCardIssueResponseDTO.setVehicleEquipmentName(maintenanceJobCard.getMaintenance().getVehicleEquipmentName());
//		// add others, on conditional
		
		return maintenanceJobCardIssueResponseDTO;
	}

	
	
	
	

}
