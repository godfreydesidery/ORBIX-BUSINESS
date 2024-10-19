package com.orbix.api.api.vehicleandequipmentparking;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
	
	private final ParkingService parkingService;
	
	private final VehicleEquipmentTypeRepository vehicleEquipmentTypeRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	
	private final InvoiceReceivableRepository invoiceReceivableRepository;
	private final InvoiceReceivableDetailRepository invoiceReceivableDetailRepository;
	
	

	@Override
	public List<VehicleEquipmentResponseDTO> getAllVehicleEquipments(HttpServletRequest request) {
		List<VehicleEquipment> vehicleEquipments = vehicleEquipmentRepository.findAll();
		List<VehicleEquipmentResponseDTO> vehicleEquipmentResponses = new ArrayList<>();

		for(VehicleEquipment vehicleEquipment : vehicleEquipments) {
			vehicleEquipmentResponses.add(vehicleEquipmentResponseDTOMapper(vehicleEquipment, null));					
		}		
		return vehicleEquipmentResponses;
	}
	
	@Override
	public List<VehicleEquipmentResponseDTO> getAllActiveVehicleEquipments(HttpServletRequest request) {
		List<VehicleEquipment> vehicleEquipments = vehicleEquipmentRepository.findAllByActiveTrue();
		List<VehicleEquipmentResponseDTO> vehicleEquipmentResponses = new ArrayList<>();

		for(VehicleEquipment vehicleEquipment : vehicleEquipments) {
			vehicleEquipmentResponses.add(vehicleEquipmentResponseDTOMapper(vehicleEquipment, null));					
		}		
		return vehicleEquipmentResponses;
	}

	@Override
	public VehicleEquipmentResponseDTO get(Long id, HttpServletRequest request) {		
		Optional<VehicleEquipment> vehicleEquipment_ = vehicleEquipmentRepository.findById(id);
		if(vehicleEquipment_.isEmpty()) {
			throw new NotFoundException("VehicleEquipment not found");
		}		
		return vehicleEquipmentResponseDTOMapper(vehicleEquipment_.get(), null);	
	}
	
	@Override
	public VehicleEquipmentResponseDTO getByChasisNo(String chasisNo, HttpServletRequest request) {		
		Optional<VehicleEquipment> vehicleEquipment_ = vehicleEquipmentRepository.findByChasisNoAndActiveTrue(chasisNo);
		if(vehicleEquipment_.isEmpty()) {
			throw new NotFoundException("VehicleEquipment not found");
		}
		
		
		// Now find the parking with pending or checked in
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");
		
		ParkingResponseDTO parkingResponse = new ParkingResponseDTO();
		
		List<Parking> parkings = parkingRepository.findAllByVehicleEquipmentAndStatusIn(vehicleEquipment_.get(), statuses);
		for(Parking p : parkings) {
			parkingResponse.setId(p.getId().toString());
		}
		
		return vehicleEquipmentResponseDTOMapper(vehicleEquipment_.get(), parkingResponse);	
	}

	@Override
	public VehicleEquipmentResponseDTO createVehicleEquipment(VehicleEquipmentRequestDTO vehicleEquipmentRequest, HttpServletRequest request) {
		
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
		//vehicleEquipment.setImage(vehicleEquipmentRequest.getImage());
		vehicleEquipment.setActive(true);
		
		vehicleEquipment.setVehicleEquipmentType(vehicleEquipmentType_.get());		
		
		vehicleEquipment.setCompany(company_.get());
		vehicleEquipment.setBranch(branch_.get());
		
		vehicleEquipment.setCreatedByUser(userService.getUser(request));
		vehicleEquipment.setCreatedDateTime(dayService.getTimeStamp());
		
		vehicleEquipment = vehicleEquipmentRepository.save(vehicleEquipment);
		/**Create  vehicleEquipment no*/
		vehicleEquipment.setNo("PKN/"+ vehicleEquipment.getId().toString());
		vehicleEquipment = vehicleEquipmentRepository.save(vehicleEquipment);
		
		// After creating, save also to parking
		
		ParkingRequestDTO parkingRequest = new ParkingRequestDTO();

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
		
		ParkingResponseDTO parkingResponse = parkingService.createParking(parkingRequest, request);
		
		return vehicleEquipmentResponseDTOMapper(vehicleEquipment, parkingResponse);
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
		//vehicleEquipment.setImage(vehicleEquipmentRequest.getImage());
				
		vehicleEquipment = vehicleEquipmentRepository.save(vehicleEquipment);
		
		return vehicleEquipmentResponseDTOMapper(vehicleEquipment, null);	
		
	}

	private VehicleEquipmentResponseDTO vehicleEquipmentResponseDTOMapper(VehicleEquipment vehicleEquipment, ParkingResponseDTO parkingResponse) {
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
		
		//vehicleEquipment.setImage(vehicleEquipmentRequest.getImage());

		vehicleEquipmentResponse.setActive(vehicleEquipment.isActive() == true ? "Active" : "Inactive");
		vehicleEquipmentResponse.setCompanyId(vehicleEquipment.getCompany().getId().toString());
		vehicleEquipmentResponse.setBranchId(vehicleEquipment.getBranch().getId().toString());
		
		vehicleEquipmentResponse.setVehicleEquipmentTypeName(vehicleEquipment.getVehicleEquipmentType().getName());
		
		if(parkingResponse != null) {
			if(parkingResponse.getId() != null) {
				vehicleEquipmentResponse.setParkingId(parkingResponse.getId().toString());
			}
		}
		
		
		return vehicleEquipmentResponse;
	}
	
	boolean validateVehicleEquipmentData(VehicleEquipmentRequestDTO vehicleEquipmentRequest) {
		
		return true;
	}
}
