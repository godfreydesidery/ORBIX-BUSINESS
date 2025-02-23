package com.orbix.api.api.vehicleandequipmentparking;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

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
import com.orbix.api.modules.vehicleandequipmentmaintenance.Maintenance;
import com.orbix.api.modules.vehicleandequipmentmaintenance.MaintenanceRepository;
import com.orbix.api.modules.vehicleandequipmentmaintenance.MaintenanceRequestDTO;
import com.orbix.api.modules.vehicleandequipmentmaintenance.MaintenanceResponseDTO;
import com.orbix.api.modules.vehicleandequipmentmaintenance.MaintenanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VehicleEquipmentServiceController implements VehicleEquipmentService {
	private final VehicleEquipmentRepository vehicleEquipmentRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final ParkingRepository parkingRepository;
	private final MaintenanceRepository maintenanceRepository;
	
	private final ParkingService parkingService;
	private final MaintenanceService maintenanceService;
	
	private final VehicleEquipmentTypeRepository vehicleEquipmentTypeRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	
	private final InvoiceReceivableRepository invoiceReceivableRepository;
	private final InvoiceReceivableDetailRepository invoiceReceivableDetailRepository;
	
	private final ParkingZoneRepository parkingZoneRepository;
	
	

	@Override
	public List<VehicleEquipmentResponseDTO> getAllVehicleEquipments(HttpServletRequest request) {
		List<VehicleEquipment> vehicleEquipments = vehicleEquipmentRepository.findAll();
		List<VehicleEquipmentResponseDTO> vehicleEquipmentResponses = new ArrayList<>();

		for(VehicleEquipment vehicleEquipment : vehicleEquipments) {
			vehicleEquipmentResponses.add(vehicleEquipmentResponseDTOMapper(vehicleEquipment, null, null));					
		}		
		return vehicleEquipmentResponses;
	}
	
	@Override
	public List<VehicleEquipmentResponseDTO> getAllActiveVehicleEquipments(HttpServletRequest request) {
		List<VehicleEquipment> vehicleEquipments = vehicleEquipmentRepository.findAllByActiveTrue();
		List<VehicleEquipmentResponseDTO> vehicleEquipmentResponses = new ArrayList<>();

		for(VehicleEquipment vehicleEquipment : vehicleEquipments) {
			vehicleEquipmentResponses.add(vehicleEquipmentResponseDTOMapper(vehicleEquipment, null, null));					
		}		
		return vehicleEquipmentResponses;
	}

	@Override
	public VehicleEquipmentResponseDTO get(Long id, HttpServletRequest request) {		
		Optional<VehicleEquipment> vehicleEquipment_ = vehicleEquipmentRepository.findById(id);
		if(vehicleEquipment_.isEmpty()) {
			throw new NotFoundException("VehicleEquipment not found");
		}		
		return vehicleEquipmentResponseDTOMapper(vehicleEquipment_.get(), null, null);	
	}
	
	@Override
	public VehicleEquipmentResponseDTO getByChasisNo(String chasisNo, HttpServletRequest request) {		
		Optional<VehicleEquipment> vehicleEquipment_ = vehicleEquipmentRepository.findFirstByChasisNoAndActiveTrue(chasisNo);
		if(vehicleEquipment_.isEmpty()) {
			throw new NotFoundException("VehicleEquipment not found");
		}
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");
		
		ParkingResponseDTO parkingResponse = new ParkingResponseDTO();
		
		List<Parking> parkings = parkingRepository.findAllByVehicleEquipmentAndStatusIn(vehicleEquipment_.get(), statuses);
		for(Parking p : parkings) {
			parkingResponse.setId(p.getId().toString());
		}
		
		return vehicleEquipmentResponseDTOMapper(vehicleEquipment_.get(), parkingResponse, null);
	}
	
	@Override
	public VehicleEquipmentResponseDTO getMaintenanceByChasisNo(String chasisNo, HttpServletRequest request) {		
		Optional<VehicleEquipment> vehicleEquipment_ = vehicleEquipmentRepository.findFirstByChasisNoAndActiveTrue(chasisNo);
		if(vehicleEquipment_.isEmpty()) {
			throw new NotFoundException("VehicleEquipment not found");
		}
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");
		
		MaintenanceResponseDTO maintenanceResponse = new MaintenanceResponseDTO();
		
		List<Maintenance> maintenances = maintenanceRepository.findAllByVehicleEquipmentAndStatusIn(vehicleEquipment_.get(), statuses);
		for(Maintenance m : maintenances) {
			maintenanceResponse.setId(m.getId().toString());
		}
		
		return vehicleEquipmentResponseDTOMapper(vehicleEquipment_.get(), null, maintenanceResponse);
	}
	
	
	@Override	
	public List<String> getChasisNos(HttpServletRequest request) {
		List<String> chasisNos = vehicleEquipmentRepository.findTop2000ByActiveTrue()
			    .stream()
			    .map(VehicleEquipment::getChasisNo)
			    .collect(Collectors.toList());
		
			return chasisNos;		
	}
	
	

	@Override
	public VehicleEquipmentResponseDTO createVehicleEquipment(VehicleEquipmentRequestDTO vehicleEquipmentRequest, HttpServletRequest request) {
		
		if(!(vehicleEquipmentRequest.getService().equals("PARKING") || vehicleEquipmentRequest.getService().equals("MAINTENANCE"))) {
			throw new InvalidOperationException("Valid Service not specified");
		}
		
		/**Validate data*/		
		if(!validateVehicleEquipmentData(vehicleEquipmentRequest)) 
			throw new InvalidEntryException("Validation failed");
		
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) 
			throw new NotFoundException("Company not found");
		
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) 
			throw new NotFoundException("Branch not found");
			
		
		
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(vehicleEquipmentRequest.getVehicleEquipmentTypeName(), company_.get());
		if(vehicleEquipmentType_.isEmpty()) 
			throw new NotFoundException("Vehicle or // equipment type not found");

		if(vehicleEquipmentType_.get().getCompany().getId() != company_.get().getId()) 
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");

		
		
		VehicleEquipment vehicleEquipment = new VehicleEquipment();
		vehicleEquipment.setNo(String.valueOf(Math.random()));
		vehicleEquipment.setOwnerFirstName(vehicleEquipmentRequest.getOwnerFirstName());
		vehicleEquipment.setOwnerMiddleName(vehicleEquipmentRequest.getOwnerMiddleName());
		vehicleEquipment.setOwnerLastName(vehicleEquipmentRequest.getOwnerLastName());
		vehicleEquipment.setOwnerCompanyName(vehicleEquipmentRequest.getOwnerCompanyName());
		vehicleEquipment.setOwnerIdNo(vehicleEquipmentRequest.getOwnerIdNo());
		vehicleEquipment.setOwnerIdType(vehicleEquipmentRequest.getOwnerIdType());
		vehicleEquipment.setOwnerPhoneNo(vehicleEquipmentRequest.getOwnerPhoneNo());
		vehicleEquipment.setOwnerEmail(vehicleEquipmentRequest.getOwnerEmail());
		vehicleEquipment.setOwnerAddress(vehicleEquipmentRequest.getOwnerAddress());
		vehicleEquipment.setRegistrationNo(vehicleEquipmentRequest.getRegistrationNo());
		vehicleEquipment.setChasisNo(vehicleEquipmentRequest.getChasisNo());
		vehicleEquipment.setCardNo(vehicleEquipmentRequest.getCardNo());
		vehicleEquipment.setTformNumber(vehicleEquipmentRequest.getTformNumber());
		vehicleEquipment.setAgentName(vehicleEquipmentRequest.getAgentName());
		vehicleEquipment.setAgentPhoneNo(vehicleEquipmentRequest.getAgentPhoneNo());
		vehicleEquipment.setAgentAddress(vehicleEquipmentRequest.getAgentAddress());
		vehicleEquipment.setAgentEmail(vehicleEquipmentRequest.getAgentEmail());
		vehicleEquipment.setDeviceStatus(vehicleEquipmentRequest.isDeviceStatus());
		//vehicleEquipment.setImage(vehicleEquipmentRequest.getImage());
		vehicleEquipment.setActive(true);
		vehicleEquipment.setVehicleEquipmentColor(vehicleEquipmentRequest.getVehicleEquipmentColor());
		
		
		vehicleEquipment.setVehicleEquipmentType(vehicleEquipmentType_.get());		
		
		//vehicleEquipment.setCompany(company_.get());
		vehicleEquipment.setBranch(branch_.get());
		
		vehicleEquipment.setCreatedByUser(userService.getUser(request));
		vehicleEquipment.setCreatedDateTime(dayService.getTimeStamp());
		
		vehicleEquipment = vehicleEquipmentRepository.save(vehicleEquipment);
		/**Create  vehicleEquipment no*/
		vehicleEquipment.setNo("PKN/"+ vehicleEquipment.getId().toString());
		vehicleEquipment = vehicleEquipmentRepository.save(vehicleEquipment);
		
		
		if(vehicleEquipmentRequest.getService().equals("PARKING")) {
			// After creating, save also to parking
			
			ParkingRequestDTO parkingRequest = new ParkingRequestDTO();
			
//			Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findByNameAndBranch(vehicleEquipmentRequest.getParkingZoneName(), branch_.get());
//			if(parkingZone_.isEmpty())throw new NotFoundException("Parking Zone not found");
//			
//			parkingRequest.setParkingZoneName(vehicleEquipmentRequest.getParkingZoneName());

			parkingRequest.setOwnerFirstName(vehicleEquipment.getOwnerFirstName());
			parkingRequest.setOwnerMiddleName(vehicleEquipment.getOwnerMiddleName());
			parkingRequest.setOwnerLastName(vehicleEquipment.getOwnerLastName());
			parkingRequest.setOwnerCompanyName(vehicleEquipment.getOwnerCompanyName());
			parkingRequest.setOwnerIdNo(vehicleEquipment.getOwnerIdNo());
			parkingRequest.setOwnerIdType(vehicleEquipment.getOwnerIdType());
			parkingRequest.setOwnerPhoneNo(vehicleEquipment.getOwnerPhoneNo());
			parkingRequest.setOwnerEmail(vehicleEquipment.getOwnerEmail());
			parkingRequest.setOwnerAddress(vehicleEquipment.getOwnerAddress());
			parkingRequest.setVehicleEquipmentTypeName(vehicleEquipment.getVehicleEquipmentType().getName());
			parkingRequest.setVehicleEquipmentId(vehicleEquipment.getId());
			
			parkingRequest.setChasisNo(vehicleEquipment.getChasisNo());
			
			parkingRequest.setTformNumber(vehicleEquipment.getTformNumber());
			
			parkingRequest.setVehicleEquipmentColor(vehicleEquipment.getVehicleEquipmentColor());
			
			parkingRequest.setAgentName(vehicleEquipment.getAgentName());
			parkingRequest.setAgentAddress(vehicleEquipment.getAgentAddress());
			parkingRequest.setAgentPhoneNo(vehicleEquipment.getAgentPhoneNo());
			parkingRequest.setAgentEmail(vehicleEquipment.getAgentEmail());
			parkingRequest.setDeviceStatus(vehicleEquipment.isDeviceStatus());
			
			parkingRequest.setComments(vehicleEquipmentRequest.getComments());
			
				
			parkingRequest.setLeftFrontLamp(true);
			parkingRequest.setRightFrontLamp(true);
			parkingRequest.setLeftRearLamp(true);
			parkingRequest.setRightRearLamp(true);
			parkingRequest.setLeftSideMirror(true);
			parkingRequest.setRightSideMirror(true);
			parkingRequest.setLeftWiper(true);
			parkingRequest.setRightWiper(true);
			parkingRequest.setBackWiper(true);
			parkingRequest.setFuelCap(true);
			parkingRequest.setSpareTire(true);
			parkingRequest.setBattery(true);
			parkingRequest.setStarter(true);
			parkingRequest.setAerial(true);
			parkingRequest.setWheelCap(true);
			parkingRequest.setRoundMirror(true);
			parkingRequest.setTireIndicator(true);
			parkingRequest.setHasKeys(true);
			
			ParkingResponseDTO parkingResponse = parkingService.createParking(parkingRequest, request);
			
			return vehicleEquipmentResponseDTOMapper(vehicleEquipment, parkingResponse, null);
			
		}else if(vehicleEquipmentRequest.getService().equals("MAINTENANCE")) {
			// After creating, save also to parking
			
			MaintenanceRequestDTO maintenanceRequest = new MaintenanceRequestDTO();
			
//			Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findByNameAndBranch(vehicleEquipmentRequest.getParkingZoneName(), branch_.get());
//			if(parkingZone_.isEmpty())throw new NotFoundException("Parking Zone not found");
//			
//			parkingRequest.setParkingZoneName(vehicleEquipmentRequest.getParkingZoneName());

			maintenanceRequest.setOwnerFirstName(vehicleEquipment.getOwnerFirstName());
			maintenanceRequest.setOwnerMiddleName(vehicleEquipment.getOwnerMiddleName());
			maintenanceRequest.setOwnerLastName(vehicleEquipment.getOwnerLastName());
			maintenanceRequest.setOwnerCompanyName(vehicleEquipment.getOwnerCompanyName());
			maintenanceRequest.setOwnerIdNo(vehicleEquipment.getOwnerIdNo());
			maintenanceRequest.setOwnerIdType(vehicleEquipment.getOwnerIdType());
			maintenanceRequest.setOwnerPhoneNo(vehicleEquipment.getOwnerPhoneNo());
			maintenanceRequest.setOwnerEmail(vehicleEquipment.getOwnerEmail());
			maintenanceRequest.setOwnerAddress(vehicleEquipment.getOwnerAddress());
			maintenanceRequest.setVehicleEquipmentTypeName(vehicleEquipment.getVehicleEquipmentType().getName());
			maintenanceRequest.setVehicleEquipmentId(vehicleEquipment.getId());
			
			maintenanceRequest.setChasisNo(vehicleEquipment.getChasisNo());
						
			maintenanceRequest.setVehicleEquipmentColor(vehicleEquipment.getVehicleEquipmentColor());
			
			maintenanceRequest.setDeviceStatus(vehicleEquipment.isDeviceStatus());
			
			maintenanceRequest.setComments(vehicleEquipmentRequest.getComments());
			
				
			maintenanceRequest.setLeftFrontLamp(true);
			maintenanceRequest.setRightFrontLamp(true);
			maintenanceRequest.setLeftRearLamp(true);
			maintenanceRequest.setRightRearLamp(true);
			maintenanceRequest.setLeftSideMirror(true);
			maintenanceRequest.setRightSideMirror(true);
			maintenanceRequest.setLeftWiper(true);
			maintenanceRequest.setRightWiper(true);
			maintenanceRequest.setBackWiper(true);
			maintenanceRequest.setFuelCap(true);
			maintenanceRequest.setSpareTire(true);
			maintenanceRequest.setBattery(true);
			maintenanceRequest.setStarter(true);
			maintenanceRequest.setAerial(true);
			maintenanceRequest.setWheelCap(true);
			maintenanceRequest.setRoundMirror(true);
			maintenanceRequest.setTireIndicator(true);
			maintenanceRequest.setHasKeys(true);
			
			MaintenanceResponseDTO maintenanceResponse = maintenanceService.createMaintenance(maintenanceRequest, request);
			
			return vehicleEquipmentResponseDTOMapper(vehicleEquipment, null, maintenanceResponse);
		}
		
		return null;
		
	}

	@Override
	public VehicleEquipmentResponseDTO updateVehicleEquipment(VehicleEquipmentRequestDTO vehicleEquipmentRequest, HttpServletRequest request) {
		
		Optional<VehicleEquipment> vehicleEquipment_ = vehicleEquipmentRepository.findById(vehicleEquipmentRequest.getId());
		if(vehicleEquipment_.isEmpty()) {
			throw new NotFoundException("VehicleEquipment not found in database");
		}
		
		if(!validateVehicleEquipmentData(vehicleEquipmentRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) {
			throw new NotFoundException("Company not found");
		}
		
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(vehicleEquipmentRequest.getVehicleEquipmentTypeName(), company_.get());
		if(vehicleEquipmentType_.isEmpty()) {
			throw new NotFoundException("Vehicle or / equipment type not found");
		}
		if(vehicleEquipmentType_.get().getCompany().getId() != company_.get().getId()) {
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		}
		
		VehicleEquipment vehicleEquipment = vehicleEquipment_.get();
		vehicleEquipment.setOwnerFirstName(vehicleEquipmentRequest.getOwnerFirstName());
		vehicleEquipment.setOwnerMiddleName(vehicleEquipmentRequest.getOwnerMiddleName());
		vehicleEquipment.setOwnerLastName(vehicleEquipmentRequest.getOwnerLastName());
		vehicleEquipment.setOwnerCompanyName(vehicleEquipmentRequest.getOwnerCompanyName());
		vehicleEquipment.setOwnerIdNo(vehicleEquipmentRequest.getOwnerIdNo());
		vehicleEquipment.setOwnerIdType(vehicleEquipmentRequest.getOwnerIdType());
		vehicleEquipment.setOwnerPhoneNo(vehicleEquipmentRequest.getOwnerPhoneNo());
		vehicleEquipment.setOwnerEmail(vehicleEquipmentRequest.getOwnerEmail());
		vehicleEquipment.setOwnerAddress(vehicleEquipmentRequest.getOwnerAddress());
		vehicleEquipment.setRegistrationNo(vehicleEquipmentRequest.getRegistrationNo());
		vehicleEquipment.setChasisNo(vehicleEquipmentRequest.getChasisNo());
		vehicleEquipment.setCardNo(vehicleEquipmentRequest.getCardNo());
		vehicleEquipment.setTformNumber(vehicleEquipmentRequest.getTformNumber());
		
		vehicleEquipment.setAgentName(vehicleEquipmentRequest.getAgentName());
		vehicleEquipment.setAgentPhoneNo(vehicleEquipmentRequest.getAgentPhoneNo());
		vehicleEquipment.setAgentAddress(vehicleEquipmentRequest.getAgentAddress());
		vehicleEquipment.setAgentEmail(vehicleEquipmentRequest.getAgentEmail());
		
		//vehicleEquipment.setImage(vehicleEquipmentRequest.getImage());
				
		vehicleEquipment = vehicleEquipmentRepository.save(vehicleEquipment);
		
		return vehicleEquipmentResponseDTOMapper(vehicleEquipment, null, null);	
		
	}

	private VehicleEquipmentResponseDTO vehicleEquipmentResponseDTOMapper(
			VehicleEquipment vehicleEquipment, 
			ParkingResponseDTO parkingResponse, 
			MaintenanceResponseDTO maintenanceResponse) {
		VehicleEquipmentResponseDTO vehicleEquipmentResponse = new VehicleEquipmentResponseDTO();
		
		vehicleEquipmentResponse.setId(String.valueOf(vehicleEquipment.getId()));
		vehicleEquipmentResponse.setNo(vehicleEquipment.getNo());
		
		vehicleEquipmentResponse.setOwnerFirstName(vehicleEquipment.getOwnerFirstName());
		vehicleEquipmentResponse.setOwnerMiddleName(vehicleEquipment.getOwnerMiddleName());
		vehicleEquipmentResponse.setOwnerLastName(vehicleEquipment.getOwnerLastName());
		vehicleEquipmentResponse.setOwnerCompanyName(vehicleEquipment.getOwnerCompanyName());
		vehicleEquipmentResponse.setOwnerIdNo(vehicleEquipment.getOwnerIdNo());
		vehicleEquipmentResponse.setOwnerIdType(vehicleEquipment.getOwnerIdType());
		vehicleEquipmentResponse.setOwnerPhoneNo(vehicleEquipment.getOwnerPhoneNo());
		vehicleEquipmentResponse.setOwnerEmail(vehicleEquipment.getOwnerEmail());
		vehicleEquipmentResponse.setOwnerAddress(vehicleEquipment.getOwnerAddress());
		vehicleEquipmentResponse.setRegistrationNo(vehicleEquipment.getRegistrationNo());
		vehicleEquipmentResponse.setChasisNo(vehicleEquipment.getChasisNo());
		vehicleEquipmentResponse.setCardNo(vehicleEquipment.getCardNo());
		vehicleEquipmentResponse.setVehicleEquipmentColor(vehicleEquipment.getVehicleEquipmentColor());
		
		vehicleEquipmentResponse.setAgentName(vehicleEquipment.getAgentName());
		vehicleEquipmentResponse.setAgentPhoneNo(vehicleEquipment.getAgentPhoneNo());
		vehicleEquipmentResponse.setAgentAddress(vehicleEquipment.getAgentAddress());
		vehicleEquipmentResponse.setAgentEmail(vehicleEquipment.getAgentEmail());
		vehicleEquipmentResponse.setDeviceStatus(vehicleEquipment.isDeviceStatus() ? "1" : "0");
		
		vehicleEquipmentResponse.setTformNumber(vehicleEquipment.getTformNumber());
		
		//vehicleEquipment.setImage(vehicleEquipmentRequest.getImage());

		vehicleEquipmentResponse.setActive(vehicleEquipment.isActive() == true ? "Active" : "Inactive");
		//vehicleEquipmentResponse.setCompanyId(vehicleEquipment.getCompany().getId().toString());
		vehicleEquipmentResponse.setBranchId(vehicleEquipment.getBranch().getId().toString());
		
		vehicleEquipmentResponse.setVehicleEquipmentTypeName(vehicleEquipment.getVehicleEquipmentType().getName());
		
		if(parkingResponse != null) {
			if(parkingResponse.getId() != null) {
				vehicleEquipmentResponse.setParkingId(parkingResponse.getId().toString());
			}
		}else if(maintenanceResponse != null) {
			if(maintenanceResponse.getId() != null) {
				vehicleEquipmentResponse.setMaintenanceId(maintenanceResponse.getId().toString());
			}
		}
		
		return vehicleEquipmentResponse;
	}
	
	boolean validateVehicleEquipmentData(VehicleEquipmentRequestDTO vehicleEquipmentRequest) {
		
		return true;
	}

	
}
