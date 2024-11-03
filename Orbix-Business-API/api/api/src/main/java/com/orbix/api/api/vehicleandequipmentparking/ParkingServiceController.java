package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.finance.InvoiceReceivable;
import com.orbix.api.modules.finance.InvoiceReceivableDetail;
import com.orbix.api.modules.finance.InvoiceReceivableDetailRepository;
import com.orbix.api.modules.finance.InvoiceReceivableRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ParkingServiceController implements ParkingService {
	
	private final ParkingRepository parkingRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final VehicleEquipmentRepository vehicleEquipmentRepository;
	
	private final ParkingZoneRepository parkingZoneRepository;
	
	private final VehicleEquipmentTypeRepository vehicleEquipmentTypeRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	private final ParkingServiceBillReceivableRepository parkingServiceBillReceivableRepository;
	private final ParkingInvoiceReceivableRepository parkingInvoiceReceivableRepository;
	
	private final InvoiceReceivableRepository invoiceReceivableRepository;
	private final InvoiceReceivableDetailRepository invoiceReceivableDetailRepository;
	
		
	

	@Override
	public List<ParkingResponseDTO> getAllParkings(HttpServletRequest request) {
		List<Parking> parkings = parkingRepository.findAll();
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking));					
		}		
		return parkingResponses;
	}
	
	@Override
	public List<ParkingResponseDTO> getAllPendingOrCheckedInParkings(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");
		
		List<Parking> parkings = parkingRepository.findAllByStatusIn(statuses);
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking));					
		}		
		return parkingResponses;
	}
	
	@Override
	public List<ParkingResponseDTO> getAllCheckedInParkings(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		List<Parking> parkings = parkingRepository.findAllByStatusIn(statuses);
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking));					
		}		
		return parkingResponses;
	}

	@Override
	public ParkingResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<Parking> parking_ = parkingRepository.findById(id);
	if(parking_.isEmpty()) {
		throw new NotFoundException("Parking not found");
	}		
	return parkingResponseDTOMapper(parking_.get());	
	}
	
	@Override
	public List<ParkingBillReceivableResponseDTO> getParkingBillReceivables(Long id, HttpServletRequest request) {		
		Optional<Parking> parking_ = parkingRepository.findById(id);
		if(parking_.isEmpty()) {
			throw new NotFoundException("Parking not found");
		}
		
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParking(parking_.get());
		
		List<ParkingBillReceivableResponseDTO> parkingBillReceivableResponses = new ArrayList<>();
		
		for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
			parkingBillReceivableResponses.add(parkingBillReceivableDTOMapper(parkingBillReceivable));
		}
		if (parkingBillReceivableResponses.isEmpty()) return null;	
		
		return parkingBillReceivableResponses;
	}

	@Override
	public ParkingResponseDTO createParking(ParkingRequestDTO parkingRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateParkingData(parkingRequest)) {
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
		
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(parkingRequest.getVehicleEquipmentTypeName(), company_.get());
		if(vehicleEquipmentType_.isEmpty()) {
			throw new NotFoundException("Vehicle or  a equipment type not found");
		}
		if(vehicleEquipmentType_.get().getCompany().getId() != company_.get().getId()) {
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		}
		
		Optional<VehicleEquipment> vehicleEquipment_ = vehicleEquipmentRepository.findById(parkingRequest.getVehicleEquipmentId());
		if(vehicleEquipment_.isEmpty()) throw new NotFoundException("Vehicle and equipment not found");
		
		Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findByNameAndBranch(parkingRequest.getParkingZoneName(), branch_.get());
		
		
		Parking parking = new Parking();
		parking.setNo(String.valueOf(Math.random()));
		parking.setOwnerFirstName(parkingRequest.getOwnerFirstName());
		parking.setOwnerMiddleName(parkingRequest.getOwnerMiddleName());
		parking.setOwnerLastName(parkingRequest.getOwnerLastName());
		parking.setOwnerCompanyName(parkingRequest.getOwnerCompanyName());
		parking.setOwnerIdNo(parkingRequest.getOwnerIdNo());
		parking.setOwnerIdType(parkingRequest.getOwnerIdType());
		parking.setOwnerPhoneNo(parkingRequest.getOwnerPhoneNo());
		parking.setOwnerEmail(parkingRequest.getOwnerEmail());
		parking.setOwnerAddress(parkingRequest.getOwnerAddress());
		parking.setAgentName(parkingRequest.getAgentName());
		parking.setAgentAddress(parkingRequest.getAgentAddress());
		parking.setAgentPhoneNo(parkingRequest.getAgentPhoneNo());
		parking.setAgentEmail(parkingRequest.getAgentEmail());
		parking.setTformNumber(parkingRequest.getTformNumber());
		parking.setRegistrationNo(parkingRequest.getRegistrationNo());
		parking.setChasisNo(parkingRequest.getChasisNo());
		parking.setCardNo(parkingRequest.getCardNo());
		parking.setLeftFrontLamp(parkingRequest.isLeftFrontLamp());
		parking.setRightFrontLamp(parkingRequest.isRightFrontLamp());
		parking.setLeftRearLamp(parkingRequest.isLeftRearLamp());
		parking.setRightRearLamp(parkingRequest.isRightRearLamp());
		parking.setLeftSideMirror(parkingRequest.isLeftSideMirror());
		parking.setRightSideMirror(parkingRequest.isRightSideMirror());
		parking.setLeftWiper(parkingRequest.isLeftWiper());
		parking.setRightWiper(parkingRequest.isRightWiper());
		parking.setBackWiper(parkingRequest.isBackWiper());
		parking.setFuelCap(parkingRequest.isFuelCap());
		parking.setSpareTire(parkingRequest.isSpareTire());
		parking.setBattery(parkingRequest.isBattery());
		parking.setStarter(parkingRequest.isStarter());
		parking.setAerial(parkingRequest.isAerial());
		parking.setWheelCap(parkingRequest.isWheelCap());
		parking.setRoundMirror(parkingRequest.isRoundMirror());
		parking.setTireIndicator(parkingRequest.isTireIndicator());
		//parking.setImage(parkingRequest.getImage());
		parking.setStatus("PENDING");
		parking.setVehicleEquipmentType(vehicleEquipmentType_.get());
		
		parking.setVehicleEquipmentName(parkingRequest.getVehicleEquipmentName());
		parking.setVehicleEquipmentColor(parkingRequest.getVehicleEquipmentColor());
		
		parking.setVehicleEquipmentCategory(parkingRequest.getVehicleEquipmentCategory());
		parking.setVehicleEquipment(vehicleEquipment_.get());
		
		parking.setHasKeys(parkingRequest.getHasKeys());
		
		parking.setBranch(branch_.get());
		
		if(!parkingZone_.isEmpty()) parking.setParkingZone(parkingZone_.get());

		parking.setCreatedByUser(userService.getUser(request));
		parking.setCreatedDateTime(dayService.getTimeStamp());
		
		parking = parkingRepository.save(parking);
		/**Create  parking no*/
		parking.setNo("PKN/"+ parking.getId().toString());
		parking = parkingRepository.save(parking);
		
		
		//parking.setStatus(parkingRequest.getStatus() != null ? parkingRequest.getStatus() : "PENDING");

//		
//		parking.setParkingZone(parkingRequest.getParkingZone());
//		parking.setVehicleEquipmentType(parkingRequest.getVehicleEquipmentType());
//		parking.setCreatedByUser(parkingRequest.getCreatedByUser());
//		parking.setCreatedDateTime(parkingRequest.getCreatedDateTime() != null ? parkingRequest.getCreatedDateTime() : LocalDateTime.now());
//		parking.setCheckedInByUser(parkingRequest.getCheckedInByUser());
//		parking.setCheckedInDateTime(parkingRequest.getCheckedInDateTime());
//		parking.setCheckedOutByUser(parkingRequest.getCheckedOutByUser());
//		parking.setCheckedOutDateTime(parkingRequest.getCheckedOutDateTime());
//		parking.setCanceledByUser(parkingRequest.getCanceledByUser());
//		parking.setCanceledDateTime(parkingRequest.getCanceledDateTime());
//		parking.setBranch(parkingRequest.getBranch());
//		parking.setCompany(parkingRequest.getCompany());

	
		
		return parkingResponseDTOMapper(parking);		
	}

	@Override
	public ParkingResponseDTO updateParking(ParkingRequestDTO parkingRequest, HttpServletRequest request) {
		
		Optional<Parking> parking_ = parkingRepository.findById(parkingRequest.getId());
		if(parking_.isEmpty()) throw new NotFoundException("Parking not found in database");
			
		if(!parking_.get().getStatus().equals("PENDING")) throw new NotFoundException("Can not update, only pending parking can be updated");
			
		if(!validateParkingData(parkingRequest)) throw new InvalidEntryException("Could not validate data");
			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
		
		
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(parkingRequest.getVehicleEquipmentTypeName(), company_.get());
		if(vehicleEquipmentType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");
			
		
		if(vehicleEquipmentType_.get().getCompany().getId() != company_.get().getId()) 
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		
		Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findByNameAndBranch(parkingRequest.getParkingZoneName(), branch_.get());
		if(parkingZone_.isEmpty())throw new NotFoundException("Parking Zone not found");
		
		
		Parking parking = parking_.get();
		parking.setOwnerFirstName(parkingRequest.getOwnerFirstName());
		parking.setOwnerMiddleName(parkingRequest.getOwnerMiddleName());
		parking.setOwnerLastName(parkingRequest.getOwnerLastName());
		parking.setOwnerCompanyName(parkingRequest.getOwnerCompanyName());
		parking.setOwnerIdNo(parkingRequest.getOwnerIdNo());
		parking.setOwnerIdType(parkingRequest.getOwnerIdType());
		parking.setOwnerPhoneNo(parkingRequest.getOwnerPhoneNo());
		parking.setOwnerEmail(parkingRequest.getOwnerEmail());
		parking.setOwnerAddress(parkingRequest.getOwnerAddress());
		parking.setAgentName(parkingRequest.getAgentName());
		parking.setAgentAddress(parkingRequest.getAgentAddress());
		parking.setAgentPhoneNo(parkingRequest.getAgentPhoneNo());
		parking.setAgentEmail(parkingRequest.getAgentEmail());
		parking.setTformNumber(parkingRequest.getTformNumber());
		parking.setRegistrationNo(parkingRequest.getRegistrationNo());
		parking.setChasisNo(parkingRequest.getChasisNo());
		parking.setCardNo(parkingRequest.getCardNo());
		parking.setLeftFrontLamp(parkingRequest.isLeftFrontLamp());
		parking.setRightFrontLamp(parkingRequest.isRightFrontLamp());
		parking.setLeftRearLamp(parkingRequest.isLeftRearLamp());
		parking.setRightRearLamp(parkingRequest.isRightRearLamp());
		parking.setLeftSideMirror(parkingRequest.isLeftSideMirror());
		parking.setRightSideMirror(parkingRequest.isRightSideMirror());
		parking.setLeftWiper(parkingRequest.isLeftWiper());
		parking.setRightWiper(parkingRequest.isRightWiper());
		parking.setBackWiper(parkingRequest.isBackWiper());
		parking.setFuelCap(parkingRequest.isFuelCap());
		parking.setSpareTire(parkingRequest.isSpareTire());
		parking.setBattery(parkingRequest.isBattery());
		parking.setStarter(parkingRequest.isStarter());
		parking.setAerial(parkingRequest.isAerial());
		parking.setWheelCap(parkingRequest.isWheelCap());
		parking.setRoundMirror(parkingRequest.isRoundMirror());
		parking.setTireIndicator(parkingRequest.isTireIndicator());
		parking.setVehicleEquipmentType(vehicleEquipmentType_.get());
		parking.setVehicleEquipmentCategory(parkingRequest.getVehicleEquipmentCategory());
		
		parking.setVehicleEquipmentName(parkingRequest.getVehicleEquipmentName());
		parking.setVehicleEquipmentColor(parkingRequest.getVehicleEquipmentColor());
		
		parking.setHasKeys(parkingRequest.getHasKeys());
		
		parking.setParkingZone(parkingZone_.get());
		
		parking.setBillingType(parkingRequest.getBillingType());
				
		parking = parkingRepository.save(parking);
		
		return parkingResponseDTOMapper(parking);			
	}

	private ParkingResponseDTO parkingResponseDTOMapper(Parking parking) {
		ParkingResponseDTO parkingResponse = new ParkingResponseDTO();
		
		parkingResponse.setId(String.valueOf(parking.getId()));
		parkingResponse.setNo(parking.getNo());
		parkingResponse.setOwnerFirstName(parking.getOwnerFirstName());
		parkingResponse.setOwnerMiddleName(parking.getOwnerMiddleName());
		parkingResponse.setOwnerLastName(parking.getOwnerLastName());
		parkingResponse.setOwnerCompanyName(parking.getOwnerCompanyName());
		parkingResponse.setOwnerIdNo(parking.getOwnerIdNo());
		parkingResponse.setOwnerIdType(parking.getOwnerIdType());
		parkingResponse.setOwnerPhoneNo(parking.getOwnerPhoneNo());
		parkingResponse.setOwnerEmail(parking.getOwnerEmail());
		parkingResponse.setOwnerAddress(parking.getOwnerAddress());
		parkingResponse.setAgentName(parking.getAgentName());
		parkingResponse.setAgentAddress(parking.getAgentAddress());
		parkingResponse.setAgentPhoneNo(parking.getAgentPhoneNo());
		parkingResponse.setAgentEmail(parking.getAgentEmail());
		parkingResponse.setTformNumber(parking.getTformNumber());
		parkingResponse.setRegistrationNo(parking.getRegistrationNo());
		parkingResponse.setChasisNo(parking.getChasisNo());
		parkingResponse.setCardNo(parking.getCardNo());
		parkingResponse.setLeftFrontLamp( parking.isLeftFrontLamp() ? "1" : "0");
		parkingResponse.setRightFrontLamp(parking.isRightFrontLamp() ? "1" : "0");
		parkingResponse.setLeftRearLamp(parking.isLeftRearLamp() ? "1" : "0");
		parkingResponse.setRightRearLamp(parking.isRightRearLamp() ? "1" : "0");
		parkingResponse.setLeftSideMirror(parking.isLeftSideMirror() ? "1" : "0");
		parkingResponse.setRightSideMirror(parking.isRightSideMirror() ? "1" : "0");
		parkingResponse.setLeftWiper(parking.isLeftWiper() ? "1" : "0");
		parkingResponse.setRightWiper(parking.isRightWiper() ? "1" : "0");
		parkingResponse.setBackWiper(parking.isBackWiper() ? "1" : "0");
		parkingResponse.setFuelCap(parking.isFuelCap() ? "1" : "0");
		parkingResponse.setSpareTire(parking.isSpareTire() ? "1" : "0");
		parkingResponse.setBattery(parking.isBattery() ? "1" : "0");
		parkingResponse.setStarter(parking.isStarter() ? "1" : "0");
		parkingResponse.setAerial(parking.isAerial() ? "1" : "0");
		parkingResponse.setWheelCap(parking.isWheelCap() ? "1" : "0");
		parkingResponse.setRoundMirror(parking.isRoundMirror() ? "1" : "0");
		parkingResponse.setTireIndicator(parking.isTireIndicator() ? "1" : "0");
		parkingResponse.setVehicleEquipmentCategory(parking.getVehicleEquipmentCategory());
		parkingResponse.setVehicleEquipmentName(parking.getVehicleEquipmentName());
		parkingResponse.setVehicleEquipmentColor(parking.getVehicleEquipmentColor());
		parkingResponse.setHasKeys(parking.getHasKeys());
		//parking.setImage(parkingRequest.getImage());
		parkingResponse.setStatus(parking.getStatus());
		//parkingResponse.setCompanyId(parking.getCompany().getId().toString());
		parkingResponse.setBranchId(parking.getBranch().getId().toString());
		
		parkingResponse.setBillingType(parking.getBillingType());
		parkingResponse.setBillingAmount(String.valueOf(parking.getBillingAmount()));
		
		parkingResponse.setVehicleEquipmentTypeName(parking.getVehicleEquipmentType().getName());
		
		parkingResponse.setParkingZoneName(
				parking.getParkingZone() != null && parking.getParkingZone().getName() != null
		        ? parking.getParkingZone().getName() 
		        : "");
		
		return parkingResponse;
	}
	
	boolean validateParkingData(ParkingRequestDTO parkingRequest) {
		
		return true;
	}

	@Override
	public ParkingResponseDTO checkIn(ParkingRequestDTO parkingRequest, HttpServletRequest request) {
		
		Optional<Parking> parking_ = parkingRepository.findById(parkingRequest.getId());
		
		if(parking_.isEmpty()) 
			throw new NotFoundException("Parking not found");			
		if(parking_.get().getBranch().getId() != userService.getUser(request).getBranch().getId()) 
			throw new InvalidOperationException("Checking in can only be done by a branch user");
		if(!parking_.get().getStatus().equals("PENDING")) 
			throw new InvalidOperationException("Can not check in, only a pending parking can be checked in");
		
		//now check in vehicle, if it has a pending status
		
		Parking parking = parking_.get();
		
		Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findByNameAndBranch(parkingRequest.getParkingZoneName(), parking_.get().getBranch());
		if(parkingZone_.isEmpty())throw new NotFoundException("Parking Zone not found");
		
		parking.setBillingAmount(parking.getVehicleEquipmentType().getDailyPrice());
		
		parking.setParkingZone(parkingZone_.get());
		parking.setStatus("CHECKED-IN");
		parking.setCardNo(parkingRequest.getCardNo());
		parking.setHasKeys(parkingRequest.getHasKeys());
		parking.setCheckedInByUser(userService.getUser(request));
		parking.setCheckedInDateTime(dayService.getTimeStamp());
		
		if(parkingRequest.startBillingAt == null) {
			parking.setStartBillingAt(dayService.getTimeStamp()); // You can change this depending on user billing preferences
		}else {
			
			//String dateString = "2024-10-26 15:30:45" ;
			String dateString = parkingRequest.getStartBillingAt() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
			parking.setStartBillingAt(dateTime);
		}		
		parking = parkingRepository.save(parking);
		
//		//generate bill, for day 1 depending on billing type
//		
//		BillReceivable billReceivable = new BillReceivable();
//		billReceivable.setNo(String.valueOf(Math.random()));
//		billReceivable.setAmount(parking.getBillingAmount());
//		billReceivable.setPaid(0);
//		billReceivable.setDue(parking.getBillingAmount());
//		//billReceivable.setCompany(parking.getCompany());
//		billReceivable.setBranch(parking.getBranch());
//		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
//		
//		billReceivable.setStatus("UNPAID");
//		billReceivable.setSummary("Parking bill for parking#: " + parking.getNo());
//		
//		billReceivable = billReceivableRepository.save(billReceivable);
//		billReceivable.setNo("BR" + billReceivable.getId().toString());
//		billReceivable = billReceivableRepository.save(billReceivable);
//		
//		
//		
//		InvoiceReceivable invoiceReceivable = null;
//		ParkingInvoiceReceivable parkingInvoiceReceivable = null;
//		
//		List<ParkingInvoiceReceivable> parkingInvoiceReceivables = parkingInvoiceReceivableRepository.findAllByParking(parking);
//		for(ParkingInvoiceReceivable pInvoiceReceivable : parkingInvoiceReceivables) {
//			if(pInvoiceReceivable.getInvoiceReceivable().getStatus()
//					.equals("OPEN")) {
//				invoiceReceivable = pInvoiceReceivable.getInvoiceReceivable();
//				break;
//			}
//		}
//		if(invoiceReceivable == null) {
//			invoiceReceivable = new InvoiceReceivable();
//			invoiceReceivable.setNo(String.valueOf(Math.random()));
//			//invoiceReceivable.setCompany(parking.getCompany());
//			invoiceReceivable.setBranch(parking.getBranch());
//			invoiceReceivable.setStatus("OPEN");
//			invoiceReceivable.setSummary("Auto invoice, for parking# " + parking.getNo());
//			
//			invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
//			invoiceReceivable.setNo("RINV" + invoiceReceivable.getId().toString());
//			
//			invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
//			
//			parkingInvoiceReceivable = new ParkingInvoiceReceivable();
//			parkingInvoiceReceivable.setParking(parking);
//			parkingInvoiceReceivable.setInvoiceReceivable(invoiceReceivable);
//			
//			parkingInvoiceReceivableRepository.save(parkingInvoiceReceivable);
//		}
//		
//		InvoiceReceivableDetail invoiceReceivableDetail = new InvoiceReceivableDetail();
//		invoiceReceivableDetail.setInvoiceReceivable(invoiceReceivable);
//		
//		invoiceReceivableDetail.setBillReceivable(billReceivable);
//		
//		invoiceReceivableDetail.setAmount(parking.getBillingAmount());
//		invoiceReceivableDetail.setDue(parking.getBillingAmount());
//		invoiceReceivableDetail.setPaid(0);
//		invoiceReceivableDetail.setSummary("Payment for parking# " + parking.getNo());
//		
//		invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);
//		
//		ParkingBillReceivable parkingBillReceivable = new ParkingBillReceivable();
//		parkingBillReceivable.setBillReceivable(billReceivable);
//		parkingBillReceivable.setStartedAt(LocalDateTime.now());
//		parkingBillReceivable.setEndedAt(LocalDateTime.now().plusDays(1));
//		parkingBillReceivable.setBillingType("DAILY");
//		parkingBillReceivable.setQty(1);
//		parkingBillReceivable.setPrice(parking.getBillingAmount());
//		parkingBillReceivable.setParking(parking);
//		//parkingBillReceivable.setInvoiceReceivableDetail(invoiceReceivableDetail);
//		
//		parkingBillReceivableRepository.save(parkingBillReceivable);
	
		return parkingResponseDTOMapper(parking);
	}

	@Override
	public ParkingResponseDTO checkOut(ParkingRequestDTO parkingRequest, HttpServletRequest request) {
		
		Optional<Parking> parking_ = parkingRepository.findById(parkingRequest.getId());
		if(parking_.isEmpty()) throw new NotFoundException("Parking not found in database");
			
		if(!parking_.get().getStatus().equals("CHECKED-IN")) throw new NotFoundException("Can not check out, only checked in parking can be checked out");
			
//		if(!validateParkingData(parkingRequest)) throw new InvalidEntryException("Could not validate data");
			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
//		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(parkingRequest.getVehicleEquipmentTypeName(), company_.get());
//		if(vehicleEquipmentType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");
		
		
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParking(parking_.get());
		LocalDateTime lastDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
		LocalDateTime lastBillDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
		for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
			if(parkingBillReceivable.getBillReceivable().getStatus().equals("UNPAID")) {
				throw new InvalidOperationException("Can not check out, bills  not cleared");
			}
			lastBillDate = parkingBillReceivable.getEndedAt();
		}
		if(lastBillDate.isBefore(lastDate.plusDays(1))) {
			throw new InvalidOperationException("Could not checkout. Some parking days have not been billed. Please generate and clear bills");
		}
		
		List<ParkingServiceBillReceivable> parkingServiceBillReceivables = parkingServiceBillReceivableRepository.findAllByParking(parking_.get());
		for(ParkingServiceBillReceivable parkingServiceBillReceivable : parkingServiceBillReceivables) {
			if(parkingServiceBillReceivable.getBillReceivable().getStatus().equals("UNPAID")) {
				throw new InvalidOperationException("Can not check out, bills  not cleared");
			}
		}
		
		Parking parking = parking_.get();
		parking.setStatus("CHECKED-OUT");
		parking.setCardNo(parkingRequest.getCardNo());
		parking.setCheckedOutByUser(userService.getUser(request));
		parking.setCheckedOutDateTime(dayService.getTimeStamp());
		
		parking = parkingRepository.save(parking);
		
		return parkingResponseDTOMapper(parking);
	}
	
	private ParkingBillReceivableResponseDTO parkingBillReceivableDTOMapper(ParkingBillReceivable parkingBillReceivable) {
		
		ParkingBillReceivableResponseDTO parkingBillReceivableResponseDTO = new ParkingBillReceivableResponseDTO();
		
		parkingBillReceivableResponseDTO.setId(parkingBillReceivable.getId().toString());
		parkingBillReceivableResponseDTO.setStartedAt(parkingBillReceivable.getStartedAt().toString());
		parkingBillReceivableResponseDTO.setEndedAt(parkingBillReceivable.getEndedAt().toString());
		parkingBillReceivableResponseDTO.setQty(String.valueOf(parkingBillReceivable.getQty()));
		parkingBillReceivableResponseDTO.setPrice(String.valueOf(parkingBillReceivable.getPrice()));
		parkingBillReceivableResponseDTO.setBillingType(parkingBillReceivable.getBillingType());
		parkingBillReceivableResponseDTO.setDiscount(String.valueOf(parkingBillReceivable.getDiscount()));
		parkingBillReceivableResponseDTO.setParkingId(parkingBillReceivable.getParking().getId().toString());
		parkingBillReceivableResponseDTO.setAmount(String.valueOf(((parkingBillReceivable.getPrice() * parkingBillReceivable.getQty()) - parkingBillReceivable.getDiscount())));
		parkingBillReceivableResponseDTO.setStatus(parkingBillReceivable.getBillReceivable().getStatus());
				
		return parkingBillReceivableResponseDTO;
		
	}

	@Override
	public ParkingBillReceivableResponseDTO createParkingBillReceivable(Long parkingId, LocalDateTime startedAt,
			LocalDateTime endedAt, String billingType, double qty, double price, double discount, int autoBilling, HttpServletRequest request) {
		// TODO Auto-generated method stub
		
		/**
		 * Here, get the parking, check for parking billing start datetime, if the billing start datetime is more
		 * than current datetime if the bill is less than current date time, invalidate the bill, ignore previous date time
		 * if billstart datetime is b4 current date time, check if there are previous bills, if yes, check the last datetime
		 * if last date time is beyond bill, invalidate bill
		 * 
		 */
		
		if (autoBilling != 1 && autoBilling != 0) {
		    throw new InvalidOperationException("Invalid billing mode selected. Accepts 1: Autobilling, 0: Manual billing");
		}
		Parking parking = parkingRepository.findById(parkingId)
		        .orElseThrow(() -> new NotFoundException("Parking not found"));
		
		if(!parking.getStatus().equals("CHECKED-IN")) throw new InvalidOperationException("Only allowed for checked in parkings");
		
		if(autoBilling == 0) {
			/**
			 * Here, do manual billing
			 */
			if(parking.getStartBillingAt().isBefore(startedAt)) {
				
				List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findByParking(parking);
				// Now check for intersection
				for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
					if(startedAt.isAfter(parkingBillReceivable.getStartedAt()) && startedAt.isBefore(parkingBillReceivable.getEndedAt().plusDays(1))) {
						throw new InvalidOperationException("Bill starting date intersects with current bills");
					}
				}
				
			}
			
			
			
			
			
			
			BillReceivable billReceivable = new BillReceivable();
			billReceivable.setNo(String.valueOf(Math.random()));
			billReceivable.setAmount(parking.getBillingAmount());
			billReceivable.setPaid(0);
			billReceivable.setDue(parking.getBillingAmount());
			//billReceivable.setCompany(parking.getCompany());
			billReceivable.setBranch(parking.getBranch());
			billReceivable.setCreatedDateTime(dayService.getTimeStamp());
			
			billReceivable.setStatus("UNPAID");
			billReceivable.setSummary("Parking bill for parking#: " + parking.getNo());
			
			billReceivable = billReceivableRepository.save(billReceivable);
			billReceivable.setNo("BR" + billReceivable.getId().toString());
			billReceivable = billReceivableRepository.save(billReceivable);
			
			
			
			InvoiceReceivable invoiceReceivable = null;
			ParkingInvoiceReceivable parkingInvoiceReceivable = null;
			
			List<ParkingInvoiceReceivable> parkingInvoiceReceivables = parkingInvoiceReceivableRepository.findAllByParking(parking);
			for(ParkingInvoiceReceivable pInvoiceReceivable : parkingInvoiceReceivables) {
				if(pInvoiceReceivable.getInvoiceReceivable().getStatus()
						.equals("OPEN")) {
					invoiceReceivable = pInvoiceReceivable.getInvoiceReceivable();
					break;
				}
			}
			if(invoiceReceivable == null) {
				invoiceReceivable = new InvoiceReceivable();
				invoiceReceivable.setNo(String.valueOf(Math.random()));
				//invoiceReceivable.setCompany(parking.getCompany());
				invoiceReceivable.setBranch(parking.getBranch());
				invoiceReceivable.setStatus("OPEN");
				invoiceReceivable.setSummary("Auto invoice, for parking# " + parking.getNo());
				
				invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
				invoiceReceivable.setNo("RINV" + invoiceReceivable.getId().toString());
				
				invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
				
				parkingInvoiceReceivable = new ParkingInvoiceReceivable();
				parkingInvoiceReceivable.setParking(parking);
				parkingInvoiceReceivable.setInvoiceReceivable(invoiceReceivable);
				
				parkingInvoiceReceivableRepository.save(parkingInvoiceReceivable);
			}
			
			InvoiceReceivableDetail invoiceReceivableDetail = new InvoiceReceivableDetail();
			invoiceReceivableDetail.setInvoiceReceivable(invoiceReceivable);
			
			invoiceReceivableDetail.setBillReceivable(billReceivable);
			
			invoiceReceivableDetail.setAmount(parking.getBillingAmount());
			invoiceReceivableDetail.setDue(parking.getBillingAmount());
			invoiceReceivableDetail.setPaid(0);
			invoiceReceivableDetail.setSummary("Payment for parking# " + parking.getNo());
			
			invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);
			
			ParkingBillReceivable parkingBillReceivable = new ParkingBillReceivable();
			parkingBillReceivable.setBillReceivable(billReceivable);
			parkingBillReceivable.setStartedAt(startedAt);
			parkingBillReceivable.setEndedAt(endedAt.plusDays(1));
			parkingBillReceivable.setBillingType("DAILY");
			parkingBillReceivable.setQty(1);
			parkingBillReceivable.setPrice(parking.getBillingAmount());
			parkingBillReceivable.setParking(parking);
			//parkingBillReceivable.setInvoiceReceivableDetail(invoiceReceivableDetail);
			
			parkingBillReceivableRepository.save(parkingBillReceivable);
			
		}else if(autoBilling == 1) {
			// Find last billing date
			
		}
		
		return null;
	}

}
