package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.api.commons.PayStatus;
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
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.warehouse.RemovedGood;
import com.orbix.api.modules.warehouse.Storage;
import com.orbix.api.modules.warehouse.StorageBillReceivable;
import com.orbix.api.api.commons.PageRequests;
import com.orbix.api.api.commons.PageResponseDTO;

import lombok.Data;
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
	
	private final RemovedVehicleEquipmentRepository removedVehicleEquipmentRepository;

	private final ParkingTripRepository parkingTripRepository;

	private final ParkingSignatureRepository parkingSignatureRepository;

	private static final List<String> CATEGORIES = List.of("IN-TRANSIT", "LOCAL");
	/** A parking still at the yard: inspected (PENDING), registered (REGISTERED) or checked in */
	private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "REGISTERED", "CHECKED-IN");

		
	

	private static final int IN_CLAUSE_CHUNK_SIZE = 1000;

	// Bills of all the given parkings in a few batched queries, grouped by parking id (same id order as findAllByParking)
	private Map<Long, List<ParkingBillReceivable>> getParkingBillReceivablesByParking(List<Parking> parkings) {
		Map<Long, List<ParkingBillReceivable>> parkingBillReceivablesByParking = new HashMap<>();
		for(int i = 0; i < parkings.size(); i += IN_CLAUSE_CHUNK_SIZE) {
			List<Parking> chunk = parkings.subList(i, Math.min(i + IN_CLAUSE_CHUNK_SIZE, parkings.size()));
			for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivableRepository.findAllByParkingIn(chunk)) {
				parkingBillReceivablesByParking.computeIfAbsent(parkingBillReceivable.getParking().getId(), k -> new ArrayList<>()).add(parkingBillReceivable);
			}
		}
		return parkingBillReceivablesByParking;
	}

	// Service bills of all the given parkings in a few batched queries, grouped by parking id (same id order as findAllByParking)
	private Map<Long, List<ParkingServiceBillReceivable>> getParkingServiceBillReceivablesByParking(List<Parking> parkings) {
		Map<Long, List<ParkingServiceBillReceivable>> parkingServiceBillReceivablesByParking = new HashMap<>();
		for(int i = 0; i < parkings.size(); i += IN_CLAUSE_CHUNK_SIZE) {
			List<Parking> chunk = parkings.subList(i, Math.min(i + IN_CLAUSE_CHUNK_SIZE, parkings.size()));
			for(ParkingServiceBillReceivable parkingServiceBillReceivable : parkingServiceBillReceivableRepository.findAllByParkingIn(chunk)) {
				parkingServiceBillReceivablesByParking.computeIfAbsent(parkingServiceBillReceivable.getParking().getId(), k -> new ArrayList<>()).add(parkingServiceBillReceivable);
			}
		}
		return parkingServiceBillReceivablesByParking;
	}

	// Cleared when every parking bill and service bill is PAID (a parking without bills is cleared)
	private boolean isCleared(List<ParkingBillReceivable> parkingBillReceivables, List<ParkingServiceBillReceivable> parkingServiceBillReceivables) {
		boolean cleared = true;
		if(!parkingBillReceivables.isEmpty() && cleared == true) {
			for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
				if(!parkingBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
					cleared = false;
					break;
				}
			}
		}
		if(!parkingServiceBillReceivables.isEmpty() && cleared == true) {
			for(ParkingServiceBillReceivable parkingServiceBillReceivable : parkingServiceBillReceivables) {
				if(!parkingServiceBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
					cleared = false;
					break;
				}
			}
		}
		return cleared;
	}

	@Override
	public List<ParkingResponseDTO> getAllParkings(HttpServletRequest request) {
		List<Parking> parkings = parkingRepository.findAll();

		// The mapper lists the bills of checked out parkings; load them for all such parkings at once
		List<Parking> checkedOutParkings = new ArrayList<>();
		for(Parking parking : parkings) {
			if("CHECKED-OUT".equals(parking.getStatus())) checkedOutParkings.add(parking);
		}
		Map<Long, List<ParkingBillReceivable>> parkingBillReceivablesByParking = getParkingBillReceivablesByParking(checkedOutParkings);
		Map<Long, List<ParkingServiceBillReceivable>> parkingServiceBillReceivablesByParking = getParkingServiceBillReceivablesByParking(checkedOutParkings);

		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();
		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking, parkingBillReceivablesByParking, parkingServiceBillReceivablesByParking));
		}
		return parkingResponses;
	}
	
	@Override
	public List<ParkingResponseDTO> getAllPendingOrCheckedInParkings(HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("REGISTERED");
		statuses.add("CHECKED-IN");
		
		List<Parking> parkings = parkingRepository.findAllByStatusIn(statuses);
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking));					
		}		
		return parkingResponses;
	}
	
	@Override
	public List<ParkingResponseDTO> getAllCleared(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		List<Parking> parkings = parkingRepository.findAllByStatusIn(statuses);
		Map<Long, List<ParkingBillReceivable>> parkingBillReceivablesByParking = getParkingBillReceivablesByParking(parkings);
		Map<Long, List<ParkingServiceBillReceivable>> parkingServiceBillReceivablesByParking = getParkingServiceBillReceivablesByParking(parkings);
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			boolean cleared = isCleared(
					parkingBillReceivablesByParking.getOrDefault(parking.getId(), Collections.emptyList()),
					parkingServiceBillReceivablesByParking.getOrDefault(parking.getId(), Collections.emptyList()));

			if(cleared) parkingResponses.add(parkingResponseDTOMapper(parking, parkingBillReceivablesByParking, parkingServiceBillReceivablesByParking));
		}
		return parkingResponses;
	}

	@Override
	public List<ParkingResponseDTO> getTodayCheckedOut(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-OUT");
		
		// Calculate the range
		LocalDateTime startOfToday = LocalDateTime.now().minusDays(1).with(LocalTime.MAX);
		LocalDateTime endOfYesterday = LocalDateTime.now().plusDays(1).with(LocalTime.MIN);

		List<Parking> parkings = parkingRepository.findAllByStatusInAndCheckedOutDateTimeBetween(statuses, startOfToday, endOfYesterday);

		//List<Parking> parkings = parkingRepository.findAllByStatusInAndCheckedOutBetween(statuses, LocalDateTime.now().);
		Map<Long, List<ParkingBillReceivable>> parkingBillReceivablesByParking = getParkingBillReceivablesByParking(parkings);
		Map<Long, List<ParkingServiceBillReceivable>> parkingServiceBillReceivablesByParking = getParkingServiceBillReceivablesByParking(parkings);
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			boolean cleared = isCleared(
					parkingBillReceivablesByParking.getOrDefault(parking.getId(), Collections.emptyList()),
					parkingServiceBillReceivablesByParking.getOrDefault(parking.getId(), Collections.emptyList()));

			if(cleared) parkingResponses.add(parkingResponseDTOMapper(parking, parkingBillReceivablesByParking, parkingServiceBillReceivablesByParking));
		}
		return parkingResponses;
	}
	
	@Override
	public List<ParkingResponseDTO> getRecentCheckedOut(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-OUT");
		
		// Calculate the range
		LocalDateTime now = dayService.getTimeStamp(); //LocalDateTime.now();
		LocalDateTime before = now.minusHours(24);


		List<Parking> parkings = parkingRepository.findAllByStatusInAndCheckedOutDateTimeBetween(statuses, before, now);

		//List<Parking> parkings = parkingRepository.findAllByStatusInAndCheckedOutBetween(statuses, LocalDateTime.now().);
		Map<Long, List<ParkingBillReceivable>> parkingBillReceivablesByParking = getParkingBillReceivablesByParking(parkings);
		Map<Long, List<ParkingServiceBillReceivable>> parkingServiceBillReceivablesByParking = getParkingServiceBillReceivablesByParking(parkings);
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			boolean cleared = isCleared(
					parkingBillReceivablesByParking.getOrDefault(parking.getId(), Collections.emptyList()),
					parkingServiceBillReceivablesByParking.getOrDefault(parking.getId(), Collections.emptyList()));

			if(cleared) parkingResponses.add(parkingResponseDTOMapper(parking, parkingBillReceivablesByParking, parkingServiceBillReceivablesByParking));
		}
		return parkingResponses;
	}
	
	@Override
	public PageResponseDTO<ParkingResponseDTO> getPendingOrCheckedInParkingPage(int page, int size, String search, String status, HttpServletRequest request) {

		// status: one or more of PENDING (awaiting registration), REGISTERED (awaiting check-in) and CHECKED-IN,
		// separated by commas; empty for all three
		List<String> statuses = new ArrayList<>();
		for(String one : (status == null ? "" : status).split(",")) {
			String value = one.trim().toUpperCase();
			if(ACTIVE_STATUSES.contains(value) && !statuses.contains(value)) statuses.add(value);
		}
		if(statuses.isEmpty()) statuses.addAll(ACTIVE_STATUSES);

		// One page, newest first (the screen showed the full list reversed), searched on the shown columns
		Page<Parking> parkings = parkingRepository.getPageByStatusIn(statuses, PageRequests.searchPattern(search), PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking));
		}
		return new PageResponseDTO<>(parkingResponses, parkings.getTotalElements());
	}

	@Override
	public PageResponseDTO<ParkingResponseDTO> getCheckedInParkingPage(int page, int size, String search, HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");

		// One page, newest first (the screen showed the full list reversed), searched on the shown columns
		Page<Parking> parkings = parkingRepository.getPageByStatusIn(statuses, PageRequests.searchPattern(search), PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking));
		}
		return new PageResponseDTO<>(parkingResponses, parkings.getTotalElements());
	}

	@Override
	public PageResponseDTO<ParkingResponseDTO> getWithDiscountsParkingPage(int page, int size, String search, HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");

		// Checked-in parkings with a requested discount; one page, newest first, searched on the shown columns
		Page<Parking> parkings = parkingRepository.getPageByStatusInAndDiscountStatus(statuses, "Requested", PageRequests.searchPattern(search), PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking));
		}
		return new PageResponseDTO<>(parkingResponses, parkings.getTotalElements());
	}

	@Override
	public PageResponseDTO<ParkingResponseDTO> getRecentCheckedOutParkingPage(int page, int size, String search, HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-OUT");

		// Same 24 hour window as the full list
		LocalDateTime now = dayService.getTimeStamp();
		LocalDateTime before = now.minusHours(24);

		// Cleared parkings only, filtered in the query so that paging counts the right rows; one page, searched on the shown columns
		Page<Parking> parkings = parkingRepository.getClearedPageByStatusInAndCheckedOutDateTimeBetween(statuses, before, now, PayStatus.PAID, PageRequests.searchPattern(search), PageRequests.of(page, size, Sort.by("id")));
		Map<Long, List<ParkingBillReceivable>> parkingBillReceivablesByParking = getParkingBillReceivablesByParking(parkings.getContent());
		Map<Long, List<ParkingServiceBillReceivable>> parkingServiceBillReceivablesByParking = getParkingServiceBillReceivablesByParking(parkings.getContent());
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();

		for(Parking parking : parkings) {
			parkingResponses.add(parkingResponseDTOMapper(parking, parkingBillReceivablesByParking, parkingServiceBillReceivablesByParking));
		}
		return new PageResponseDTO<>(parkingResponses, parkings.getTotalElements());
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
	public List<ParkingResponseDTO> getAllWithDiscounts(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		// Load only the bills with a requested discount, instead of every bill of every checked-in parking
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByDiscountStatusAndParking_StatusIn("Requested", statuses);
		Map<Long, Parking> parkings = new LinkedHashMap<>();
		for(ParkingBillReceivable pbr : parkingBillReceivables) {
			if(pbr.getDiscountStatus() != null && pbr.getDiscountStatus().equals("Requested")) {
				parkings.putIfAbsent(pbr.getParking().getId(), pbr.getParking());
			}
		}

		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();
		for(Parking parking : parkings.values()) {
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
	public ParkingInspectionResponseDTO getInspection(Long id, HttpServletRequest request) {
		Parking parking = parkingRepository.findById(id).orElseThrow(() -> new NotFoundException("Parking not found"));

		ParkingInspectionResponseDTO inspection = new ParkingInspectionResponseDTO();
		inspection.setParkingId(String.valueOf(parking.getId()));

		List<ParkingInspectionResponseDTO.Signature> signatures = new ArrayList<>();
		for(ParkingSignature parkingSignature : parkingSignatureRepository.findAllByParkingOrderBySignedAtAscIdAsc(parking)) {
			ParkingInspectionResponseDTO.Signature signature = new ParkingInspectionResponseDTO.Signature();
			signature.setStage(parkingSignature.getStage());
			signature.setRole(parkingSignature.getRole());
			signature.setSignerName(parkingSignature.getSignerName());
			signature.setSignedAt(parkingSignature.getSignedAt() == null ? "" : parkingSignature.getSignedAt().toString());
			signature.setImage(SignatureImages.encode(parkingSignature.getImage()));
			signature.setChanges(parkingSignature.getChanges());
			signatures.add(signature);
		}
		inspection.setSignatures(signatures);
		return inspection;
	}

	@Override
	public List<ParkingBillReceivableResponseDTO> getParkingBillReceivables(Long id, HttpServletRequest request) {		
		Optional<Parking> parking_ = parkingRepository.findById(id);
		if(parking_.isEmpty()) {
			throw new NotFoundException("Parking not found");
		}
		
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking_.get()));
		
		List<ParkingBillReceivableResponseDTO> parkingBillReceivableResponses = new ArrayList<>();
		
		for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
			parkingBillReceivableResponses.add(parkingBillReceivableDTOMapper(parkingBillReceivable));
		}
		if (parkingBillReceivableResponses.isEmpty()) return null;	
		
		return parkingBillReceivableResponses;
	}

	@Override
	public ParkingResponseDTO createParking(ParkingRequestDTO parkingRequest, HttpServletRequest request) {
		
		if (parkingRequest.getChasisNo() != null && !parkingRequest.getChasisNo().trim().isEmpty()) {
			List<String> statuses = new ArrayList<>();
			statuses.add("PENDING");
			statuses.add("REGISTERED");
			statuses.add("CHECKED-IN");
		    if (parkingRepository.existsByChasisNoAndStatusIn(parkingRequest.getChasisNo(), statuses)) {
		        throw new InvalidOperationException("Chasis number already exist");
		    }
		}
		
		/**Validate data*/
		if(!validateParkingData(parkingRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		// What the inspection must have; the database itself requires none of the new values
		required(parkingRequest.getChasisNo(), "Chassis number is required");
		required(parkingRequest.getBodyCondition(), "Body condition is required");
		String category = category(parkingRequest);
		byte[] inspectorSignature = SignatureImages.decode(parkingRequest.getInspectorSignature(), "Inspector");
		byte[] driverSignature = SignatureImages.decode(parkingRequest.getDriverSignature(), "Driver");
		Integer deviceCount = deviceCount(parkingRequest, true);
		
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
		if(!Objects.equals(vehicleEquipmentType_.get().getCompany().getId(), company_.get().getId())) {
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		}
		
		// An existing vehicle (Existing Vehicle), or a new one registered with this inspection, in the same transaction
		VehicleEquipment vehicleEquipment = parkingRequest.getVehicleEquipmentId() != null
				? vehicleEquipmentRepository.findById(parkingRequest.getVehicleEquipmentId()).orElseThrow(() -> new NotFoundException("Vehicle and equipment not found"))
				: vehicleForInspection(parkingRequest, vehicleEquipmentType_.get(), branch_.get(), request);
		
		// The parking zone and slot are chosen at registration; the vehicle type and category here, at inspection

		Parking parking = new Parking();
		parking.setNo(String.valueOf(Math.random()));
		// The registrar enters the owner; until then the owner is the placeholder
		parking.setOwnerFirstName(Parking.ownerFirstNameOrPending(parkingRequest.getOwnerFirstName()));
		parking.setOwnerMiddleName(parkingRequest.getOwnerMiddleName());
		parking.setOwnerLastName(Parking.ownerLastNameOrPending(parkingRequest.getOwnerLastName()));
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
		parking.setHasKeys(true);
		parking.setDeviceStatus(parkingRequest.isDeviceStatus());
		
		parking.setComments(parkingRequest.getComments());
		
		parking.setBillingType("DAILY");
		
		//parking.setImage(parkingRequest.getImage());
		parking.setStatus("PENDING");
		parking.setVehicleEquipmentType(vehicleEquipmentType_.get());
		parking.setVehicleEquipmentCategory(category);
		
		parking.setVehicleEquipmentName(vehicleEquipmentType_.get().getName()); // Look here later
		parking.setVehicleEquipmentColor(parkingRequest.getVehicleEquipmentColor());
		
		parking.setVehicleEquipment(vehicleEquipment);

		parking.setInteriorDetails(text(parkingRequest.getInteriorDetails()));
		parking.setBodyCondition(text(parkingRequest.getBodyCondition()));

		parking.setBranch(branch_.get());

		User user = userService.getUser(request);
		parking.setCreatedByUser(user);
		parking.setCreatedDateTime(dayService.getTimeStamp());

		parking = parkingRepository.save(parking);
		/**Create  parking no*/
		parking.setNo("PKN/"+ parking.getId().toString());
		parking.setDeviceCount(deviceCount);
		parking = parkingRepository.save(parking);

		sign(parking, ParkingSignature.INSPECTION, ParkingSignature.INSPECTOR, user, nameOf(user), inspectorSignature, null);
		// The driver's name is put under this signature when the registrar enters the driver's details
		sign(parking, ParkingSignature.INSPECTION, ParkingSignature.DRIVER, null, null, driverSignature, null);
		
		
		//parking.setParkingStatus(parkingRequest.getParkingStatus() != null ? parkingRequest.getParkingStatus() : "PENDING");

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
			
		if(!"PENDING".equals(parking_.get().getStatus()) && !"REGISTERED".equals(parking_.get().getStatus()))
			throw new InvalidOperationException("Can not edit the inspection: the vehicle is already checked in (use Modify)");
			
		if(!validateParkingData(parkingRequest)) throw new InvalidEntryException("Could not validate data");
			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
		
		
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(parkingRequest.getVehicleEquipmentTypeName(), company_.get());
		if(vehicleEquipmentType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");
			
		
		if(!Objects.equals(vehicleEquipmentType_.get().getCompany().getId(), company_.get().getId())) 
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		
		// The parking zone belongs to registration: editing an inspection leaves it as it is
		String editedCategory = category(parkingRequest);
		
		Parking parking = parking_.get();
		parking.setOwnerFirstName(Parking.ownerFirstNameOrPending(parkingRequest.getOwnerFirstName()));
		parking.setOwnerMiddleName(parkingRequest.getOwnerMiddleName());
		parking.setOwnerLastName(Parking.ownerLastNameOrPending(parkingRequest.getOwnerLastName()));
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
		parking.setHasKeys(parkingRequest.isHasKeys());
		parking.setDeviceStatus(parkingRequest.isDeviceStatus());
		parking.setVehicleEquipmentType(vehicleEquipmentType_.get());
		parking.setVehicleEquipmentCategory(editedCategory);

		parking.setVehicleEquipmentName(vehicleEquipmentType_.get().getName()); // Look here later
		parking.setVehicleEquipmentColor(parkingRequest.getVehicleEquipmentColor());

		parking.setComments(parkingRequest.getComments());

		parking.setInteriorDetails(text(parkingRequest.getInteriorDetails()));
		parking.setBodyCondition(text(parkingRequest.getBodyCondition()));
		parking.setDeviceCount(deviceCount(parkingRequest, false));

		if(parkingRequest.getBillingType() != null) parking.setBillingType(parkingRequest.getBillingType());

		parking = parkingRepository.save(parking);

		return parkingResponseDTOMapper(parking);
	}
	
	@Override
	public ParkingResponseDTO modifyParking(ParkingRequestDTO parkingRequest, HttpServletRequest request) {
		
		Optional<Parking> parking_ = parkingRepository.findById(parkingRequest.getId());
		if(parking_.isEmpty()) throw new NotFoundException("Parking not found in database");
			
		if(!parking_.get().getStatus().equals("CHECKED-IN")) throw new NotFoundException("Can not modify only checked-in parking can be modified");
			
		if(!validateParkingData(parkingRequest)) throw new InvalidEntryException("Could not validate data");
			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
		Optional<VehicleEquipmentType> vehicleEquipmentType_ = vehicleEquipmentTypeRepository.findByNameAndCompany(parkingRequest.getVehicleEquipmentTypeName(), company_.get());
		if(vehicleEquipmentType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");
			
		
		if(!Objects.equals(vehicleEquipmentType_.get().getCompany().getId(), company_.get().getId())) 
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		
		Optional<ParkingZone> parkingZone_ = parkingZoneRepository.findByNameAndBranch(parkingRequest.getParkingZoneName(), branch_.get());
		if(parkingZone_.isEmpty())throw new NotFoundException("Parking Zone not found");
		
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking_.get()));

		if(!parkingBillReceivables.isEmpty()) {
			throw new InvalidOperationException("Cannot proceed with modification as there are already existing bills.");
		}

		// After registration the owner is a real name: no blank, and not the placeholder
		String modifiedOwnerFirstName = required(parkingRequest.getOwnerFirstName(), "Owner first name is required");
		String modifiedOwnerLastName = required(parkingRequest.getOwnerLastName(), "Owner last name is required");
		if(Parking.isOwnerPending(modifiedOwnerFirstName, modifiedOwnerLastName)) throw new InvalidEntryException("Enter the owner's name");
		byte[] modifierSignature = SignatureImages.decode(parkingRequest.getModifierSignature(), "Your");

		// Moving the vehicle to another zone or slot takes a space there, so it must be free
		ParkingZone currentZone = parking_.get().getParkingZone();
		Integer slotNo = parkingZone_.get().getNoOfSlots() > 0 ? parkingRequest.getParkingSlotNo() : null;
		boolean zoneChanged = currentZone == null || !Objects.equals(currentZone.getId(), parkingZone_.get().getId());
		if(zoneChanged || !Objects.equals(parking_.get().getParkingSlotNo(), slotNo)) {
			zoneWithSpace(parkingZone_.get(), slotNo, parking_.get());
		}

		Parking parking = parking_.get();
		// What the record looked like, to keep what changed beside the signature
		Map<String, String> before = snapshot(parking);
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
		parking.setHasKeys(parkingRequest.isHasKeys());
		parking.setDeviceStatus(parkingRequest.isDeviceStatus());
		parking.setVehicleEquipmentType(vehicleEquipmentType_.get());
		parking.setVehicleEquipmentCategory(parkingRequest.getVehicleEquipmentCategory());
		
		parking.setVehicleEquipmentName(vehicleEquipmentType_.get().getName()); // Look here later
		parking.setVehicleEquipmentColor(parkingRequest.getVehicleEquipmentColor());
		
		parking.setComments(parkingRequest.getComments());
		
		if(parkingRequest.startBillingAt == null) {
			parking.setStartBillingAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this depending on user billing preferences
		}else {
			
			//String dateString = "2024-10-26 15:30:45" ;
			String dateString = parkingRequest.getStartBillingAt() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
			parking.setStartBillingAt(dateTime.plusDays(1));
		}	
		
		
		
		parking.setParkingZone(parkingZone_.get());
		parking.setParkingSlotNo(slotNo);

		parking.setBillingType(parkingRequest.getBillingType());

		parking.setBillingAmount(parking.getVehicleEquipmentType().getDailyPrice());

		parking.setInteriorDetails(text(parkingRequest.getInteriorDetails()));
		parking.setBodyCondition(text(parkingRequest.getBodyCondition()));
		parking.setDriverName(text(parkingRequest.getDriverName()));
		parking.setDriverPhoneNo(text(parkingRequest.getDriverPhoneNo()));
		parking.setDriverIdType(text(parkingRequest.getDriverIdType()));
		parking.setDriverIdNo(text(parkingRequest.getDriverIdNo()));
		parking.setDeviceCount(deviceCount(parkingRequest, false));

		parking = parkingRepository.save(parking);

		User user = userService.getUser(request);
		String changes = changes(before, snapshot(parking));
		if(parking.getDriverName() != null) parkingSignatureRepository.nameDriver(parking, parking.getDriverName());
		sign(parking, ParkingSignature.MODIFICATION, ParkingSignature.MODIFIER, user, nameOf(user), modifierSignature, changes.isEmpty() ? "No changes" : changes);

		return parkingResponseDTOMapper(parking);
	}

	private ParkingResponseDTO parkingResponseDTOMapper(Parking parking) {
		return parkingResponseDTOMapper(parking, null, null);
	}

	// Takes the bills from the preloaded maps when given, otherwise queries them for this parking
	private ParkingResponseDTO parkingResponseDTOMapper(Parking parking,
			Map<Long, List<ParkingBillReceivable>> parkingBillReceivablesByParking,
			Map<Long, List<ParkingServiceBillReceivable>> parkingServiceBillReceivablesByParking) {
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
		parkingResponse.setHasKeys(parking.isHasKeys() ? "1" : "0");
		parkingResponse.setDeviceStatus(parking.isDeviceStatus() ? "1" : "0");
		parkingResponse.setDeviceCount(parking.getDeviceCount() == null ? "" : String.valueOf(parking.getDeviceCount()));
		parkingResponse.setParkingSlotNo(parking.getParkingSlotNo() == null ? "" : String.valueOf(parking.getParkingSlotNo()));
		parkingResponse.setVehicleEquipmentId(parking.getVehicleEquipment() == null ? "" : String.valueOf(parking.getVehicleEquipment().getId()));
		parkingResponse.setVehicleEquipmentCategory(parking.getVehicleEquipmentCategory());
		parkingResponse.setVehicleEquipmentName(parking.getVehicleEquipmentName());
		parkingResponse.setVehicleEquipmentColor(parking.getVehicleEquipmentColor());
		parkingResponse.setBillingStartAt(
			    Optional.ofNullable(parking.getStartBillingAt())
			            .map(Object::toString)
			            .orElse("")
			);
		
				
		parkingResponse.setParkingZoneName(
			    Optional.ofNullable(parking.getParkingZone())
			            .map(ParkingZone::getName)
			            .orElse("")
			);
		
		parkingResponse.setComments(parking.getComments());
		parkingResponse.setInteriorDetails(parking.getInteriorDetails());
		parkingResponse.setBodyCondition(parking.getBodyCondition());
		parkingResponse.setDriverName(parking.getDriverName());
		parkingResponse.setDriverPhoneNo(parking.getDriverPhoneNo());
		parkingResponse.setDriverIdType(parking.getDriverIdType());
		parkingResponse.setDriverIdNo(parking.getDriverIdNo());
		//parking.setImage(parkingRequest.getImage());
		parkingResponse.setStatus(parking.getStatus());
		parkingResponse.setCustody(parking.isOutside() ? Parking.OUTSIDE : Parking.INSIDE);
		//parkingResponse.setCompanyId(parking.getCompany().getId().toString());
		parkingResponse.setBranchId(parking.getBranch().getId().toString());
		
		parkingResponse.setBillingType(parking.getBillingType());
		parkingResponse.setBillingAmount(String.valueOf(parking.getBillingAmount()));
		
		parkingResponse.setVehicleEquipmentTypeName(parking.getVehicleEquipmentType().getName());
		
		parkingResponse.setParkingZoneName(
				parking.getParkingZone() != null && parking.getParkingZone().getName() != null
		        ? parking.getParkingZone().getName() 
		        : "");
		
		if(parking.getStatus().equals("CHECKED-OUT")) {
			List<ServiceBillItem> items = new ArrayList<>();
		
			List<ParkingBillReceivable> pbs = parkingBillReceivablesByParking != null
					? parkingBillReceivablesByParking.getOrDefault(parking.getId(), Collections.emptyList())
					: parkingBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking));
			List<ParkingServiceBillReceivable> psbs = parkingServiceBillReceivablesByParking != null
					? parkingServiceBillReceivablesByParking.getOrDefault(parking.getId(), Collections.emptyList())
					: parkingServiceBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking));
			int sn = 1;
			for(ParkingBillReceivable pbr : pbs) {
				ServiceBillItem sbi = new ServiceBillItem();
				sbi.setSn(sn);
				sbi.setItem(pbr.getBillReceivable().getSummary());
				sbi.setQty(pbr.getQty());
				sbi.setAmount(pbr.getBillReceivable().getAmount());
				sbi.setPayStatus(pbr.getBillReceivable().getPayStatus().toString());
				items.add(sbi);
			}
			
			sn = 1;
			for(ParkingServiceBillReceivable psbr : psbs) {
				ServiceBillItem sbi = new ServiceBillItem();
				sbi.setSn(sn);
				sbi.setItem(psbr.getDescription());
				sbi.setQty(psbr.getQty());
				sbi.setAmount(psbr.getBillReceivable().getAmount());
				sbi.setPayStatus(psbr.getBillReceivable().getPayStatus().toString());
				items.add(sbi);
			}
			
			parkingResponse.setServiceBillItems(items);
		}
			
		return parkingResponse;
	}
	
	private static String text(String value) {
		if(value == null) return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static String required(String value, String message) {
		String trimmed = text(value);
		if(trimmed == null) throw new InvalidEntryException(message);
		return trimmed;
	}

	private static String nameOf(User user) {
		if(user == null) return null;
		String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " " + (user.getLastName() == null ? "" : user.getLastName())).trim();
		if(name.isEmpty()) name = user.getNickname();
		if(name == null || name.trim().isEmpty()) name = user.getUsername();
		return name;
	}

	/**
	 * The zone, locked until the transaction ends so two vehicles cannot both take its last space or the same slot,
	 * once it is known to be active and to have room: a free space and, in a zone with slots, the chosen slot free.
	 * No of slots 0 means no limit and no slots. 'moving' is a checked-in vehicle changing zone or slot (Modify),
	 * which already counts towards the zone it is in and may keep its own slot.
	 */
	private ParkingZone zoneWithSpace(ParkingZone zone, Integer slotNo, Parking moving) {
		ParkingZone parkingZone = parkingZoneRepository.findByIdForUpdate(zone.getId()).orElseThrow(() -> new NotFoundException("Parking Zone not found"));
		if(!parkingZone.isActive()) throw new InvalidOperationException("Parking Zone " + parkingZone.getName() + " is not active");
		if(parkingZone.getNoOfSlots() <= 0) return parkingZone;
		// A zone with numbered slots is full when every slot number is held; vehicles parked before slots existed hold none
		boolean holdsSlotHere = moving != null && moving.getParkingZone() != null && Objects.equals(moving.getParkingZone().getId(), parkingZone.getId())
				&& moving.getParkingSlotNo() != null && moving.getParkingSlotNo() >= 1 && moving.getParkingSlotNo() <= parkingZone.getNoOfSlots();
		if(!holdsSlotHere && parkingRepository.countTakenSlots(parkingZone, parkingZone.getNoOfSlots()) >= parkingZone.getNoOfSlots())
			throw new InvalidOperationException("Parking Zone " + parkingZone.getName() + " is full, select another zone");
		if(slotNo == null || slotNo < 1 || slotNo > parkingZone.getNoOfSlots())
			throw new InvalidEntryException("Select a slot in Parking Zone " + parkingZone.getName() + " (1 to " + parkingZone.getNoOfSlots() + ")");
		if(parkingRepository.isSlotTaken(parkingZone, slotNo, moving == null ? -1L : moving.getId()))
			throw new InvalidOperationException("Slot " + slotNo + " in Parking Zone " + parkingZone.getName() + " is taken, select another slot");
		return parkingZone;
	}

	/**
	 * The vehicle of a new inspection submitted in one step: one already registered with this chassis and still
	 * waiting for its inspection, or else a new one. Its owner is the placeholder until registration.
	 */
	private VehicleEquipment vehicleForInspection(ParkingRequestDTO parkingRequest, VehicleEquipmentType vehicleEquipmentType, Branch branch, HttpServletRequest request) {
		String chasisNo = required(parkingRequest.getChasisNo(), "Chassis number is required");
		List<VehicleEquipment> waiting = vehicleEquipmentRepository.findAwaitingInspection(chasisNo, branch);
		VehicleEquipment vehicleEquipment = waiting.isEmpty() ? new VehicleEquipment() : waiting.get(0);
		if(vehicleEquipment.getId() == null) {
			vehicleEquipment.setNo(String.valueOf(Math.random()));
			vehicleEquipment.setOwnerFirstName(Parking.OWNER_PENDING_FIRST_NAME);
			vehicleEquipment.setOwnerLastName(Parking.OWNER_PENDING_LAST_NAME);
			vehicleEquipment.setActive(true);
			vehicleEquipment.setBranch(branch);
			vehicleEquipment.setCreatedByUser(userService.getUser(request));
			vehicleEquipment.setCreatedDateTime(dayService.getTimeStamp());
		}
		vehicleEquipment.setChasisNo(chasisNo);
		vehicleEquipment.setRegistrationNo(text(parkingRequest.getRegistrationNo()));
		vehicleEquipment.setTformNumber(text(parkingRequest.getTformNumber()));
		vehicleEquipment.setVehicleEquipmentColor(parkingRequest.getVehicleEquipmentColor());
		vehicleEquipment.setVehicleEquipmentType(vehicleEquipmentType);
		vehicleEquipment.setDeviceStatus(parkingRequest.isDeviceStatus());
		vehicleEquipment = vehicleEquipmentRepository.save(vehicleEquipment);
		if(vehicleEquipment.getNo() == null || !vehicleEquipment.getNo().startsWith("PKN/")) {
			vehicleEquipment.setNo("PKN/" + vehicleEquipment.getId());
			vehicleEquipment = vehicleEquipmentRepository.save(vehicleEquipment);
		}
		return vehicleEquipment;
	}

	/** Keeps a signature given at one step of the parking; signatures are never changed afterwards */
	private void sign(Parking parking, String stage, String role, User user, String signerName, byte[] image, String changes) {
		ParkingSignature signature = new ParkingSignature();
		signature.setParking(parking);
		signature.setStage(stage);
		signature.setRole(role);
		signature.setUser(user);
		signature.setSignerName(signerName);
		signature.setImage(image);
		signature.setSignedAt(dayService.getTimeStamp());
		signature.setChanges(changes);
		parkingSignatureRepository.save(signature);
	}

	/**
	 * How many tracking devices are attached: none when no device is attached, otherwise at least 1. Required for a new
	 * inspection; older records may have none recorded, so editing them only checks a count that is given.
	 */
	private static Integer deviceCount(ParkingRequestDTO parkingRequest, boolean required) {
		if(!parkingRequest.isDeviceStatus()) return null;
		Integer count = parkingRequest.getDeviceCount();
		if(count == null && !required) return null;
		if(count == null || count < 1) throw new InvalidEntryException("Enter the number of tracking devices attached");
		return count;
	}

	/** The values a modification can change, by the label people see, to tell what a modification changed */
	private static Map<String, String> snapshot(Parking parking) {
		Map<String, String> values = new LinkedHashMap<>();
		values.put("Owner first name", parking.getOwnerFirstName());
		values.put("Owner middle name", parking.getOwnerMiddleName());
		values.put("Owner last name", parking.getOwnerLastName());
		values.put("Owner company", parking.getOwnerCompanyName());
		values.put("Owner ID type", parking.getOwnerIdType());
		values.put("Owner ID no", parking.getOwnerIdNo());
		values.put("Owner phone", parking.getOwnerPhoneNo());
		values.put("Owner email", parking.getOwnerEmail());
		values.put("Owner address", parking.getOwnerAddress());
		values.put("Agent name", parking.getAgentName());
		values.put("Agent address", parking.getAgentAddress());
		values.put("Agent phone", parking.getAgentPhoneNo());
		values.put("Agent email", parking.getAgentEmail());
		values.put("T-form no", parking.getTformNumber());
		values.put("Registration no", parking.getRegistrationNo());
		values.put("Chassis no", parking.getChasisNo());
		values.put("Card no", parking.getCardNo());
		values.put("Vehicle type", parking.getVehicleEquipmentType() == null ? null : parking.getVehicleEquipmentType().getName());
		values.put("Category", parking.getVehicleEquipmentCategory());
		values.put("Colour", parking.getVehicleEquipmentColor());
		values.put("Parking zone", parking.getParkingZone() == null ? null : parking.getParkingZone().getName());
		values.put("Parking slot", parking.getParkingSlotNo() == null ? null : String.valueOf(parking.getParkingSlotNo()));
		values.put("Billing starts", parking.getStartBillingAt() == null ? null : parking.getStartBillingAt().toLocalDate().toString());
		values.put("Left front lamp", yesNo(parking.isLeftFrontLamp()));
		values.put("Right front lamp", yesNo(parking.isRightFrontLamp()));
		values.put("Left rear lamp", yesNo(parking.isLeftRearLamp()));
		values.put("Right rear lamp", yesNo(parking.isRightRearLamp()));
		values.put("Left side mirror", yesNo(parking.isLeftSideMirror()));
		values.put("Right side mirror", yesNo(parking.isRightSideMirror()));
		values.put("Left wiper", yesNo(parking.isLeftWiper()));
		values.put("Right wiper", yesNo(parking.isRightWiper()));
		values.put("Back wiper", yesNo(parking.isBackWiper()));
		values.put("Fuel cap", yesNo(parking.isFuelCap()));
		values.put("Spare tyre", yesNo(parking.isSpareTire()));
		values.put("Battery", yesNo(parking.isBattery()));
		values.put("Starter", yesNo(parking.isStarter()));
		values.put("Aerial", yesNo(parking.isAerial()));
		values.put("Wheel cap", yesNo(parking.isWheelCap()));
		values.put("Round mirror", yesNo(parking.isRoundMirror()));
		values.put("Tyre indicator", yesNo(parking.isTireIndicator()));
		values.put("Has keys", yesNo(parking.isHasKeys()));
		values.put("Interior details", parking.getInteriorDetails());
		values.put("Body condition", parking.getBodyCondition());
		values.put("Device status", parking.isDeviceStatus() ? "Attached" : "Not attached");
		values.put("Number of devices", parking.getDeviceCount() == null ? null : String.valueOf(parking.getDeviceCount()));
		values.put("Driver name", parking.getDriverName());
		values.put("Driver phone", parking.getDriverPhoneNo());
		values.put("Driver ID type", parking.getDriverIdType());
		values.put("Driver ID no", parking.getDriverIdNo());
		values.put("Comments", parking.getComments());
		return values;
	}

	/** The category chosen at inspection: In-transit or Local */
	private static String category(ParkingRequestDTO parkingRequest) {
		String category = required(parkingRequest.getVehicleEquipmentCategory(), "Select the category").toUpperCase();
		if(!CATEGORIES.contains(category)) throw new InvalidEntryException("Select the category: In-transit or Local");
		return category;
	}

	private static String yesNo(boolean value) {
		return value ? "Yes" : "No";
	}

	/** One "Label: old → new" line per value that differs */
	private static String changes(Map<String, String> before, Map<String, String> after) {
		StringBuilder changes = new StringBuilder();
		for(Map.Entry<String, String> entry : after.entrySet()) {
			String old = text(before.get(entry.getKey()));
			String now = text(entry.getValue());
			if(old == null ? now != null : !old.equals(now)) {
				if(changes.length() > 0) changes.append('\n');
				changes.append(entry.getKey()).append(": ").append(old == null ? "(empty)" : old).append(" → ").append(now == null ? "(empty)" : now);
			}
		}
		return changes.toString();
	}

	boolean validateParkingData(ParkingRequestDTO parkingRequest) {
		
		return true;
	}

	/**
	 * Registers an inspected (PENDING) vehicle: the owner, agent, Sub T1, driver, parking zone and slot, signed by the
	 * registrar. The vehicle then waits to be checked in (REGISTERED), holding its slot; card, keys and the day billing
	 * starts are taken at check-in.
	 */
	@Override
	public ParkingResponseDTO registerParking(ParkingRequestDTO parkingRequest, HttpServletRequest request) {

		// Locked, so the same vehicle cannot be registered twice at once
		Parking parking = parkingRepository.findByIdForUpdate(parkingRequest.getId()).orElseThrow(() -> new NotFoundException("Parking not found"));
		User user = userService.getUser(request);
		if(!Objects.equals(parking.getBranch().getId(), user.getBranch().getId()))
			throw new InvalidOperationException("Registering can only be done by a branch user");
		if(!"PENDING".equals(parking.getStatus()))
			throw new InvalidOperationException("Can not register, only an inspected vehicle waiting for registration can be registered");

		// What registration must have; the database itself requires none of the new values
		// The category comes from the inspection; a vehicle inspected before it was asked there must have it edited first
		if(parking.getVehicleEquipmentCategory() == null || !CATEGORIES.contains(parking.getVehicleEquipmentCategory()))
			throw new InvalidOperationException("The category is missing: edit the inspection and choose In-transit or Local");
		String ownerFirstName = required(parkingRequest.getOwnerFirstName(), "Owner first name is required");
		String ownerLastName = required(parkingRequest.getOwnerLastName(), "Owner last name is required");
		if(Parking.isOwnerPending(ownerFirstName, ownerLastName)) throw new InvalidEntryException("Enter the owner's name");
		String driverName = required(parkingRequest.getDriverName(), "Driver name is required");
		byte[] registrarSignature = SignatureImages.decode(parkingRequest.getRegistrarSignature(), "Registrar");

		// The parking zone and slot are chosen at registration and held until check-out
		ParkingZone requestedZone = parkingZoneRepository.findByNameAndBranch(parkingRequest.getParkingZoneName(), parking.getBranch())
				.orElseThrow(() -> new NotFoundException("Parking Zone not found"));
		ParkingZone parkingZone = zoneWithSpace(requestedZone, parkingRequest.getParkingSlotNo(), null);
		parking.setParkingZone(parkingZone);
		parking.setParkingSlotNo(parkingZone.getNoOfSlots() > 0 ? parkingRequest.getParkingSlotNo() : null);

		parking.setOwnerFirstName(ownerFirstName);
		parking.setOwnerMiddleName(text(parkingRequest.getOwnerMiddleName()));
		parking.setOwnerLastName(ownerLastName);
		parking.setOwnerCompanyName(text(parkingRequest.getOwnerCompanyName()));
		parking.setOwnerIdType(text(parkingRequest.getOwnerIdType()));
		parking.setOwnerIdNo(text(parkingRequest.getOwnerIdNo()));
		parking.setOwnerPhoneNo(text(parkingRequest.getOwnerPhoneNo()));
		parking.setOwnerEmail(text(parkingRequest.getOwnerEmail()));
		parking.setOwnerAddress(text(parkingRequest.getOwnerAddress()));
		parking.setAgentName(text(parkingRequest.getAgentName()));
		parking.setAgentPhoneNo(text(parkingRequest.getAgentPhoneNo()));
		parking.setAgentAddress(text(parkingRequest.getAgentAddress()));
		parking.setAgentEmail(text(parkingRequest.getAgentEmail()));
		// The transit document (Sub T1) is entered at registration
		parking.setTformNumber(text(parkingRequest.getTformNumber()));
		// The vehicle keeps its owner too, so "Existing Vehicle" fills it in next time
		VehicleEquipment vehicleEquipment = parking.getVehicleEquipment();
		if(vehicleEquipment != null) {
			vehicleEquipment.setOwnerFirstName(parking.getOwnerFirstName());
			vehicleEquipment.setOwnerMiddleName(parking.getOwnerMiddleName());
			vehicleEquipment.setOwnerLastName(parking.getOwnerLastName());
			vehicleEquipment.setOwnerCompanyName(parking.getOwnerCompanyName());
			vehicleEquipment.setOwnerIdType(parking.getOwnerIdType());
			vehicleEquipment.setOwnerIdNo(parking.getOwnerIdNo());
			vehicleEquipment.setOwnerPhoneNo(parking.getOwnerPhoneNo());
			vehicleEquipment.setOwnerEmail(parking.getOwnerEmail());
			vehicleEquipment.setOwnerAddress(parking.getOwnerAddress());
			vehicleEquipment.setAgentName(parking.getAgentName());
			vehicleEquipment.setAgentPhoneNo(parking.getAgentPhoneNo());
			vehicleEquipment.setAgentAddress(parking.getAgentAddress());
			vehicleEquipment.setAgentEmail(parking.getAgentEmail());
			vehicleEquipment.setTformNumber(parking.getTformNumber());
			vehicleEquipmentRepository.save(vehicleEquipment);
		}
		parking.setDriverName(driverName);
		parking.setDriverPhoneNo(text(parkingRequest.getDriverPhoneNo()));
		parking.setDriverIdType(text(parkingRequest.getDriverIdType()));
		parking.setDriverIdNo(text(parkingRequest.getDriverIdNo()));
		parking.setStatus("REGISTERED");
		parking = parkingRepository.save(parking);

		sign(parking, ParkingSignature.REGISTRATION, ParkingSignature.REGISTRAR, user, nameOf(user), registrarSignature, null);
		parkingSignatureRepository.nameDriver(parking, driverName);
		return parkingResponseDTOMapper(parking);
	}

	@Override
	public ParkingResponseDTO checkIn(ParkingRequestDTO parkingRequest, HttpServletRequest request) {

		// Locked, so the same vehicle cannot be checked in twice at once
		Optional<Parking> parking_ = parkingRepository.findByIdForUpdate(parkingRequest.getId());

		if(parking_.isEmpty())
			throw new NotFoundException("Parking not found");
		User user = userService.getUser(request);
		if(!Objects.equals(parking_.get().getBranch().getId(), user.getBranch().getId()))
			throw new InvalidOperationException("Checking in can only be done by a branch user");
		if(!"REGISTERED".equals(parking_.get().getStatus()))
			throw new InvalidOperationException("Can not check in, only a registered vehicle can be checked in");

		// Check-in: the card and keys, and the day billing starts; the parking zone and slot were chosen at registration
		Parking parking = parking_.get();
		// The check-in signature is optional; when given it must be a proper (small) image
		byte[] checkInSignature = SignatureImages.isPresent(parkingRequest.getCheckInSignature())
				? SignatureImages.decode(parkingRequest.getCheckInSignature(), "Check-in") : null;
		// Normally the zone and slot were chosen at registration. A vehicle registered before that (or without them)
		// gets its zone and slot here instead
		if(parking.getParkingZone() == null) {
			if(parkingRequest.getParkingZoneName() == null || parkingRequest.getParkingZoneName().isBlank())
				throw new InvalidEntryException("Select the parking zone");
			ParkingZone requestedZone = parkingZoneRepository.findByNameAndBranch(parkingRequest.getParkingZoneName(), parking.getBranch())
					.orElseThrow(() -> new NotFoundException("Parking Zone not found"));
			ParkingZone parkingZone = zoneWithSpace(requestedZone, parkingRequest.getParkingSlotNo(), parking);
			parking.setParkingZone(parkingZone);
			parking.setParkingSlotNo(parkingZone.getNoOfSlots() > 0 ? parkingRequest.getParkingSlotNo() : null);
		}

		// The vehicle type and category were set at inspection; the daily price follows the type
		parking.setBillingAmount(parking.getVehicleEquipmentType().getDailyPrice());

		parking.setStatus("CHECKED-IN");
		parking.setCardNo(parkingRequest.getCardNo());
		parking.setHasKeys(parkingRequest.isHasKeys());
		parking.setCheckedInByUser(user);
		parking.setCheckedInDateTime(dayService.getTimeStamp());

		if(parkingRequest.startBillingAt == null) {
			parking.setStartBillingAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this depending on user billing preferences
		}else {

			//String dateString = "2024-10-26 15:30:45" ;
			String dateString = parkingRequest.getStartBillingAt() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
			parking.setStartBillingAt(dateTime);
		}
		parking = parkingRepository.save(parking);
		if(checkInSignature != null) {
			sign(parking, ParkingSignature.CHECK_IN, ParkingSignature.CHECK_IN_OFFICER, user, nameOf(user), checkInSignature, null);
		}
		
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
//		billReceivable.setParkingStatus("UNPAID");
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
//			if(pInvoiceReceivable.getInvoiceReceivable().getParkingStatus()
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
//			invoiceReceivable.setParkingStatus("OPEN");
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
		
		
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking_.get()));
		LocalDateTime lastDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
		LocalDateTime lastBillDate = LocalDateTime.now().toLocalDate().atStartOfDay();
		for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
			if(parkingBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.UNPAID)) {
				throw new InvalidOperationException("Can not check out, bills  not cleared");
			}
			lastBillDate = parkingBillReceivable.getEndedAt();
		}
		if(lastBillDate.isBefore(lastDate)) {
			throw new InvalidOperationException("Could not checkout. Some parking days have not been billed. Please generate and clear bills");
		}
		
		List<ParkingServiceBillReceivable> parkingServiceBillReceivables = parkingServiceBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking_.get()));
		for(ParkingServiceBillReceivable parkingServiceBillReceivable : parkingServiceBillReceivables) {
			if(parkingServiceBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.UNPAID)) {
				throw new InvalidOperationException("Can not check out, bills  not cleared");
			}
		}
		
		Parking parking = parking_.get();
		parking.setStatus("CHECKED-OUT");
		parking.setCheckedOutByUser(userService.getUser(request));
		parking.setCheckedOutDateTime(dayService.getTimeStamp());

		parking = parkingRepository.save(parking);

		// Checked out while away on a trip: the vehicle is not coming back, so its trip is closed as not returned
		if(parking.isOutside()) {
			Optional<ParkingTrip> trip_ = parkingTripRepository.findFirstByParkingAndStatusOrderByIdDesc(parking, ParkingTrip.OUT);
			if(trip_.isPresent()) {
				ParkingTrip trip = trip_.get();
				trip.setStatus(ParkingTrip.NOT_RETURNED);
				trip.setReceivedByUser(parking.getCheckedOutByUser());
				trip.setReceivedAt(parking.getCheckedOutDateTime());
				parkingTripRepository.save(trip);
			}
		}

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
		parkingBillReceivableResponseDTO.setPayStatus(parkingBillReceivable.getBillReceivable().getPayStatus().toString());
				
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
				
				List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking));
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
			
			billReceivable.setPayStatus(PayStatus.UNPAID);
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

	@Override
	public List<MonthlyParkingStatusResponseDTO> getMonthlyStats(int year, HttpServletRequest request) {
		List<Object[]> rawStats = parkingRepository.getMonthlyStats(year);
		return rawStats.stream().map(row -> new MonthlyParkingStatusResponseDTO(((Number) row[0]).intValue(),
				((Number) row[1]).longValue(), ((Number) row[2]).longValue())).collect(Collectors.toList());

	}
	
	@Override
	public void removeVehicleEquipment(Long parkingId, String reason, HttpServletRequest request) {
		Optional<Parking> parking_ = parkingRepository.findById(parkingId);
		if(parking_.isEmpty()) throw new NotFoundException("Parking not found in database");
		
		if(!parking_.get().getStatus().equals("CHECKED-IN")) throw new NotFoundException("Can only archive vehicles/equipments from checked in parkings");
		if(parking_.get().isOutside()) throw new InvalidOperationException("Can not archive. The vehicle is outside the yard; send it in first.");

			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking_.get()));
		
		for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
			if(parkingBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
				throw new InvalidOperationException("Can not archive. The parking has already a paid bill.");
			}
		}
		
		if(reason.isBlank()) {
			throw new InvalidOperationException("Reason is required");
		}
		
		Parking parking = parking_.get();
		
		parking.setStatus("CHECKED-OUT");
		parking.setCheckedOutByUser(userService.getUser(request));
		parking.setCheckedOutDateTime(dayService.getTimeStamp());
		
		parking = parkingRepository.save(parking);
		
		RemovedVehicleEquipment removedVehicleEquipment = new RemovedVehicleEquipment();
		removedVehicleEquipment.setReason(reason);
		removedVehicleEquipment.setParking(parking);
		removedVehicleEquipment.setCreatedByUser(userService.getUser(request));
		removedVehicleEquipment.setCreatedDateTime(dayService.getTimeStamp());
		
		removedVehicleEquipmentRepository.save(removedVehicleEquipment);
		
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
