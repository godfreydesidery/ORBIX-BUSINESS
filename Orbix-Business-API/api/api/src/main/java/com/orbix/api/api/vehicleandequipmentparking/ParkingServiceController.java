package com.orbix.api.api.vehicleandequipmentparking;

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
	
	private final ParkingZoneRepository parkingZoneRepository;
	
	private final VehicleAndEquipmentTypeRepository vehicleAndEquipmentTypeRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
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
	public ParkingResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<Parking> parking_ = parkingRepository.findById(id);
	if(parking_.isEmpty()) {
		throw new NotFoundException("Parking not found");
	}		
	return parkingResponseDTOMapper(parking_.get());	
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
		
		Optional<VehicleAndEquipmentType> vehicleAndEquipmentType_ = vehicleAndEquipmentTypeRepository.findByNameAndCompany(parkingRequest.getVehicleAndEquipmentTypeName(), company_.get());
		if(vehicleAndEquipmentType_.isEmpty()) {
			throw new NotFoundException("Vehicle or equipment type not found");
		}
		if(vehicleAndEquipmentType_.get().getCompany().getId() != company_.get().getId()) {
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		}
		
		
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
		parking.setLeftFrontLamp(parkingRequest.getLeftFrontLamp());
		parking.setRightFrontLamp(parkingRequest.getRightFrontLamp());
		parking.setLeftRearLamp(parkingRequest.getLeftRearLamp());
		parking.setRightRearLamp(parkingRequest.getRightRearLamp());
		parking.setLeftSideMirror(parkingRequest.getLeftSideMirror());
		parking.setRightSideMirror(parkingRequest.getRightSideMirror());
		parking.setLeftWiper(parkingRequest.getLeftWiper());
		parking.setRightWiper(parkingRequest.getRightWiper());
		parking.setBackWiper(parkingRequest.getBackWiper());
		parking.setFuelCap(parkingRequest.getFuelCap());
		parking.setSpareTire(parkingRequest.getSpareTire());
		parking.setBattery(parkingRequest.getBattery());
		parking.setStarter(parkingRequest.getStarter());
		parking.setAerial(parkingRequest.getAerial());
		parking.setWheelCap(parkingRequest.getWheelCap());
		parking.setRoundMirror(parkingRequest.getRoundMirror());
		parking.setTireIndicator(parkingRequest.getTireIndicator());
		//parking.setImage(parkingRequest.getImage());
		parking.setStatus("PENDING");
		parking.setVehicleAndEquipmentType(vehicleAndEquipmentType_.get());
		
		parking.setVehicleAndEquipmentCategory(parkingRequest.getVehicleAndEquipmentCategory());
		
		parking.setCompany(company_.get());
		parking.setBranch(branch_.get());
		
		parking.setCreatedByUser(userService.getUser(request));
		parking.setCreatedDateTime(dayService.getTimeStamp());
		
		parking = parkingRepository.save(parking);
		/**Create  parking no*/
		parking.setNo("PKN/"+ parking.getId().toString());
		parking = parkingRepository.save(parking);
		
		
		//parking.setStatus(parkingRequest.getStatus() != null ? parkingRequest.getStatus() : "PENDING");

//		
//		parking.setParkingZone(parkingRequest.getParkingZone());
//		parking.setVehicleAndEquipmentType(parkingRequest.getVehicleAndEquipmentType());
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
		if(parking_.isEmpty()) {
			throw new NotFoundException("Parking not found in database");
		}
		
		if(!validateParkingData(parkingRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) {
			throw new NotFoundException("Company not found");
		}
		
		Optional<VehicleAndEquipmentType> vehicleAndEquipmentType_ = vehicleAndEquipmentTypeRepository.findByNameAndCompany(parkingRequest.getVehicleAndEquipmentTypeName(), company_.get());
		if(vehicleAndEquipmentType_.isEmpty()) {
			throw new NotFoundException("Vehicle or equipment type not found");
		}
		if(vehicleAndEquipmentType_.get().getCompany().getId() != company_.get().getId()) {
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		}
		
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
		parking.setLeftFrontLamp(parkingRequest.getLeftFrontLamp());
		parking.setRightFrontLamp(parkingRequest.getRightFrontLamp());
		parking.setLeftRearLamp(parkingRequest.getLeftRearLamp());
		parking.setRightRearLamp(parkingRequest.getRightRearLamp());
		parking.setLeftSideMirror(parkingRequest.getLeftSideMirror());
		parking.setRightSideMirror(parkingRequest.getRightSideMirror());
		parking.setLeftWiper(parkingRequest.getLeftWiper());
		parking.setRightWiper(parkingRequest.getRightWiper());
		parking.setBackWiper(parkingRequest.getBackWiper());
		parking.setFuelCap(parkingRequest.getFuelCap());
		parking.setSpareTire(parkingRequest.getSpareTire());
		parking.setBattery(parkingRequest.getBattery());
		parking.setStarter(parkingRequest.getStarter());
		parking.setAerial(parkingRequest.getAerial());
		parking.setWheelCap(parkingRequest.getWheelCap());
		parking.setRoundMirror(parkingRequest.getRoundMirror());
		parking.setTireIndicator(parkingRequest.getTireIndicator());
		parking.setVehicleAndEquipmentType(vehicleAndEquipmentType_.get());
		parking.setVehicleAndEquipmentCategory(parkingRequest.getVehicleAndEquipmentCategory());
				
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
		parkingResponse.setLeftFrontLamp(parking.getLeftFrontLamp());
		parkingResponse.setRightFrontLamp(parking.getRightFrontLamp());
		parkingResponse.setLeftRearLamp(parking.getLeftRearLamp());
		parkingResponse.setRightRearLamp(parking.getRightRearLamp());
		parkingResponse.setLeftSideMirror(parking.getLeftSideMirror());
		parkingResponse.setRightSideMirror(parking.getRightSideMirror());
		parkingResponse.setLeftWiper(parking.getLeftWiper());
		parkingResponse.setRightWiper(parking.getRightWiper());
		parkingResponse.setBackWiper(parking.getBackWiper());
		parkingResponse.setFuelCap(parking.getFuelCap());
		parkingResponse.setSpareTire(parking.getSpareTire());
		parkingResponse.setBattery(parking.getBattery());
		parkingResponse.setStarter(parking.getStarter());
		parkingResponse.setAerial(parking.getAerial());
		parkingResponse.setWheelCap(parking.getWheelCap());
		parkingResponse.setRoundMirror(parking.getRoundMirror());
		parkingResponse.setTireIndicator(parking.getTireIndicator());
		parkingResponse.setVehicleAndEquipmentCategory(parking.getVehicleAndEquipmentCategory());
		//parking.setImage(parkingRequest.getImage());
		parkingResponse.setStatus(parking.getStatus());
		parkingResponse.setCompanyId(parking.getCompany().getId().toString());
		parkingResponse.setBranchId(parking.getBranch().getId().toString());
		
		parkingResponse.setBillingType(parking.getBillingType());
		parkingResponse.setBillingAmount(String.valueOf(parking.getBillingAmount()));
		
		parkingResponse.setVehicleAndEquipmentTypeName(parking.getVehicleAndEquipmentType().getName());
		
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
		
		parking.setBillingAmount(parking.getVehicleAndEquipmentType().getDailyPrice());
		
		parking.setParkingZone(parkingZone_.get());
		parking.setStatus("CHECKED-IN");
		parking.setCheckedInByUser(userService.getUser(request));
		parking.setCheckedInDateTime(dayService.getTimeStamp());
		
		parking = parkingRepository.save(parking);
		
		//generate bill, for day 1 depending on billing type
		
		BillReceivable billReceivable = new BillReceivable();
		billReceivable.setNo(String.valueOf(Math.random()));
		billReceivable.setAmount(parking.getBillingAmount());
		billReceivable.setPaid(0);
		billReceivable.setDue(parking.getBillingAmount());
		billReceivable.setCompany(parking.getCompany());
		billReceivable.setBranch(parking.getBranch());
		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
		
		billReceivable.setStatus("UNPAID");
		billReceivable.setSummary("Parking bill for parking#: " + parking.getNo());
		
		billReceivable = billReceivableRepository.save(billReceivable);
		billReceivable.setNo("BR" + billReceivable.getId().toString());
		billReceivableRepository.save(billReceivable);
		
		
		
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
			invoiceReceivable.setCompany(parking.getCompany());
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
		invoiceReceivableDetail.setAmount(parking.getBillingAmount());
		invoiceReceivableDetail.setDue(parking.getBillingAmount());
		invoiceReceivableDetail.setPaid(0);
		invoiceReceivableDetail.setSummary("Payment for parking# " + parking.getNo());
		
		invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);
		
		ParkingBillReceivable parkingBillReceivable = new ParkingBillReceivable();
		parkingBillReceivable.setBillReceivable(billReceivable);
		parkingBillReceivable.setParking(parking);
		parkingBillReceivable.setInvoiceReceivableDetail(invoiceReceivableDetail);
		
		parkingBillReceivableRepository.save(parkingBillReceivable);
	
		
		return parkingResponseDTOMapper(parking);
	}

	@Override
	public ParkingResponseDTO checkOut(ParkingRequestDTO parkingRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
