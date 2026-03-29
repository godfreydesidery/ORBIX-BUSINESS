package com.orbix.api.modules.bond;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.commons.WorkFlowStatus;
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

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BondItemServiceController implements BondItemService {
	private final BondItemRepository bondItemRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final UserService userService;
	private final DayService dayService;

	private final BondZoneRepository bondZoneRepository;

	private final BondItemTypeRepository bondItemTypeRepository;

	private final BillReceivableRepository billReceivableRepository;

	private final BondItemBillReceivableRepository bondItemBillReceivableRepository;
	private final BondItemInvoiceReceivableRepository bondItemInvoiceReceivableRepository;

	private final InvoiceReceivableRepository invoiceReceivableRepository;
	private final InvoiceReceivableDetailRepository invoiceReceivableDetailRepository;

	@Override
	public List<BondItemResponseDTO> getAllBondItems(HttpServletRequest request) {
		List<BondItem> bondItems = bondItemRepository.findAll();
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {
			bondItemResponses.add(bondItemResponseDTOMapper(bondItem));
		}
		return bondItemResponses;
	}

	@Override
	public List<BondItemResponseDTO> getAllPendingOrCheckedInBondItems(HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");

		List<BondItem> bondItems = bondItemRepository.findAllByStatusIn(statuses);
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {
			bondItemResponses.add(bondItemResponseDTOMapper(bondItem));
		}
		return bondItemResponses;
	}

	@Override
	public List<BondItemResponseDTO> getAllPendingOrCheckedInBondItemsByBondZone(Long bondZoneId,
			HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");

		BondZone bondZone = bondZoneRepository.findById(bondZoneId)
				.orElseThrow(() -> new NotFoundException("BondZone not found"));

		List<BondItem> bondItems = bondItemRepository.findAllByBondZoneAndStatusIn(bondZone, statuses);
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {
			bondItemResponses.add(bondItemResponseDTOMapper(bondItem));
		}
		return bondItemResponses;
	}

	@Override
	public List<BondItemResponseDTO> getAllCheckedInBondItemsByBondZone(Long bondZoneId, HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");

		BondZone bondZone = bondZoneRepository.findById(bondZoneId)
				.orElseThrow(() -> new NotFoundException("BondZone not found"));

		List<BondItem> bondItems = bondItemRepository.findAllByBondZoneAndStatusIn(bondZone, statuses);
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {
			bondItemResponses.add(bondItemResponseDTOMapper(bondItem));
		}
		return bondItemResponses;
	}

	@Override
	public List<BondItemResponseDTO> getAllWithDiscounts(HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");

		List<BondItem> bondItems = bondItemRepository.findAllByStatusIn(statuses);
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {

			List<BondItemBillReceivable> sbrs = bondItemBillReceivableRepository.findByBondItem(bondItem);
			for (BondItemBillReceivable sbr : sbrs) {
				if (sbr.getDiscountStatus() != null && sbr.getDiscountStatus().equals("Requested")) {
					bondItemResponses.add(bondItemResponseDTOMapper(bondItem));
					break;
				}
			}
		}
		return bondItemResponses;
	}

	@Override
	public List<BondItemResponseDTO> getAllRecentCheckedOutBondItemsByBondZone(Long bondZoneId,
			HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-OUT");

		BondZone bondZone = bondZoneRepository.findById(bondZoneId)
				.orElseThrow(() -> new NotFoundException("BondZone not found"));

		LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
		List<BondItem> bondItems = bondItemRepository.findAllByBondZoneAndStatusInAndCheckedOutDateTimeAfter(bondZone,
				statuses, yesterday);

		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {
			bondItemResponses.add(bondItemResponseDTOMapper(bondItem));
		}
		return bondItemResponses;
	}

	@Override
	public List<BondItemResponseDTO> getAllCleared(HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");

		List<BondItem> bondItems = bondItemRepository.findAllByStatusIn(statuses);
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {

			boolean cleared = true;

			List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository
					.findAllByBondItem(bondItem);
			if (!bondItemBillReceivables.isEmpty() && cleared == true) {
				for (BondItemBillReceivable bondItemBillReceivable : bondItemBillReceivables) {
					if (!bondItemBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
						cleared = false;
						break;
					}
				}
			}

			if (cleared)
				bondItemResponses.add(bondItemResponseDTOMapper(bondItem));

		}
		return bondItemResponses;
	}

	@Override
	public List<BondItemResponseDTO> getTodayCheckedOut(HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-OUT");

		// Calculate the range
		LocalDateTime startOfToday = LocalDateTime.now().minusDays(1).with(LocalTime.MAX);
		LocalDateTime endOfYesterday = LocalDateTime.now().plusDays(1).with(LocalTime.MIN);

		List<BondItem> bondItems = bondItemRepository.findAllByStatusInAndCheckedOutDateTimeBetween(statuses,
				startOfToday, endOfYesterday);

		// List<BondItem> bondItems =
		// bondItemRepository.findAllByStatusInAndCheckedOutBetween(statuses,
		// LocalDateTime.now().);
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {

			boolean cleared = true;

			List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository
					.findAllByBondItem(bondItem);
			if (!bondItemBillReceivables.isEmpty() && cleared == true) {
				for (BondItemBillReceivable bondItemBillReceivable : bondItemBillReceivables) {
					if (!bondItemBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
						cleared = false;
						break;
					}
				}
			}

			if (cleared)
				bondItemResponses.add(bondItemResponseDTOMapper(bondItem));

		}
		return bondItemResponses;
	}

	@Override
	public List<BondItemResponseDTO> getAllCheckedInBondItems(HttpServletRequest request) {

		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");

		List<BondItem> bondItems = bondItemRepository.findAllByStatusIn(statuses);
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();

		for (BondItem bondItem : bondItems) {
			bondItemResponses.add(bondItemResponseDTOMapper(bondItem));
		}
		return bondItemResponses;
	}

	@Override
	public BondItemResponseDTO get(Long id, HttpServletRequest request) {
		Optional<BondItem> bondItem_ = bondItemRepository.findById(id);
		if (bondItem_.isEmpty()) {
			throw new NotFoundException("BondItem not found");
		}
		return bondItemResponseDTOMapper(bondItem_.get());
	}

	@Override
	public List<BondItemBillReceivableResponseDTO> getBondItemBillReceivables(Long id, HttpServletRequest request) {
		Optional<BondItem> bondItem_ = bondItemRepository.findById(id);
		if (bondItem_.isEmpty()) {
			throw new NotFoundException("BondItem not found");
		}

		List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository
				.findAllByBondItem(bondItem_.get());

		List<BondItemBillReceivableResponseDTO> bondItemBillReceivableResponses = new ArrayList<>();

		for (BondItemBillReceivable bondItemBillReceivable : bondItemBillReceivables) {
			bondItemBillReceivableResponses.add(bondItemBillReceivableDTOMapper(bondItemBillReceivable));
		}
		if (bondItemBillReceivableResponses.isEmpty())
			return null;

		return bondItemBillReceivableResponses;
	}

	@Override
	public BondItemResponseDTO createBondItem(BondItemRequestDTO bondItemRequest, HttpServletRequest request) {

		/** Validate data */
		if (!validateBondItemData(bondItemRequest)) {
			throw new InvalidEntryException("Validation failed");
		}

		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if (company_.isEmpty()) {
			throw new NotFoundException("Company not found");
		}
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if (branch_.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}

		Optional<BondItemType> bondItemType_ = bondItemTypeRepository
				.findByNameAndCompany(bondItemRequest.getBondItemTypeName(), company_.get());
		if (bondItemType_.isEmpty()) {
			throw new NotFoundException("BondItem type not found");
		}
		if (bondItemType_.get().getCompany().getId() != company_.get().getId()) {
			throw new InvalidOperationException("BondItem type does not belong to this company");
		}

		Optional<BondZone> bondZone_ = bondZoneRepository.findByIdAndBranch(bondItemRequest.getBondZoneId(),
				branch_.get());
		if (bondZone_.isEmpty()) {
			throw new NotFoundException("BondZone not found in this branch, please select valid bondZone");
		}

		BondItem bondItem = new BondItem();
		bondItem.setNo(String.valueOf(Math.random()));
		bondItem.setOwnerFirstName(bondItemRequest.getOwnerFirstName());
		bondItem.setOwnerMiddleName(bondItemRequest.getOwnerMiddleName());
		bondItem.setOwnerLastName(bondItemRequest.getOwnerLastName());
		bondItem.setOwnerCompanyName(bondItemRequest.getOwnerCompanyName());
		bondItem.setOwnerIdNo(bondItemRequest.getOwnerIdNo());
		bondItem.setOwnerIdType(bondItemRequest.getOwnerIdType());
		bondItem.setOwnerPhoneNo(bondItemRequest.getOwnerPhoneNo());
		bondItem.setOwnerEmail(bondItemRequest.getOwnerEmail());
		bondItem.setOwnerAddress(bondItemRequest.getOwnerAddress());

		bondItem.setComments(bondItemRequest.getComments());

		///////////////////////////////
		bondItem.setOwnerFirstName(bondItemRequest.getOwnerFirstName());
		bondItem.setOwnerMiddleName(bondItemRequest.getOwnerMiddleName());
		bondItem.setOwnerLastName(bondItemRequest.getOwnerLastName());
		bondItem.setOwnerCompanyName(bondItemRequest.getOwnerCompanyName());
		bondItem.setOwnerIdNo(bondItemRequest.getOwnerIdNo());
		bondItem.setOwnerIdType(bondItemRequest.getOwnerIdType());
		bondItem.setOwnerPhoneNo(bondItemRequest.getOwnerPhoneNo());
		bondItem.setOwnerEmail(bondItemRequest.getOwnerEmail());
		bondItem.setOwnerAddress(bondItemRequest.getOwnerAddress());
		bondItem.setAgentName(bondItemRequest.getAgentName());
		bondItem.setAgentAddress(bondItemRequest.getAgentAddress());
		bondItem.setAgentPhoneNo(bondItemRequest.getAgentPhoneNo());
		bondItem.setAgentEmail(bondItemRequest.getAgentEmail());
		bondItem.setTformNumber(bondItemRequest.getTformNumber());
		bondItem.setRegistrationNo(bondItemRequest.getRegistrationNo());
		bondItem.setChasisNo(bondItemRequest.getChasisNo());
		bondItem.setCardNo(bondItemRequest.getCardNo());
		bondItem.setLeftFrontLamp(bondItemRequest.isLeftFrontLamp());
		bondItem.setRightFrontLamp(bondItemRequest.isRightFrontLamp());
		bondItem.setLeftRearLamp(bondItemRequest.isLeftRearLamp());
		bondItem.setRightRearLamp(bondItemRequest.isRightRearLamp());
		bondItem.setLeftSideMirror(bondItemRequest.isLeftSideMirror());
		bondItem.setRightSideMirror(bondItemRequest.isRightSideMirror());
		bondItem.setLeftWiper(bondItemRequest.isLeftWiper());
		bondItem.setRightWiper(bondItemRequest.isRightWiper());
		bondItem.setBackWiper(bondItemRequest.isBackWiper());
		bondItem.setFuelCap(bondItemRequest.isFuelCap());
		bondItem.setSpareTire(bondItemRequest.isSpareTire());
		bondItem.setBattery(bondItemRequest.isBattery());
		bondItem.setStarter(bondItemRequest.isStarter());
		bondItem.setAerial(bondItemRequest.isAerial());
		bondItem.setWheelCap(bondItemRequest.isWheelCap());
		bondItem.setRoundMirror(bondItemRequest.isRoundMirror());
		bondItem.setTireIndicator(bondItemRequest.isTireIndicator());
		bondItem.setHasKeys(true);
		bondItem.setDeviceStatus(bondItemRequest.isDeviceStatus());

		bondItem.setComments(bondItemRequest.getComments());

		bondItem.setBillingType("MONTHLY");

		// bondItem.setImage(bondItemRequest.getImage());
		bondItem.setStatus("PENDING");
		bondItem.setBondItemType(bondItemType_.get());

		bondItem.setBondItemName(bondItemType_.get().getName()); // Look here later
		bondItem.setBondItemColor(bondItemRequest.getBondItemColor());

		////////////////////////////////

		// Calculate billing rate based on selected vehicle type

//		if(bondItemRequest.getBillingAmount() <= 0) throw new InvalidOperationException("Price can not be zero");	
//		bondItem.setBillingAmount(bondItemRequest.getBillingAmount());
//		

		bondItem.setBillingType("MONTHLY");

		if (bondItemRequest.startBillingAt == null) {
			bondItem.setStartBillingAt(dayService.getTimeStamp()); // You can change this depending on user billing
																	// preferences
		} else {
			// String dateString = "2024-10-26 15:30:45" ;
			String dateString = bondItemRequest.getStartBillingAt() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
			bondItem.setStartBillingAt(dateTime);
			if (dateTime.isAfter(dayService.getTimeStamp())) {
				throw new InvalidOperationException(
						"The selected date cannot be in the future. Please choose today or an earlier date.");
			}
		}

		// bondItem.setImage(bondItemRequest.getImage());
		bondItem.setStatus("PENDING");
		bondItem.setBondItemType(bondItemType_.get());

		bondItem.setBondItemName(bondItemRequest.getBondItemName()); // Look here later
		bondItem.setBondItemDescription(bondItemRequest.getBondItemDescription());

		bondItem.setBranch(branch_.get());

		bondItem.setBondZone(bondZone_.get());

		bondItem.setCreatedByUser(userService.getUser(request));
		bondItem.setCreatedDateTime(dayService.getTimeStamp());

		bondItem = bondItemRepository.save(bondItem);
		/** Create bondItem no */
		bondItem.setNo("BND/" + bondItem.getId().toString());
		bondItem = bondItemRepository.save(bondItem);

		// bondItem.setBondItemStatus(bondItemRequest.getBondItemStatus() != null ?
		// bondItemRequest.getBondItemStatus() : "PENDING");

//		
//		bondItem.setBondZone(bondItemRequest.getBondZone());
//		bondItem.setBondItemType(bondItemRequest.getBondItemType());
//		bondItem.setCreatedByUser(bondItemRequest.getCreatedByUser());
//		bondItem.setCreatedDateTime(bondItemRequest.getCreatedDateTime() != null ? bondItemRequest.getCreatedDateTime() : LocalDateTime.now());
//		bondItem.setCheckedInByUser(bondItemRequest.getCheckedInByUser());
//		bondItem.setCheckedInDateTime(bondItemRequest.getCheckedInDateTime());
//		bondItem.setCheckedOutByUser(bondItemRequest.getCheckedOutByUser());
//		bondItem.setCheckedOutDateTime(bondItemRequest.getCheckedOutDateTime());
//		bondItem.setCanceledByUser(bondItemRequest.getCanceledByUser());
//		bondItem.setCanceledDateTime(bondItemRequest.getCanceledDateTime());
//		bondItem.setBranch(bondItemRequest.getBranch());
//		bondItem.setCompany(bondItemRequest.getCompany());

		return bondItemResponseDTOMapper(bondItem);
	}

	@Override
	public BondItemResponseDTO updateBondItem(BondItemRequestDTO bondItemRequest, HttpServletRequest request) {

		/** Validate data */
		if (!validateBondItemData(bondItemRequest)) {
			throw new InvalidEntryException("Validation failed");
		}

		Optional<BondItem> bondItem_ = bondItemRepository.findById(bondItemRequest.getId());
		if (bondItem_.isEmpty())
			throw new NotFoundException("BondItem not found in database");

		if (!bondItem_.get().getStatus().equals("PENDING"))
			throw new NotFoundException("Can not update, only pending bondItem can be updated");

		if (!validateBondItemData(bondItemRequest))
			throw new InvalidEntryException("Could not validate data");

		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if (company_.isEmpty())
			throw new NotFoundException("Company not found");

		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if (branch_.isEmpty())
			throw new NotFoundException("Branch not found");

		Optional<BondItemType> bondItemType_ = bondItemTypeRepository
				.findByNameAndCompany(bondItemRequest.getBondItemTypeName(), company_.get());
		if (bondItemType_.isEmpty())
			throw new NotFoundException("Vehicle or equipment type not found");

		if (bondItemType_.get().getCompany().getId() != company_.get().getId())
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");

//		Optional<BondZone> bondZone_ = bondZoneRepository.findByNameAndBranch(bondItemRequest.getBondZoneName(), branch_.get());
//		if(bondZone_.isEmpty())throw new NotFoundException("BondItem Zone not found");		

		BondItem bondItem = bondItem_.get();
		bondItem.setOwnerFirstName(bondItemRequest.getOwnerFirstName());
		bondItem.setOwnerMiddleName(bondItemRequest.getOwnerMiddleName());
		bondItem.setOwnerLastName(bondItemRequest.getOwnerLastName());
		bondItem.setOwnerCompanyName(bondItemRequest.getOwnerCompanyName());
		bondItem.setOwnerIdNo(bondItemRequest.getOwnerIdNo());
		bondItem.setOwnerIdType(bondItemRequest.getOwnerIdType());
		bondItem.setOwnerPhoneNo(bondItemRequest.getOwnerPhoneNo());
		bondItem.setOwnerEmail(bondItemRequest.getOwnerEmail());
		bondItem.setOwnerAddress(bondItemRequest.getOwnerAddress());

		bondItem.setBondItemType(bondItemType_.get());

		bondItem.setBondItemName(bondItemRequest.getBondItemName()); // Look here later
		bondItem.setBondItemDescription(bondItemRequest.getBondItemDescription());

		bondItem.setComments(bondItemRequest.getComments());

		bondItem.setWidth(bondItemRequest.getWidth());
		bondItem.setLength(bondItemRequest.getLength());
		bondItem.setHeight(bondItemRequest.getHeight());
		bondItem.setWeight(bondItemRequest.getWeight());

		////////////////////
		bondItem.setOwnerFirstName(bondItemRequest.getOwnerFirstName());
		bondItem.setOwnerMiddleName(bondItemRequest.getOwnerMiddleName());
		bondItem.setOwnerLastName(bondItemRequest.getOwnerLastName());
		bondItem.setOwnerCompanyName(bondItemRequest.getOwnerCompanyName());
		bondItem.setOwnerIdNo(bondItemRequest.getOwnerIdNo());
		bondItem.setOwnerIdType(bondItemRequest.getOwnerIdType());
		bondItem.setOwnerPhoneNo(bondItemRequest.getOwnerPhoneNo());
		bondItem.setOwnerEmail(bondItemRequest.getOwnerEmail());
		bondItem.setOwnerAddress(bondItemRequest.getOwnerAddress());
		bondItem.setAgentName(bondItemRequest.getAgentName());
		bondItem.setAgentAddress(bondItemRequest.getAgentAddress());
		bondItem.setAgentPhoneNo(bondItemRequest.getAgentPhoneNo());
		bondItem.setAgentEmail(bondItemRequest.getAgentEmail());
		bondItem.setTformNumber(bondItemRequest.getTformNumber());
		bondItem.setRegistrationNo(bondItemRequest.getRegistrationNo());
		bondItem.setChasisNo(bondItemRequest.getChasisNo());
		bondItem.setCardNo(bondItemRequest.getCardNo());
		bondItem.setLeftFrontLamp(bondItemRequest.isLeftFrontLamp());
		bondItem.setRightFrontLamp(bondItemRequest.isRightFrontLamp());
		bondItem.setLeftRearLamp(bondItemRequest.isLeftRearLamp());
		bondItem.setRightRearLamp(bondItemRequest.isRightRearLamp());
		bondItem.setLeftSideMirror(bondItemRequest.isLeftSideMirror());
		bondItem.setRightSideMirror(bondItemRequest.isRightSideMirror());
		bondItem.setLeftWiper(bondItemRequest.isLeftWiper());
		bondItem.setRightWiper(bondItemRequest.isRightWiper());
		bondItem.setBackWiper(bondItemRequest.isBackWiper());
		bondItem.setFuelCap(bondItemRequest.isFuelCap());
		bondItem.setSpareTire(bondItemRequest.isSpareTire());
		bondItem.setBattery(bondItemRequest.isBattery());
		bondItem.setStarter(bondItemRequest.isStarter());
		bondItem.setAerial(bondItemRequest.isAerial());
		bondItem.setWheelCap(bondItemRequest.isWheelCap());
		bondItem.setRoundMirror(bondItemRequest.isRoundMirror());
		bondItem.setTireIndicator(bondItemRequest.isTireIndicator());
		bondItem.setHasKeys(true);
		bondItem.setDeviceStatus(bondItemRequest.isDeviceStatus());

		bondItem.setComments(bondItemRequest.getComments());
		////////////////////

//		bondItem.setBondZone(bondZone_.get());

		bondItem.setBillingType(bondItemRequest.getBillingType());
		bondItem.setBillingAmount(bondItemRequest.getBillingAmount());

		if (bondItemRequest.startBillingAt != null && bondItem.getStatus().equals("PENDING")) {
			LocalDateTime dateTime;
			String raw = bondItemRequest.getStartBillingAt();
			if (raw.contains("T")) {
				dateTime = LocalDateTime.parse(raw);
			} else {
				String dateString = raw + " 00:00:00";
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				dateTime = LocalDateTime.parse(dateString, formatter);
			}
			if (dateTime.isAfter(dayService.getTimeStamp())) {
				throw new InvalidOperationException(
						"The selected date cannot be in the future. Please choose today or an earlier date.");
			}
			bondItem.setStartBillingAt(dateTime);
		}

		bondItem = bondItemRepository.save(bondItem);

		return bondItemResponseDTOMapper(bondItem);
	}

	private BondItemResponseDTO bondItemResponseDTOMapper(BondItem bondItem) {
		BondItemResponseDTO bondItemResponse = new BondItemResponseDTO();

		bondItemResponse.setId(String.valueOf(bondItem.getId()));
		bondItemResponse.setNo(bondItem.getNo());
		bondItemResponse.setOwnerFirstName(bondItem.getOwnerFirstName());
		bondItemResponse.setOwnerMiddleName(bondItem.getOwnerMiddleName());
		bondItemResponse.setOwnerLastName(bondItem.getOwnerLastName());
		bondItemResponse.setOwnerCompanyName(bondItem.getOwnerCompanyName());
		bondItemResponse.setOwnerIdNo(bondItem.getOwnerIdNo());
		bondItemResponse.setOwnerIdType(bondItem.getOwnerIdType());
		bondItemResponse.setOwnerPhoneNo(bondItem.getOwnerPhoneNo());
		bondItemResponse.setOwnerEmail(bondItem.getOwnerEmail());
		bondItemResponse.setOwnerAddress(bondItem.getOwnerAddress());

		bondItemResponse.setWidth(String.valueOf(bondItem.getWidth()));
		bondItemResponse.setLength(String.valueOf(bondItem.getLength()));
		bondItemResponse.setHeight(String.valueOf(bondItem.getHeight()));
		bondItemResponse.setWeight(String.valueOf(bondItem.getWeight()));
		bondItemResponse.setInitialQty(String.valueOf(bondItem.getInitialQty()));
		
		if(bondItem.getCurrency() != null) {
			bondItemResponse.setCurrency(bondItem.getCurrency().getCurrencyCode());
		}else {
			bondItemResponse.setCurrency("");
		}

		bondItemResponse
				.setBillingStartAt(Optional.ofNullable(bondItem.getStartBillingAt()).map(Object::toString).orElse(""));

		bondItemResponse.setBondZoneName(Optional.ofNullable(bondItem.getBondZone()).map(BondZone::getName).orElse(""));

		bondItemResponse.setComments(bondItem.getComments());
		// bondItem.setImage(bondItemRequest.getImage());
		bondItemResponse.setStatus(bondItem.getStatus().toString());
		// bondItemResponse.setCompanyId(bondItem.getCompany().getId().toString());
		bondItemResponse.setBranchId(bondItem.getBranch().getId().toString());

		bondItemResponse.setBillingType(bondItem.getBillingType());
		bondItemResponse.setBillingAmount(String.valueOf(bondItem.getBillingAmount()));

		bondItemResponse.setBondItemTypeName(bondItem.getBondItemType().getName());
		bondItemResponse.setBondItemName(bondItem.getBondItemName());
		bondItemResponse.setBondItemDescription(bondItem.getBondItemDescription());
		bondItemResponse.setInitialQty(String.valueOf(bondItem.getInitialQty()));
		bondItemResponse.setCurrentQty(String.valueOf(bondItem.getCurrentQty()));
		bondItemResponse.setAgentName(bondItem.getAgentName());
		bondItemResponse.setAgentAddress(bondItem.getAgentAddress());
		bondItemResponse.setAgentPhoneNo(bondItem.getAgentPhoneNo());
		bondItemResponse.setAgentEmail(bondItem.getAgentEmail());
		bondItemResponse.setTformNumber(bondItem.getTformNumber());
		bondItemResponse.setRegistrationNo(bondItem.getRegistrationNo());
		bondItemResponse.setChasisNo(bondItem.getChasisNo());
		bondItemResponse.setCardNo(bondItem.getCardNo());
		bondItemResponse.setLeftFrontLamp(bondItem.isLeftFrontLamp() ? "1" : "0");
		bondItemResponse.setRightFrontLamp(bondItem.isRightFrontLamp() ? "1" : "0");
		bondItemResponse.setLeftRearLamp(bondItem.isLeftRearLamp() ? "1" : "0");
		bondItemResponse.setRightRearLamp(bondItem.isRightRearLamp() ? "1" : "0");
		bondItemResponse.setLeftSideMirror(bondItem.isLeftSideMirror() ? "1" : "0");
		bondItemResponse.setRightSideMirror(bondItem.isRightSideMirror() ? "1" : "0");
		bondItemResponse.setLeftWiper(bondItem.isLeftWiper() ? "1" : "0");
		bondItemResponse.setRightWiper(bondItem.isRightWiper() ? "1" : "0");
		bondItemResponse.setBackWiper(bondItem.isBackWiper() ? "1" : "0");
		bondItemResponse.setFuelCap(bondItem.isFuelCap() ? "1" : "0");
		bondItemResponse.setSpareTire(bondItem.isSpareTire() ? "1" : "0");
		bondItemResponse.setBattery(bondItem.isBattery() ? "1" : "0");
		bondItemResponse.setStarter(bondItem.isStarter() ? "1" : "0");
		bondItemResponse.setAerial(bondItem.isAerial() ? "1" : "0");
		bondItemResponse.setWheelCap(bondItem.isWheelCap() ? "1" : "0");
		bondItemResponse.setRoundMirror(bondItem.isRoundMirror() ? "1" : "0");
		bondItemResponse.setTireIndicator(bondItem.isTireIndicator() ? "1" : "0");
		bondItemResponse.setHasKeys(bondItem.isHasKeys() ? "1" : "0");
		bondItemResponse.setDeviceStatus(bondItem.isDeviceStatus() ? "1" : "0");
		bondItemResponse.setBondItemCategory(bondItem.getBondItemCategory());
		bondItemResponse.setBondItemName(bondItem.getBondItemName());
		bondItemResponse.setBondItemColor(bondItem.getBondItemColor());
		bondItemResponse
				.setBillingStartAt(Optional.ofNullable(bondItem.getStartBillingAt()).map(Object::toString).orElse(""));

		bondItemResponse.setBondZoneName(bondItem.getBondZone() != null && bondItem.getBondZone().getName() != null
				? bondItem.getBondZone().getName()
				: "");

		if (bondItem.getStatus().equals("CHECKED-OUT")) {
			List<ServiceBillItem> items = new ArrayList<>();

			List<BondItemBillReceivable> pbs = bondItemBillReceivableRepository.findAllByBondItem(bondItem);
			int sn = 1;
			for (BondItemBillReceivable pbr : pbs) {
				ServiceBillItem sbi = new ServiceBillItem();
				sbi.setSn(sn);
				sbi.setItem(pbr.getBillReceivable().getSummary());
				sbi.setQty(pbr.getQty());
				sbi.setAmount(pbr.getBillReceivable().getAmount());
				sbi.setPayStatus(pbr.getBillReceivable().getPayStatus().toString());
				items.add(sbi);
			}

			bondItemResponse.setServiceBillItems(items);
		}
		///////////////////

		return bondItemResponse;
	}

	boolean validateBondItemData(BondItemRequestDTO bondItemRequest) {

		if (bondItemRequest.getOwnerFirstName().isBlank() || bondItemRequest.getOwnerLastName().isBlank()
				|| bondItemRequest.getBondItemName().isBlank()) {
			throw new InvalidOperationException("First name, Last name, BondItem name can not be empty");
		}
		if (bondItemRequest.getBillingAmount() < 0) {
			throw new InvalidEntryException("Invalid billing amount");
		}
		return true;
	}

	@Override
	public BondItemResponseDTO checkIn(BondItemRequestDTO bondItemRequest, HttpServletRequest request) {

		Optional<BondItem> bondItem_ = bondItemRepository.findById(bondItemRequest.getId());

		if (bondItem_.isEmpty())
			throw new NotFoundException("BondItem not found");
		if (bondItem_.get().getBranch().getId() != userService.getUser(request).getBranch().getId())
			throw new InvalidOperationException("Checking in can only be done by a branch user");
		if (!bondItem_.get().getStatus().equals("PENDING"))
			throw new InvalidOperationException("Can not check in, only a pending bondItem can be checked in");

		if (bondItem_.get().getBondZone() == null)
			throw new InvalidOperationException("BondZone not assigned");

		BondItem bondItem = bondItem_.get();

		bondItem.setStatus("CHECKED-IN");
		bondItem.setCheckedInByUser(userService.getUser(request));
		bondItem.setCheckedInDateTime(dayService.getTimeStamp());

		bondItem.setBillingAmount(bondItem.getBondItemType().getDailyPrice());

		bondItem.setStatus("CHECKED-IN");
		bondItem.setCardNo(bondItemRequest.getCardNo());
		bondItem.setHasKeys(bondItemRequest.isHasKeys());
		bondItem.setCheckedInByUser(userService.getUser(request));
		bondItem.setCheckedInDateTime(dayService.getTimeStamp());

		if (bondItemRequest.startBillingAt == null) {
			bondItem.setStartBillingAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this
																								// depending on user
																								// billing preferences
		} else {

			// String dateString = "2024-10-26 15:30:45" ;
			String dateString = bondItemRequest.getStartBillingAt() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
			bondItem.setStartBillingAt(dateTime);
		}

		bondItem = bondItemRepository.save(bondItem);

		return bondItemResponseDTOMapper(bondItem);
	}

	@Override
	public BondItemResponseDTO checkOut(BondItemRequestDTO bondItemRequest, HttpServletRequest request) {

		Optional<BondItem> bondItem_ = bondItemRepository.findById(bondItemRequest.getId());
		if (bondItem_.isEmpty())
			throw new NotFoundException("BondItem not found in database");

		if (!bondItem_.get().getStatus().equals("CHECKED-IN"))
			throw new NotFoundException("Can not check out, only checked in bondItem can be checked out");

//		if(!validateBondItemData(bondItemRequest)) throw new InvalidEntryException("Could not validate data");

		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if (company_.isEmpty())
			throw new NotFoundException("Company not found");

		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if (branch_.isEmpty())
			throw new NotFoundException("Branch not found");

//		Optional<BondItemType> bondItemType_ = bondItemTypeRepository.findByNameAndCompany(bondItemRequest.getBondItemTypeName(), company_.get());
//		if(bondItemType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");

		List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository
				.findAllByBondItem(bondItem_.get());
		LocalDateTime lastDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
		LocalDateTime lastBillDate = LocalDateTime.now().toLocalDate().atStartOfDay();
		double billedQty = 0;
		for (BondItemBillReceivable bondItemBillReceivable : bondItemBillReceivables) {
			billedQty = billedQty + bondItemBillReceivable.getQty();
			if (bondItemBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.UNPAID)) {
				throw new InvalidOperationException("Can not check out, bills  not cleared");
			}
			lastBillDate = bondItemBillReceivable.getEndedAt();
		}
		if (bondItem_.get().getCurrentQty() > 0 && (!bondItem_.get().getBillingType().equals("FLAT-RATE"))) {
			throw new InvalidOperationException(
					"Could not checkout. Some bondItem days have not been billed. Please generate and clear bills");
		}

		if (bondItem_.get().getBillingType().equals("FLAT-RATE")) {
			if (billedQty < bondItem_.get().getInitialQty()) {
				throw new InvalidOperationException(
						"Could not check out. Billed qty is less than total qty, for flat rate");
			}
		}

		BondItem bondItem = bondItem_.get();
		bondItem.setStatus("CHECKED-OUT");
		bondItem.setCheckedOutByUser(userService.getUser(request));
		bondItem.setCheckedOutDateTime(dayService.getTimeStamp());

		bondItem = bondItemRepository.save(bondItem);

		return bondItemResponseDTOMapper(bondItem);
	}

	private BondItemBillReceivableResponseDTO bondItemBillReceivableDTOMapper(
			BondItemBillReceivable bondItemBillReceivable) {

		BondItemBillReceivableResponseDTO bondItemBillReceivableResponseDTO = new BondItemBillReceivableResponseDTO();

		bondItemBillReceivableResponseDTO.setId(bondItemBillReceivable.getId().toString());
		bondItemBillReceivableResponseDTO.setStartedAt(bondItemBillReceivable.getStartedAt().toString());
		bondItemBillReceivableResponseDTO.setEndedAt(bondItemBillReceivable.getEndedAt().toString());
		bondItemBillReceivableResponseDTO.setQty(String.valueOf(bondItemBillReceivable.getQty()));
		bondItemBillReceivableResponseDTO.setPrice(String.valueOf(bondItemBillReceivable.getPrice()));
		bondItemBillReceivableResponseDTO.setBillingType(bondItemBillReceivable.getBillingType());
		bondItemBillReceivableResponseDTO.setDiscount(String.valueOf(bondItemBillReceivable.getDiscount()));
		bondItemBillReceivableResponseDTO.setBondItemId(bondItemBillReceivable.getBondItem().getId().toString());
		bondItemBillReceivableResponseDTO
				.setAmount(String.valueOf(((bondItemBillReceivable.getPrice() * bondItemBillReceivable.getQty())
						- bondItemBillReceivable.getDiscount())));
		bondItemBillReceivableResponseDTO
				.setPayStatus(bondItemBillReceivable.getBillReceivable().getPayStatus().toString());

		return bondItemBillReceivableResponseDTO;

	}

	@Override
	public BondItemBillReceivableResponseDTO createBondItemBillReceivable(Long bondItemId, LocalDateTime startedAt,
			LocalDateTime endedAt, String billingType, double qty, double price, double discount, int autoBilling,
			HttpServletRequest request) {
		// TODO Auto-generated method stub

		/**
		 * Here, get the bondItem, check for bondItem billing start datetime, if the
		 * billing start datetime is more than current datetime if the bill is less than
		 * current date time, invalidate the bill, ignore previous date time if
		 * billstart datetime is b4 current date time, check if there are previous
		 * bills, if yes, check the last datetime if last date time is beyond bill,
		 * invalidate bill
		 * 
		 */

		if (autoBilling != 1 && autoBilling != 0) {
			throw new InvalidOperationException(
					"Invalid billing mode selected. Accepts 1: Autobilling, 0: Manual billing");
		}
		BondItem bondItem = bondItemRepository.findById(bondItemId)
				.orElseThrow(() -> new NotFoundException("BondItem not found"));

		if (!bondItem.getStatus().equals("CHECKED-IN"))
			throw new InvalidOperationException("Only allowed for checked in bondItems");

		if (autoBilling == 0) {
			/**
			 * Here, do manual billing
			 */
			if (bondItem.getStartBillingAt().isBefore(startedAt)) {

				List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository
						.findByBondItem(bondItem);
				// Now check for intersection
				for (BondItemBillReceivable bondItemBillReceivable : bondItemBillReceivables) {
					if (startedAt.isAfter(bondItemBillReceivable.getStartedAt())
							&& startedAt.isBefore(bondItemBillReceivable.getEndedAt().plusDays(1))) {
						throw new InvalidOperationException("Bill starting date intersects with current bills");
					}
				}

			}

			BillReceivable billReceivable = new BillReceivable();
			billReceivable.setNo(String.valueOf(Math.random()));
			billReceivable.setAmount(bondItem.getBillingAmount());
			billReceivable.setPaid(0);
			billReceivable.setDue(bondItem.getBillingAmount());
			// billReceivable.setCompany(bondItem.getCompany());
			billReceivable.setBranch(bondItem.getBranch());
			billReceivable.setCreatedDateTime(dayService.getTimeStamp());

			billReceivable.setPayStatus(PayStatus.UNPAID);
			billReceivable.setSummary("BondItem bill for bondItem#: " + bondItem.getNo());

			billReceivable = billReceivableRepository.save(billReceivable);
			billReceivable.setNo("BR" + billReceivable.getId().toString());
			billReceivable = billReceivableRepository.save(billReceivable);

			InvoiceReceivable invoiceReceivable = null;
			BondItemInvoiceReceivable bondItemInvoiceReceivable = null;

			List<BondItemInvoiceReceivable> bondItemInvoiceReceivables = bondItemInvoiceReceivableRepository
					.findAllByBondItem(bondItem);
			for (BondItemInvoiceReceivable pInvoiceReceivable : bondItemInvoiceReceivables) {
				if (pInvoiceReceivable.getInvoiceReceivable().getStatus().equals("OPEN")) {
					invoiceReceivable = pInvoiceReceivable.getInvoiceReceivable();
					break;
				}
			}
			if (invoiceReceivable == null) {
				invoiceReceivable = new InvoiceReceivable();
				invoiceReceivable.setNo(String.valueOf(Math.random()));
				// invoiceReceivable.setCompany(bondItem.getCompany());
				invoiceReceivable.setBranch(bondItem.getBranch());
				invoiceReceivable.setStatus("OPEN");
				invoiceReceivable.setSummary("Auto invoice, for bondItem# " + bondItem.getNo());

				invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
				invoiceReceivable.setNo("RINV" + invoiceReceivable.getId().toString());

				invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);

				bondItemInvoiceReceivable = new BondItemInvoiceReceivable();
				bondItemInvoiceReceivable.setBondItem(bondItem);
				bondItemInvoiceReceivable.setInvoiceReceivable(invoiceReceivable);

				bondItemInvoiceReceivableRepository.save(bondItemInvoiceReceivable);
			}

			InvoiceReceivableDetail invoiceReceivableDetail = new InvoiceReceivableDetail();
			invoiceReceivableDetail.setInvoiceReceivable(invoiceReceivable);

			invoiceReceivableDetail.setBillReceivable(billReceivable);

			invoiceReceivableDetail.setAmount(bondItem.getBillingAmount());
			invoiceReceivableDetail.setDue(bondItem.getBillingAmount());
			invoiceReceivableDetail.setPaid(0);
			invoiceReceivableDetail.setSummary("Payment for bondItem# " + bondItem.getNo());

			invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);

			BondItemBillReceivable bondItemBillReceivable = new BondItemBillReceivable();
			bondItemBillReceivable.setBillReceivable(billReceivable);
			bondItemBillReceivable.setStartedAt(startedAt);
			bondItemBillReceivable.setEndedAt(endedAt.plusDays(1));
			bondItemBillReceivable.setBillingType("DAILY");
			bondItemBillReceivable.setQty(1);
			bondItemBillReceivable.setPrice(bondItem.getBillingAmount());
			bondItemBillReceivable.setBondItem(bondItem);
			// bondItemBillReceivable.setInvoiceReceivableDetail(invoiceReceivableDetail);

			bondItemBillReceivableRepository.save(bondItemBillReceivable);

		} else if (autoBilling == 1) {
			// Find last billing date

		}

		return null;
	}

	@Override
	public BondItemCustomBillDetail showBondItemCustomBillDetail(Long bondItemId, HttpServletRequest request) {

		BondItem bondItem = bondItemRepository.findById(bondItemId)
				.orElseThrow(() -> new NotFoundException("BondItem not found"));

		LocalDateTime startBillingAt = bondItem.getStartBillingAt();
		double noOfDays = (long) Math
				.floor((double) Duration.between(startBillingAt, LocalDateTime.now()).toHours() / 24);
		if (noOfDays <= 0) {
			noOfDays = 1;
		}

		List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository
				.findByBondItem(bondItem);
		BondItemCustomBillDetail bondItemCustomBillDetail = new BondItemCustomBillDetail();

		bondItemCustomBillDetail.setTotalQty(bondItem.getInitialQty());
		double billedQty = 0;

		for (BondItemBillReceivable sbr : bondItemBillReceivables) {
			billedQty = billedQty + sbr.getQty();
		}

		bondItemCustomBillDetail.setBilledQty(billedQty);

		bondItemCustomBillDetail.setUnbilledQty(bondItem.getInitialQty() - billedQty);

		bondItemCustomBillDetail.setBillingRate(bondItem.getBillingAmount());
		bondItemCustomBillDetail.setNoOfDays(noOfDays);
		bondItemCustomBillDetail.setBillingType(bondItem.getBillingType());

		return bondItemCustomBillDetail;
	}

	@Override
	public List<MonthlyBondItemStatusResponseDTO> getMonthlyStats(int year, HttpServletRequest request) {
		List<Object[]> rawStats = bondItemRepository.getMonthlyStats(year);
		return rawStats.stream().map(row -> new MonthlyBondItemStatusResponseDTO(((Number) row[0]).intValue(),
				((Number) row[1]).longValue(), ((Number) row[2]).longValue())).collect(Collectors.toList());

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

@Data
class BondItemCustomBillDetail {
	double totalQty;
	double billedQty;
	double unbilledQty;
	double billingRate;
	double noOfDays;
	String billingType;
}
