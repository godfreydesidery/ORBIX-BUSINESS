package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipment;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipmentRepository;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipmentType;
import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipmentTypeRepository;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.finance.InvoiceReceivableDetailRepository;
import com.orbix.api.modules.finance.InvoiceReceivableRepository;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MaintenanceServiceController implements MaintenanceService {
	
	private final MaintenanceRepository maintenanceRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final MaintenanceJobCardRepository maintenanceJobCardRepository;
	
	private final VehicleEquipmentRepository vehicleEquipmentRepository;
	
	private final MaintenanceJobCardService maintenanceJobCardService;
	
//	private final MaintenanceZoneRepository maintenanceZoneRepository;
	
	private final VehicleEquipmentTypeRepository vehicleEquipmentTypeRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	
	private final MaintenanceJobCardIssueBillReceivableRepository maintenanceJobCardIssueBillReceivableRepository;
//	private final MaintenanceServiceBillReceivableRepository maintenanceServiceBillReceivableRepository;
//	private final MaintenanceInvoiceReceivableRepository maintenanceInvoiceReceivableRepository;
	
	private final InvoiceReceivableRepository invoiceReceivableRepository;
	private final InvoiceReceivableDetailRepository invoiceReceivableDetailRepository;

	@Override
	public List<MaintenanceResponseDTO> getAllMaintenances(HttpServletRequest request) {
		List<Maintenance> maintenances = maintenanceRepository.findAll();
		List<MaintenanceResponseDTO> maintenanceResponses = new ArrayList<>();

		for(Maintenance maintenance : maintenances) {
			maintenanceResponses.add(maintenanceResponseDTOMapper(maintenance));
		}		
		return maintenanceResponses;
	}

	@Override
	@Transactional // Because it fetches lazy loaded collections
	public List<MaintenanceResponseDTO> getAllPendingOrCheckedInMaintenances(HttpServletRequest request) {
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");
		
		List<Maintenance> maintenances = maintenanceRepository.findAllByStatusIn(statuses);
		List<MaintenanceResponseDTO> maintenanceResponses = new ArrayList<>();

		for(Maintenance maintenance : maintenances) {
			maintenanceResponses.add(maintenanceResponseDTOMapper(maintenance));					
		}		
		return maintenanceResponses;
	}
	
	@Override
	@Transactional // Because it fetches lazy loaded collections
	public List<MaintenanceResponseDTO> getAllCheckedInMaintenancesWithOpenJobs(HttpServletRequest request) {
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");  //Consider removing this status
		statuses.add("CHECKED-IN");
		
		List<Maintenance> maintenances = maintenanceRepository.findAllByStatusInAndOpenMaintenanceJobCardIssues(statuses);
		
		List<MaintenanceResponseDTO> maintenanceResponses = new ArrayList<>();

		for(Maintenance maintenance : maintenances) {
			maintenanceResponses.add(maintenanceResponseDTOMapper(maintenance));					
		}		
		return maintenanceResponses;
	}
	
	@Override
	@Transactional // Because it fetches lazy loaded collections
	public List<MaintenanceResponseDTO> getAllCheckedInMaintenancesWithOpenJobsAndMine(HttpServletRequest request) {
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");  //Consider removing this status
		statuses.add("CHECKED-IN");
		
		User user = userService.getUser(request);
		
		List<Maintenance> maintenances = maintenanceRepository.findAllByStatusInAndOpenMaintenanceJobCardIssuesAndServiceSpecialistUser(statuses, user);
		
		//List<Maintenance> maintenances = maintenanceRepository.findAllByStatusInAndOpenMaintenanceJobCardIssues(statuses);
		List<MaintenanceResponseDTO> maintenanceResponses = new ArrayList<>();

		for(Maintenance maintenance : maintenances) {
			maintenanceResponses.add(maintenanceResponseDTOMapper(maintenance));					
		}		
		return maintenanceResponses;
	}
	
	@Override
	@Transactional // Because it fetches lazy loaded collections
	public List<MaintenanceResponseDTO> getAllCheckedInMaintenancesWithClosedJobsAndMine(HttpServletRequest request) {
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");  //Consider removing this status
		statuses.add("CHECKED-IN");
		statuses.add("CHECKED-OUT");
		
		User user = userService.getUser(request);
		
		LocalDateTime closedSince = LocalDateTime.now().minusHours(24);
		List<Maintenance> maintenances = maintenanceRepository.findAllByStatusInAndClosedMaintenanceJobCardIssuesAndServiceSpecialistUser(statuses, user, closedSince);
		
		//List<Maintenance> maintenances = maintenanceRepository.findAllByStatusInAndOpenMaintenanceJobCardIssues(statuses);
		List<MaintenanceResponseDTO> maintenanceResponses = new ArrayList<>();

		for(Maintenance maintenance : maintenances) {
			maintenanceResponses.add(maintenanceResponseDTOMapper(maintenance));					
		}		
		return maintenanceResponses;
	}

	@Override
	public List<MaintenanceResponseDTO> getAllCleared(HttpServletRequest request) {
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		List<Maintenance> maintenances = maintenanceRepository.findAllByStatusIn(statuses);
		List<MaintenanceResponseDTO> maintenanceResponses = new ArrayList<>();

		for(Maintenance maintenance : maintenances) {
			
			boolean cleared = true;
			
			List<MaintenanceJobCardIssueBillReceivable> maintenanceJobCardIssueBillReceivables = maintenanceJobCardIssueBillReceivableRepository.findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(maintenance);
			if(!maintenanceJobCardIssueBillReceivables.isEmpty() && cleared == true) {
				for(MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable : maintenanceJobCardIssueBillReceivables) {
					if(!maintenanceJobCardIssueBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
						cleared = false;
						break;
					}
				}
			}
			
			if(cleared) maintenanceResponses.add(maintenanceResponseDTOMapper(maintenance));	
							
		}		
		return maintenanceResponses;
	}

	@Override
	public List<MaintenanceResponseDTO> getTodayCheckedOut(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MaintenanceResponseDTO> getRecentCheckedOut(HttpServletRequest request) {
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-OUT");
		
		// Calculate the range
		LocalDateTime now = dayService.getTimeStamp(); //LocalDateTime.now();
		LocalDateTime before = now.minusHours(24);

		List<Maintenance> maintenances = maintenanceRepository.findAllByStatusInAndCheckedOutDateTimeBetween(statuses, before, now);
		
		//List<Maintenance> maintenances = maintenanceRepository.findAllByStatusInAndCheckedOutBetween(statuses, LocalDateTime.now().);
		List<MaintenanceResponseDTO> maintenanceResponses = new ArrayList<>();

		for(Maintenance maintenance : maintenances) {
			
			boolean cleared = true;
			
			List<MaintenanceJobCardIssueBillReceivable> maintenanceJobCardIssueBillReceivables = maintenanceJobCardIssueBillReceivableRepository.findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(maintenance);
			if(!maintenanceJobCardIssueBillReceivables.isEmpty() && cleared == true) {
				for(MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable : maintenanceJobCardIssueBillReceivables) {
					if(!maintenanceJobCardIssueBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
						cleared = false;
						break;
					}
				}
			}
			
			if(cleared) maintenanceResponses.add(maintenanceResponseDTOMapper(maintenance));	
							
		}		
		return maintenanceResponses;
	}

	@Override
	public List<MaintenanceResponseDTO> getAllCheckedInMaintenances(HttpServletRequest request) {
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		List<Maintenance> maintenances = maintenanceRepository.findAllByStatusIn(statuses);
		List<MaintenanceResponseDTO> maintenanceResponses = new ArrayList<>();

		for(Maintenance maintenance : maintenances) {
			maintenanceResponses.add(maintenanceResponseDTOMapper(maintenance));					
		}		
		return maintenanceResponses;
	}

	@Override
	public MaintenanceResponseDTO get(Long id, HttpServletRequest request) {
		Optional<Maintenance> maintenance_ = maintenanceRepository.findById(id);
		if(maintenance_.isEmpty()) {
			throw new NotFoundException("Maintenance not found");
		}		
		return maintenanceResponseDTOMapper(maintenance_.get());
	}

	@Override
	public MaintenanceResponseDTO createMaintenance(MaintenanceRequestDTO maintenanceRequest,
			HttpServletRequest request) {
		/**Validate data*/		
		if(!validateMaintenanceData(maintenanceRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) {
			throw new NotFoundException("Company not found");
		}
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}
		
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(maintenanceRequest.getVehicleEquipmentTypeName(), company_.get());
		if(vehicleEquipmentType_.isEmpty()) {
			throw new NotFoundException("Vehicle or  a equipment type not found");
		}
		if(vehicleEquipmentType_.get().getCompany().getId() != company_.get().getId()) {
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		}
		
		Optional<VehicleEquipment> vehicleEquipment_ = vehicleEquipmentRepository.findById(maintenanceRequest.getVehicleEquipmentId());
		if(vehicleEquipment_.isEmpty()) throw new NotFoundException("Vehicle and equipment not found");
				
		Maintenance maintenance = new Maintenance();
		maintenance.setNo(String.valueOf(Math.random()));
		maintenance.setOwnerFirstName(maintenanceRequest.getOwnerFirstName());
		maintenance.setOwnerMiddleName(maintenanceRequest.getOwnerMiddleName());
		maintenance.setOwnerLastName(maintenanceRequest.getOwnerLastName());
		maintenance.setOwnerCompanyName(maintenanceRequest.getOwnerCompanyName());
		maintenance.setOwnerIdNo(maintenanceRequest.getOwnerIdNo());
		maintenance.setOwnerIdType(maintenanceRequest.getOwnerIdType());
		maintenance.setOwnerPhoneNo(maintenanceRequest.getOwnerPhoneNo());
		maintenance.setOwnerEmail(maintenanceRequest.getOwnerEmail());
		maintenance.setOwnerAddress(maintenanceRequest.getOwnerAddress());
		maintenance.setRegistrationNo(maintenanceRequest.getRegistrationNo());
		maintenance.setChasisNo(maintenanceRequest.getChasisNo());
		maintenance.setCardNo(maintenanceRequest.getCardNo());
		maintenance.setLeftFrontLamp(maintenanceRequest.isLeftFrontLamp());
		maintenance.setRightFrontLamp(maintenanceRequest.isRightFrontLamp());
		maintenance.setLeftRearLamp(maintenanceRequest.isLeftRearLamp());
		maintenance.setRightRearLamp(maintenanceRequest.isRightRearLamp());
		maintenance.setLeftSideMirror(maintenanceRequest.isLeftSideMirror());
		maintenance.setRightSideMirror(maintenanceRequest.isRightSideMirror());
		maintenance.setLeftWiper(maintenanceRequest.isLeftWiper());
		maintenance.setRightWiper(maintenanceRequest.isRightWiper());
		maintenance.setBackWiper(maintenanceRequest.isBackWiper());
		maintenance.setFuelCap(maintenanceRequest.isFuelCap());
		maintenance.setSpareTire(maintenanceRequest.isSpareTire());
		maintenance.setBattery(maintenanceRequest.isBattery());
		maintenance.setStarter(maintenanceRequest.isStarter());
		maintenance.setAerial(maintenanceRequest.isAerial());
		maintenance.setWheelCap(maintenanceRequest.isWheelCap());
		maintenance.setRoundMirror(maintenanceRequest.isRoundMirror());
		maintenance.setTireIndicator(maintenanceRequest.isTireIndicator());
		maintenance.setHasKeys(true);
		maintenance.setDeviceStatus(true);
		
		maintenance.setComments(maintenanceRequest.getComments());
		
		//maintenance.setImage(maintenanceRequest.getImage());
		maintenance.setStatus("PENDING");
		maintenance.setVehicleEquipmentType(vehicleEquipmentType_.get());
		
		maintenance.setVehicleEquipmentName(vehicleEquipmentType_.get().getName()); // Look here later
		maintenance.setVehicleEquipmentColor(maintenanceRequest.getVehicleEquipmentColor());
		
		maintenance.setVehicleEquipmentCategory("IN-TRANSIT");
		maintenance.setVehicleEquipment(vehicleEquipment_.get());
		
		
		
		maintenance.setBranch(branch_.get());

		maintenance.setCreatedByUser(userService.getUser(request));
		maintenance.setCreatedDateTime(dayService.getTimeStamp());
		
		maintenance = maintenanceRepository.save(maintenance);
		/**Create  maintenance no*/
		maintenance.setNo("MTC/"+ maintenance.getId().toString());
		maintenance = maintenanceRepository.save(maintenance);
		
		return maintenanceResponseDTOMapper(maintenance);
	}

	@Override
	public MaintenanceResponseDTO updateMaintenance(MaintenanceRequestDTO maintenanceRequest,
			HttpServletRequest request) {
		Optional<Maintenance> maintenance_ = maintenanceRepository.findById(maintenanceRequest.getId());
		if(maintenance_.isEmpty()) throw new NotFoundException("Maintenance not found in database");
			
		if(!maintenance_.get().getStatus().equals("PENDING")) throw new NotFoundException("Can not update, only pending maintenance can be updated");
			
		if(!validateMaintenanceData(maintenanceRequest)) throw new InvalidEntryException("Could not validate data");
			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(maintenanceRequest.getVehicleEquipmentTypeName(), company_.get());
		if(vehicleEquipmentType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");
			
		if(vehicleEquipmentType_.get().getCompany().getId() != company_.get().getId()) 
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		
		Maintenance maintenance = maintenance_.get();
		maintenance.setOwnerFirstName(maintenanceRequest.getOwnerFirstName());
		maintenance.setOwnerMiddleName(maintenanceRequest.getOwnerMiddleName());
		maintenance.setOwnerLastName(maintenanceRequest.getOwnerLastName());
		maintenance.setOwnerCompanyName(maintenanceRequest.getOwnerCompanyName());
		maintenance.setOwnerIdNo(maintenanceRequest.getOwnerIdNo());
		maintenance.setOwnerIdType(maintenanceRequest.getOwnerIdType());
		maintenance.setOwnerPhoneNo(maintenanceRequest.getOwnerPhoneNo());
		maintenance.setOwnerEmail(maintenanceRequest.getOwnerEmail());
		maintenance.setOwnerAddress(maintenanceRequest.getOwnerAddress());
		maintenance.setRegistrationNo(maintenanceRequest.getRegistrationNo());
		maintenance.setChasisNo(maintenanceRequest.getChasisNo());
		maintenance.setCardNo(maintenanceRequest.getCardNo());
		maintenance.setLeftFrontLamp(maintenanceRequest.isLeftFrontLamp());
		maintenance.setRightFrontLamp(maintenanceRequest.isRightFrontLamp());
		maintenance.setLeftRearLamp(maintenanceRequest.isLeftRearLamp());
		maintenance.setRightRearLamp(maintenanceRequest.isRightRearLamp());
		maintenance.setLeftSideMirror(maintenanceRequest.isLeftSideMirror());
		maintenance.setRightSideMirror(maintenanceRequest.isRightSideMirror());
		maintenance.setLeftWiper(maintenanceRequest.isLeftWiper());
		maintenance.setRightWiper(maintenanceRequest.isRightWiper());
		maintenance.setBackWiper(maintenanceRequest.isBackWiper());
		maintenance.setFuelCap(maintenanceRequest.isFuelCap());
		maintenance.setSpareTire(maintenanceRequest.isSpareTire());
		maintenance.setBattery(maintenanceRequest.isBattery());
		maintenance.setStarter(maintenanceRequest.isStarter());
		maintenance.setAerial(maintenanceRequest.isAerial());
		maintenance.setWheelCap(maintenanceRequest.isWheelCap());
		maintenance.setRoundMirror(maintenanceRequest.isRoundMirror());
		maintenance.setTireIndicator(maintenanceRequest.isTireIndicator());
		maintenance.setHasKeys(maintenanceRequest.isHasKeys());
		maintenance.setDeviceStatus(maintenanceRequest.isDeviceStatus());
		maintenance.setVehicleEquipmentType(vehicleEquipmentType_.get());
		maintenance.setVehicleEquipmentCategory(maintenanceRequest.getVehicleEquipmentCategory());
		
		maintenance.setVehicleEquipmentName(vehicleEquipmentType_.get().getName()); // Look here later
		maintenance.setVehicleEquipmentColor(maintenanceRequest.getVehicleEquipmentColor());
		
		maintenance.setComments(maintenanceRequest.getComments());
			
		maintenance = maintenanceRepository.save(maintenance);
		
		return maintenanceResponseDTOMapper(maintenance);
	}

	@Override
	public MaintenanceResponseDTO checkIn(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request) {
		Optional<Maintenance> maintenance_ = maintenanceRepository.findById(maintenanceRequest.getId());
		
		if(maintenance_.isEmpty()) 
			throw new NotFoundException("Maintenance not found");			
		if(maintenance_.get().getBranch().getId() != userService.getUser(request).getBranch().getId()) 
			throw new InvalidOperationException("Checking in can only be done by a branch user");
		if(!maintenance_.get().getStatus().equals("PENDING")) 
			throw new InvalidOperationException("Can not check in, only a pending maintenance can be checked in");
		
		//now check in vehicle, if it has a pending status
		
		Maintenance maintenance = maintenance_.get();
				
		maintenance.setStatus("CHECKED-IN");
		maintenance.setCardNo(maintenanceRequest.getCardNo());
		maintenance.setHasKeys(maintenanceRequest.isHasKeys());
		maintenance.setCheckedInByUser(userService.getUser(request));
		maintenance.setCheckedInDateTime(dayService.getTimeStamp());
			
		maintenance = maintenanceRepository.save(maintenance);
		
		return maintenanceResponseDTOMapper(maintenance);
	}

	@Override
	public MaintenanceResponseDTO checkOut(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request) {
		Optional<Maintenance> maintenance_ = maintenanceRepository.findById(maintenanceRequest.getId());
		if(maintenance_.isEmpty()) throw new NotFoundException("Maintenance not found in database");
			
		if(!maintenance_.get().getStatus().equals("CHECKED-IN")) throw new NotFoundException("Can not check out, only checked in maintenance can be checked out");
			
//		if(!validateMaintenanceData(maintenanceRequest)) throw new InvalidEntryException("Could not validate data");
			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
//		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(maintenanceRequest.getVehicleEquipmentTypeName(), company_.get());
//		if(vehicleEquipmentType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");
		
		
		List<MaintenanceJobCardIssueBillReceivable> maintenanceJobCardIssueBillReceivables = maintenanceJobCardIssueBillReceivableRepository.findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(maintenance_.get());
		for(MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable : maintenanceJobCardIssueBillReceivables) {
			if(maintenanceJobCardIssueBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.UNPAID)) {
				throw new InvalidOperationException("Can not check out, bills  not cleared");
			}
		}
		
		Maintenance maintenance = maintenance_.get();
		maintenance.setStatus("CHECKED-OUT");
		maintenance.setCardNo(maintenanceRequest.getCardNo());
		maintenance.setCheckedOutByUser(userService.getUser(request));
		maintenance.setCheckedOutDateTime(dayService.getTimeStamp());
		
		maintenance = maintenanceRepository.save(maintenance);
		
		return maintenanceResponseDTOMapper(maintenance);
	}
	
	@Override
	public List<MaintenanceJobCardIssueBillReceivableResponseDTO> getMaintenanceJobCardIssueBillReceivables(
			Long id, HttpServletRequest request) {
		Optional<Maintenance> maintenance_ = maintenanceRepository.findById(id);
		if(maintenance_.isEmpty()) {
			throw new NotFoundException("Maintenance not found");
		}
		
		List<MaintenanceJobCardIssueBillReceivable> maintenanceJobCardIssueBillReceivables = maintenanceJobCardIssueBillReceivableRepository.findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(maintenance_.get());
		
		List<MaintenanceJobCardIssueBillReceivableResponseDTO> maintenanceJobCardIssueBillReceivableResponses = new ArrayList<>();
		
		for(MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable : maintenanceJobCardIssueBillReceivables) {
			maintenanceJobCardIssueBillReceivableResponses.add(maintenanceJobCardIssueBillReceivableDTOMapper(maintenanceJobCardIssueBillReceivable));
		}
		if (maintenanceJobCardIssueBillReceivableResponses.isEmpty()) return null;
		
		return maintenanceJobCardIssueBillReceivableResponses;
	}
	
	private MaintenanceResponseDTO maintenanceResponseDTOMapper(Maintenance maintenance) {
		MaintenanceResponseDTO maintenanceResponse = new MaintenanceResponseDTO();
		
		maintenanceResponse.setId(String.valueOf(maintenance.getId()));
		maintenanceResponse.setNo(maintenance.getNo());
		maintenanceResponse.setOwnerFirstName(maintenance.getOwnerFirstName());
		maintenanceResponse.setOwnerMiddleName(maintenance.getOwnerMiddleName());
		maintenanceResponse.setOwnerLastName(maintenance.getOwnerLastName());
		maintenanceResponse.setOwnerCompanyName(maintenance.getOwnerCompanyName());
		maintenanceResponse.setOwnerIdNo(maintenance.getOwnerIdNo());
		maintenanceResponse.setOwnerIdType(maintenance.getOwnerIdType());
		maintenanceResponse.setOwnerPhoneNo(maintenance.getOwnerPhoneNo());
		maintenanceResponse.setOwnerEmail(maintenance.getOwnerEmail());
		maintenanceResponse.setOwnerAddress(maintenance.getOwnerAddress());
		maintenanceResponse.setRegistrationNo(maintenance.getRegistrationNo());
		maintenanceResponse.setChasisNo(maintenance.getChasisNo());
		maintenanceResponse.setCardNo(maintenance.getCardNo());
		maintenanceResponse.setLeftFrontLamp( maintenance.isLeftFrontLamp() ? "1" : "0");
		maintenanceResponse.setRightFrontLamp(maintenance.isRightFrontLamp() ? "1" : "0");
		maintenanceResponse.setLeftRearLamp(maintenance.isLeftRearLamp() ? "1" : "0");
		maintenanceResponse.setRightRearLamp(maintenance.isRightRearLamp() ? "1" : "0");
		maintenanceResponse.setLeftSideMirror(maintenance.isLeftSideMirror() ? "1" : "0");
		maintenanceResponse.setRightSideMirror(maintenance.isRightSideMirror() ? "1" : "0");
		maintenanceResponse.setLeftWiper(maintenance.isLeftWiper() ? "1" : "0");
		maintenanceResponse.setRightWiper(maintenance.isRightWiper() ? "1" : "0");
		maintenanceResponse.setBackWiper(maintenance.isBackWiper() ? "1" : "0");
		maintenanceResponse.setFuelCap(maintenance.isFuelCap() ? "1" : "0");
		maintenanceResponse.setSpareTire(maintenance.isSpareTire() ? "1" : "0");
		maintenanceResponse.setBattery(maintenance.isBattery() ? "1" : "0");
		maintenanceResponse.setStarter(maintenance.isStarter() ? "1" : "0");
		maintenanceResponse.setAerial(maintenance.isAerial() ? "1" : "0");
		maintenanceResponse.setWheelCap(maintenance.isWheelCap() ? "1" : "0");
		maintenanceResponse.setRoundMirror(maintenance.isRoundMirror() ? "1" : "0");
		maintenanceResponse.setTireIndicator(maintenance.isTireIndicator() ? "1" : "0");
		maintenanceResponse.setHasKeys(maintenance.isHasKeys() ? "1" : "0");
		maintenanceResponse.setDeviceStatus(maintenance.isDeviceStatus() ? "1" : "0");
		maintenanceResponse.setVehicleEquipmentCategory(maintenance.getVehicleEquipmentCategory());
		maintenanceResponse.setVehicleEquipmentName(maintenance.getVehicleEquipmentName());
		maintenanceResponse.setVehicleEquipmentColor(maintenance.getVehicleEquipmentColor());
		
		maintenanceResponse.setComments(maintenance.getComments());
		//maintenance.setImage(maintenanceRequest.getImage());
		maintenanceResponse.setStatus(maintenance.getStatus());
		//maintenanceResponse.setCompanyId(maintenance.getCompany().getId().toString());
		maintenanceResponse.setBranchId(maintenance.getBranch().getId().toString());
		
		maintenanceResponse.setVehicleEquipmentTypeName(maintenance.getVehicleEquipmentType().getName());
		
		if(maintenance.getStatus().equals("CHECKED-OUT")) {
			List<ServiceBillItem> items = new ArrayList<>();
		
			List<MaintenanceJobCardIssueBillReceivable> pbs = maintenanceJobCardIssueBillReceivableRepository.findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(maintenance);
			int sn = 1;
			for(MaintenanceJobCardIssueBillReceivable pbr : pbs) {
				ServiceBillItem sbi = new ServiceBillItem();
				sbi.setSn(sn);
				sbi.setItem(pbr.getBillReceivable().getSummary());
				sbi.setQty(pbr.getQty());
				sbi.setAmount(pbr.getBillReceivable().getAmount());
				sbi.setPayStatus(pbr.getBillReceivable().getPayStatus().toString());
				items.add(sbi);
			}			
			maintenanceResponse.setServiceBillItems(items);
		}
			
		return maintenanceResponse;
	}
	
	private MaintenanceJobCardIssueBillReceivableResponseDTO maintenanceJobCardIssueBillReceivableDTOMapper(MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable) {
		
		MaintenanceJobCardIssueBillReceivableResponseDTO maintenanceJobCardIssueBillReceivableResponseDTO = new MaintenanceJobCardIssueBillReceivableResponseDTO();
		
		maintenanceJobCardIssueBillReceivableResponseDTO.setId(maintenanceJobCardIssueBillReceivable.getId().toString());
		maintenanceJobCardIssueBillReceivableResponseDTO.setQty(String.valueOf(maintenanceJobCardIssueBillReceivable.getQty()));
		maintenanceJobCardIssueBillReceivableResponseDTO.setPrice(String.valueOf(maintenanceJobCardIssueBillReceivable.getPrice()));
		maintenanceJobCardIssueBillReceivableResponseDTO.setBillingType(maintenanceJobCardIssueBillReceivable.getBillingType());
		maintenanceJobCardIssueBillReceivableResponseDTO.setDiscount(String.valueOf(maintenanceJobCardIssueBillReceivable.getDiscount()));
		maintenanceJobCardIssueBillReceivableResponseDTO.setMaintenanceId(maintenanceJobCardIssueBillReceivable.getMaintenanceJobCardIssue().getMaintenanceJobCard().getMaintenance().getId().toString());
		maintenanceJobCardIssueBillReceivableResponseDTO.setAmount(String.valueOf(((maintenanceJobCardIssueBillReceivable.getPrice() * maintenanceJobCardIssueBillReceivable.getQty()) - maintenanceJobCardIssueBillReceivable.getDiscount())));
		maintenanceJobCardIssueBillReceivableResponseDTO.setPayStatus(maintenanceJobCardIssueBillReceivable.getBillReceivable().getPayStatus().toString());
				
		return maintenanceJobCardIssueBillReceivableResponseDTO;
		
	}
	
	boolean validateMaintenanceData(MaintenanceRequestDTO maintenanceRequest) {		
		return true;
	}

	@Override
	public MaintenanceJobCardResponseDTO createMaintenanceJobCard(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request) {
		// Find for any job card in the maintenance
		
		Maintenance maintenance = maintenanceRepository.findById(maintenanceRequest.getId())
				.orElseThrow(() -> new NotFoundException("Maintenance not found"));
		
		if(!maintenance.getStatus().equals("CHECKED-IN")) {
			throw new InvalidOperationException("Only allowed for checked in maintenances");
		}
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("OPEN");
		
		Optional<MaintenanceJobCard> maintenanceJobCard_ = maintenanceJobCardRepository
			    .findFirstByMaintenanceAndStatusIn(maintenance, statuses);
		
		if(maintenanceJobCard_.isPresent()) {
			return maintenanceJobCardService.showMaintenanceJobCard(maintenanceJobCard_.get(), null);
		}else {
			return maintenanceJobCardService.createMaintenanceJobCard(maintenanceRequest, request);
		}		
	}
	
	@Override
	public MaintenanceJobCardResponseDTO loadMyJobCard(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request) {
		// Find for any job card in the maintenance
		
		Maintenance maintenance = maintenanceRepository.findById(maintenanceRequest.getId())
				.orElseThrow(() -> new NotFoundException("Maintenance not found"));
		
		if(!maintenance.getStatus().equals("CHECKED-IN")) {
			throw new InvalidOperationException("Only allowed for checked in maintenances");
		}
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("OPEN");
		
		Optional<MaintenanceJobCard> maintenanceJobCard_ = maintenanceJobCardRepository
			    .findFirstByMaintenanceAndStatusIn(maintenance, statuses);
		
		if(maintenanceJobCard_.isPresent()) {
			return maintenanceJobCardService.showMaintenanceJobCard(maintenanceJobCard_.get(), userService.getUser(request));
		}else {
			return null;
		}		
	}
	
	@Override
	public MaintenanceJobCardResponseDTO loadMyClosedJobCard(MaintenanceRequestDTO maintenanceRequest, HttpServletRequest request) {
		// Find for any job card in the maintenance
		
		Maintenance maintenance = maintenanceRepository.findById(maintenanceRequest.getId())
				.orElseThrow(() -> new NotFoundException("Maintenance not found"));
		
//		if(!maintenance.getStatus().equals("CHECKED-IN")) {
//			throw new InvalidOperationException("Only allowed for checked in maintenances");
//		}
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("OPEN");
		statuses.add("CLOSED");
		
		Optional<MaintenanceJobCard> maintenanceJobCard_ = maintenanceJobCardRepository
			    .findFirstByMaintenanceAndStatusIn(maintenance, statuses);
		
		if(maintenanceJobCard_.isPresent()) {
			return maintenanceJobCardService.showMaintenanceJobCard(maintenanceJobCard_.get(), userService.getUser(request));
		}else {
			return null;
		}		
	}

	@Override
	public MaintenanceJobCardResponseDTO openMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MaintenanceJobCardResponseDTO closeMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MaintenanceJobCardResponseDTO reopenMaintenanceJobCard(MaintenanceJobCard maintenanceJobCard,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
}

@Data
class ServiceBillItem {
	int sn;
	String item;
	double qty;
	String payStatus;
	double amount;
}
