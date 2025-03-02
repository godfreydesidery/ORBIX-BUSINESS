package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.warehouse.StorageBillReceivable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MaintenanceJobCardIssueServiceController implements MaintenanceJobCardIssueService {
	private final MaintenanceJobCardIssueRepository maintenanceJobCardIssueRepository;
	private final MaintenanceJobCardRepository maintenanceJobCardRepository;
	
	private final UserService userService;
	private final DayService dayService;
	private final UserRepository userRepository;
	private final MaintenanceIssueTypeRepository maintenanceIssueTypeRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	private final MaintenanceJobCardIssueBillReceivableRepository maintenanceJobCardIssueBillReceivableRepository;
	
	@Override
	public MaintenanceJobCardIssueResponseDTO get(Long id, HttpServletRequest request ) {
		MaintenanceJobCardIssue maintenanceJobCardIssue = maintenanceJobCardIssueRepository.findById(id)
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCardIssue not found with ID: " + id));
		return maintenanceJobCardIssueResponseDTOMapper(maintenanceJobCardIssue);
	}
	
	@Override
	public MaintenanceJobCardIssueResponseDTO createMaintenanceJobCardIssue(MaintenanceJobCardRequestDTO maintenanceJobCardRequest, HttpServletRequest request) {
		
		User serviceSpecialistUser = userRepository.findByNickname(maintenanceJobCardRequest.getMaintenanceJobCardIssueRequest().serviceSpecialistUserNickname)
				.orElseThrow(() -> new NotFoundException("User not found"));
		
		MaintenanceJobCard maintenanceJobCard = maintenanceJobCardRepository.findById(maintenanceJobCardRequest.getId())
				.orElseThrow(() -> new NotFoundException("Maintenance job card not found"));
		
		MaintenanceIssueType maintenanceIssueType = maintenanceIssueTypeRepository.findByNameAndCompany(maintenanceJobCardRequest.getMaintenanceJobCardIssueRequest().getMaintenanceIssueTypeName(), userService.getUserCompany(request))
				.orElseThrow(() -> new NotFoundException("Issue type not found in this company"));
		
		if(maintenanceJobCard.getStatus().equals("PENDING")) {
			maintenanceJobCard.setStatus("OPEN");
			maintenanceJobCard.setOpenedByUser(userService.getUser(request));
			maintenanceJobCard.setOpenedDateTime(dayService.getTimeStamp());
			maintenanceJobCard = maintenanceJobCardRepository.save(maintenanceJobCard);
		}else if(!maintenanceJobCard.getStatus().equals("OPEN")) {
			throw new InvalidOperationException("Can not create issue, job card is not opened");
		}
		
		MaintenanceJobCardIssue maintenanceJobCardIssue = new MaintenanceJobCardIssue();
		
		maintenanceJobCardIssue.setNo("MIS/TEMP-" + UUID.randomUUID());
		
		if(maintenanceJobCardRequest.getMaintenanceJobCardIssueRequest().noOfDays <= 0) {
			throw new InvalidOperationException("Invalid no of days, Days can be 1 or more");
		}
		
		maintenanceJobCardIssue.setMaintenanceJobCard(maintenanceJobCard);
		maintenanceJobCardIssue.setMaintenanceIssueType(maintenanceIssueType);
		maintenanceJobCardIssue.setName(maintenanceJobCardRequest.getMaintenanceJobCardIssueRequest().getName());
		maintenanceJobCardIssue.setDescription(maintenanceJobCardRequest.getMaintenanceJobCardIssueRequest().getDescription());
		
		maintenanceJobCardIssue.setPrice(maintenanceJobCardRequest.getMaintenanceJobCardIssueRequest().price);
		maintenanceJobCardIssue.setNoOfDays(maintenanceJobCardRequest.getMaintenanceJobCardIssueRequest().noOfDays);
		
		maintenanceJobCardIssue.setCreatedByUser(userService.getUser(request));
		maintenanceJobCardIssue.setCreatedDateTime(dayService.getTimeStamp());
		maintenanceJobCardIssue.setStatus("PENDING");
		maintenanceJobCardIssue.setServiceSpecialistUser(serviceSpecialistUser);
		
		maintenanceJobCardIssue = maintenanceJobCardIssueRepository.save(maintenanceJobCardIssue);
		maintenanceJobCardIssue.setNo("MIS/" + maintenanceJobCardIssue.getId());
		
		return maintenanceJobCardIssueResponseDTOMapper(maintenanceJobCardIssueRepository.save(maintenanceJobCardIssue));
	}

	@Override
	public MaintenanceJobCardIssueResponseDTO openMaintenanceJobCardIssue(MaintenanceJobCardIssueRequestDTO jobCardIssue,
			HttpServletRequest request) {
		MaintenanceJobCardIssue maintenanceJobCardIssue = maintenanceJobCardIssueRepository.findById(jobCardIssue.getId())
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCardIssue not found with ID: " + jobCardIssue.getId()));
		if(!maintenanceJobCardIssue.getNo().equals(jobCardIssue.getNo())) {
			throw new InvalidOperationException("ID and number do not match");
		}
		if(maintenanceJobCardIssue.getStatus().equals("PENDING")) {
			maintenanceJobCardIssue.setStatus("OPEN");
			maintenanceJobCardIssue.setOpenedByUser(userService.getUser(request));
			maintenanceJobCardIssue.setOpenedDateTime(dayService.getTimeStamp());
			maintenanceJobCardIssue = maintenanceJobCardIssueRepository.save(maintenanceJobCardIssue);
			
			// Here, create respective bills
			
			
			BillReceivable billReceivable = new BillReceivable();
			billReceivable.setNo(String.valueOf(Math.random()));
			billReceivable.setAmount(maintenanceJobCardIssue.getPrice());
			billReceivable.setPaid(0);
			billReceivable.setDue(maintenanceJobCardIssue.getPrice());
			billReceivable.setBranch(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getBranch());
			billReceivable.setCreatedDateTime(dayService.getTimeStamp());
			
			billReceivable.setPayStatus(PayStatus.UNPAID);
			billReceivable.setSummary("Maintenance bill for issue#: " + maintenanceJobCardIssue.getNo());
			
			billReceivable = billReceivableRepository.save(billReceivable);
			billReceivable.setNo("BR" + billReceivable.getId().toString());
			billReceivable = billReceivableRepository.save(billReceivable);
			
			MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable = new MaintenanceJobCardIssueBillReceivable();
			
			maintenanceJobCardIssueBillReceivable.setPrice(maintenanceJobCardIssue.getPrice());
			maintenanceJobCardIssueBillReceivable.setQty(1);
			maintenanceJobCardIssueBillReceivable.setDiscount(0);
			maintenanceJobCardIssueBillReceivable.setBillReceivable(billReceivable);
			maintenanceJobCardIssueBillReceivable.setMaintenanceJobCardIssue(maintenanceJobCardIssue);
			
			maintenanceJobCardIssueBillReceivable = maintenanceJobCardIssueBillReceivableRepository.save(maintenanceJobCardIssueBillReceivable);
			
			return maintenanceJobCardIssueResponseDTOMapper(maintenanceJobCardIssue);
		}else {
			throw new InvalidOperationException("Can only open a pending Job card issue");
		}	
	}

	@Override
	public MaintenanceJobCardIssueResponseDTO closeMaintenanceJobCardIssue(MaintenanceJobCardIssueRequestDTO jobCardIssue,
			HttpServletRequest request) {
		MaintenanceJobCardIssue maintenanceJobCardIssue = maintenanceJobCardIssueRepository.findById(jobCardIssue.getId())
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCard issue not found with ID: " + jobCardIssue.getId()));
		if(!maintenanceJobCardIssue.getNo().equals(jobCardIssue.getNo())) {
			throw new InvalidOperationException("ID and number do not match");
		}
		if(maintenanceJobCardIssue.getStatus().equals("OPEN")) {
			maintenanceJobCardIssue.setStatus("CLOSED");
			maintenanceJobCardIssue.setClosedByUser(userService.getUser(request));
			maintenanceJobCardIssue.setClosedDateTime(dayService.getTimeStamp());
			maintenanceJobCardIssue = maintenanceJobCardIssueRepository.save(maintenanceJobCardIssue);
			return maintenanceJobCardIssueResponseDTOMapper(maintenanceJobCardIssue);
		}else {
			throw new InvalidOperationException("Can only close an opened Job card issue");
		}
	}
	
	@Override
	public MaintenanceJobCardIssueResponseDTO removeMaintenanceJobCardIssue(MaintenanceJobCardIssueRequestDTO jobCardIssue,
			HttpServletRequest request) {
		MaintenanceJobCardIssue maintenanceJobCardIssue = maintenanceJobCardIssueRepository.findById(jobCardIssue.getId())
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCard issue not found with ID: " + jobCardIssue.getId()));
		if(!maintenanceJobCardIssue.getNo().equals(jobCardIssue.getNo())) {
			throw new InvalidOperationException("ID and number do not match");
		}
		if(maintenanceJobCardIssue.getStatus().equals("PENDING")) {
			maintenanceJobCardIssueRepository.delete(maintenanceJobCardIssue); // Delete it

			// Consider marking it as removed, instead of deleting it, not a good practice
			
			return null;
		}else {
			throw new InvalidOperationException("Can only close an opened Job card issue");
		}
	}
	
	@Override
	public MaintenanceJobCardIssueResponseDTO reopenMaintenanceJobCardIssue(MaintenanceJobCardIssueRequestDTO jobCardIssue,
			HttpServletRequest request) {
		MaintenanceJobCardIssue maintenanceJobCardIssue = maintenanceJobCardIssueRepository.findById(jobCardIssue.getId())
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCard issue not found with ID: " + jobCardIssue.getId()));
		if(!maintenanceJobCardIssue.getNo().equals(jobCardIssue.getNo())) {
			throw new InvalidOperationException("ID and number do not match");
		}
		if(maintenanceJobCardIssue.getStatus().equals("CLOSED")) {
			maintenanceJobCardIssue.setStatus("OPEN");
			maintenanceJobCardIssue = maintenanceJobCardIssueRepository.save(maintenanceJobCardIssue);
			return maintenanceJobCardIssueResponseDTOMapper(maintenanceJobCardIssue);
		}else {
			throw new InvalidOperationException("Can only re-open a closed Job card issue");
		}
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

		maintenanceJobCardIssueResponseDTO.setMaintenanceNo(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getNo());
		maintenanceJobCardIssueResponseDTO.setComments(maintenanceJobCardIssue.getComments());
		
		String regNo = "NA";
		String eqName = "NA";
		String eqType = "NA";
		
		if(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getVehicleEquipment().getRegistrationNo() != null) {
			regNo = maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getVehicleEquipment().getRegistrationNo();
		}
		if(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getVehicleEquipment().getVehicleEquipmentName() != null) {
			eqName = maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getVehicleEquipment().getVehicleEquipmentName();
		}
		if(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getVehicleEquipment().getVehicleEquipmentType().getName() != null) {
			eqType = maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getVehicleEquipment().getVehicleEquipmentType().getName();
		}
		String ref = "Reg No: " + regNo + ", " + "Name: " + eqName + ", " + "Type: " + eqType;
		maintenanceJobCardIssueResponseDTO.setEquipmentReference(ref);
		
		maintenanceJobCardIssueResponseDTO.setOwnerName(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getOwnerFirstName() + " " + maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getOwnerLastName());
		if(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getChasisNo() != null) {
			maintenanceJobCardIssueResponseDTO.setChasisNo(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getChasisNo());
		}else {
			maintenanceJobCardIssueResponseDTO.setChasisNo("");
		}
		if(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getOwnerPhoneNo() != null) {
			maintenanceJobCardIssueResponseDTO.setPhoneNo(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().getOwnerPhoneNo());
		}else {
			maintenanceJobCardIssueResponseDTO.setPhoneNo("");
		}
		
		if(maintenanceJobCardIssue.getMaintenanceJobCard().getMaintenance().isHasKeys() == true) {
			maintenanceJobCardIssueResponseDTO.setKeys("YES");
		}else {
			maintenanceJobCardIssueResponseDTO.setKeys("NO");
		}
		
		
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

	@Override
	public List<MaintenanceJobCardIssueResponseDTO> getMyJobs(HttpServletRequest request) {
		List<MaintenanceJobCardIssue> maintenanceJobCardIssues = maintenanceJobCardIssueRepository.findAllByServiceSpecialistUserAndStatus(userService.getUser(request), "OPEN");
		List<MaintenanceJobCardIssueResponseDTO> myJobs = new ArrayList<>();
		for(MaintenanceJobCardIssue maintenanceJobCardIssue : maintenanceJobCardIssues) {
			MaintenanceJobCardIssueResponseDTO job = maintenanceJobCardIssueResponseDTOMapper(maintenanceJobCardIssue);
			myJobs.add(job);
		}
		return myJobs;
	}
	
	
	@Override
	public MaintenanceJobCardIssueResponseDTO saveComments(MaintenanceJobCardIssueRequestDTO jobCardIssue,
			HttpServletRequest request) {
		MaintenanceJobCardIssue maintenanceJobCardIssue = maintenanceJobCardIssueRepository.findById(jobCardIssue.getId())
			    .orElseThrow(() -> new NotFoundException("MaintenanceJobCard issue not found with ID: " + jobCardIssue.getId()));
//		if(!maintenanceJobCardIssue.getNo().equals(jobCardIssue.getNo())) {
//			throw new InvalidOperationException("ID and number do not match");
//		}
		
		// think of validating later on, if I get time
		maintenanceJobCardIssue.setComments(jobCardIssue.getComments());
		maintenanceJobCardIssue = maintenanceJobCardIssueRepository.save(maintenanceJobCardIssue);
		return maintenanceJobCardIssueResponseDTOMapper(maintenanceJobCardIssue);		
	}

	
	
	
	

}
