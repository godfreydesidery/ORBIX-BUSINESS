package com.orbix.api.modules.warehouse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingResponseDTO;
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
public class StorageServiceController implements StorageService {
	private final StorageRepository storageRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final UserService userService;
	private final DayService dayService;
	
	
	private final WarehouseRepository warehouseRepository;
	
	private final GoodTypeRepository goodTypeRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	
	private final StorageBillReceivableRepository storageBillReceivableRepository;
	private final StorageInvoiceReceivableRepository storageInvoiceReceivableRepository;
	
	private final InvoiceReceivableRepository invoiceReceivableRepository;
	private final InvoiceReceivableDetailRepository invoiceReceivableDetailRepository;
	
		
	

	@Override
	public List<StorageResponseDTO> getAllStorages(HttpServletRequest request) {
		List<Storage> storages = storageRepository.findAll();
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			storageResponses.add(storageResponseDTOMapper(storage));					
		}		
		return storageResponses;
	}
	
	@Override
	public List<StorageResponseDTO> getAllPendingOrCheckedInStorages(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");
		
		List<Storage> storages = storageRepository.findAllByStatusIn(statuses);
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			storageResponses.add(storageResponseDTOMapper(storage));					
		}		
		return storageResponses;
	}
	
	@Override
	public List<StorageResponseDTO> getAllPendingOrCheckedInStoragesByWarehouse(Long warehouseId, HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("PENDING");
		statuses.add("CHECKED-IN");
		
		Warehouse warehouse = warehouseRepository.findById(warehouseId)
		        .orElseThrow(() -> new NotFoundException("Warehouse not found"));
		
		List<Storage> storages = storageRepository.findAllByWarehouseAndStatusIn(warehouse, statuses);
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			storageResponses.add(storageResponseDTOMapper(storage));					
		}		
		return storageResponses;
	}
	
	@Override
	public List<StorageResponseDTO> getAllCheckedInStoragesByWarehouse(Long warehouseId, HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		Warehouse warehouse = warehouseRepository.findById(warehouseId)
		        .orElseThrow(() -> new NotFoundException("Warehouse not found"));
		
		List<Storage> storages = storageRepository.findAllByWarehouseAndStatusIn(warehouse, statuses);
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			storageResponses.add(storageResponseDTOMapper(storage));					
		}		
		return storageResponses;
	}
	
	@Override
	public List<StorageResponseDTO> getAllWithDiscounts(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		List<Storage> storages = storageRepository.findAllByStatusIn(statuses);
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			
			List<StorageBillReceivable> sbrs = storageBillReceivableRepository.findByStorage(storage);
			for(StorageBillReceivable sbr : sbrs) {
				if(sbr.getDiscountStatus() != null && sbr.getDiscountStatus().equals("Requested")) {
					storageResponses.add(storageResponseDTOMapper(storage));
					break;
				}
			}							
		}		
		return storageResponses;
	}
	
	@Override
	public List<StorageResponseDTO> getAllRecentCheckedOutStoragesByWarehouse(Long warehouseId, HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-OUT");
		
		Warehouse warehouse = warehouseRepository.findById(warehouseId)
		        .orElseThrow(() -> new NotFoundException("Warehouse not found"));
		
		LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
		List<Storage> storages = storageRepository.findAllByWarehouseAndStatusInAndCheckedOutDateTimeAfter(warehouse, statuses, yesterday);
		
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			storageResponses.add(storageResponseDTOMapper(storage));					
		}		
		return storageResponses;
	}
	
	@Override
	public List<StorageResponseDTO> getAllCleared(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		List<Storage> storages = storageRepository.findAllByStatusIn(statuses);
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			
			boolean cleared = true;
			
			List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findAllByStorage(storage);
			if(!storageBillReceivables.isEmpty() && cleared == true) {
				for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
					if(!storageBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
						cleared = false;
						break;
					}
				}
			}
			
			if(cleared) storageResponses.add(storageResponseDTOMapper(storage));	
							
		}		
		return storageResponses;
	}
	
	@Override
	public List<StorageResponseDTO> getTodayCheckedOut(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-OUT");
		
		// Calculate the range
		LocalDateTime startOfToday = LocalDateTime.now().minusDays(1).with(LocalTime.MAX);
		LocalDateTime endOfYesterday = LocalDateTime.now().plusDays(1).with(LocalTime.MIN);

		List<Storage> storages = storageRepository.findAllByStatusInAndCheckedOutDateTimeBetween(statuses, startOfToday, endOfYesterday);
		
		//List<Storage> storages = storageRepository.findAllByStatusInAndCheckedOutBetween(statuses, LocalDateTime.now().);
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			
			boolean cleared = true;
			
			List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findAllByStorage(storage);
			if(!storageBillReceivables.isEmpty() && cleared == true) {
				for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
					if(!storageBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.PAID)) {
						cleared = false;
						break;
					}
				}
			}
			
			if(cleared) storageResponses.add(storageResponseDTOMapper(storage));	
							
		}		
		return storageResponses;
	}
	
	@Override
	public List<StorageResponseDTO> getAllCheckedInStorages(HttpServletRequest request) {
		
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		
		List<Storage> storages = storageRepository.findAllByStatusIn(statuses);
		List<StorageResponseDTO> storageResponses = new ArrayList<>();

		for(Storage storage : storages) {
			storageResponses.add(storageResponseDTOMapper(storage));					
		}		
		return storageResponses;
	}

	@Override
	public StorageResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<Storage> storage_ = storageRepository.findById(id);
	if(storage_.isEmpty()) {
		throw new NotFoundException("Storage not found");
	}		
	return storageResponseDTOMapper(storage_.get());	
	}
	
	@Override
	public List<StorageBillReceivableResponseDTO> getStorageBillReceivables(Long id, HttpServletRequest request) {		
		Optional<Storage> storage_ = storageRepository.findById(id);
		if(storage_.isEmpty()) {
			throw new NotFoundException("Storage not found");
		}
		
		List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findAllByStorage(storage_.get());
		
		List<StorageBillReceivableResponseDTO> storageBillReceivableResponses = new ArrayList<>();
		
		for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
			storageBillReceivableResponses.add(storageBillReceivableDTOMapper(storageBillReceivable));
		}
		if (storageBillReceivableResponses.isEmpty()) return null;	
		
		return storageBillReceivableResponses;
	}

	@Override
	public StorageResponseDTO createStorage(StorageRequestDTO storageRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateStorageData(storageRequest)) {
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
		
		Optional<GoodType> goodType_ = goodTypeRepository.findByNameAndCompany(storageRequest.getGoodTypeName(), company_.get());
		if(goodType_.isEmpty()) {
			throw new NotFoundException("Good type not found");
		}
		if(goodType_.get().getCompany().getId() != company_.get().getId()) {
			throw new InvalidOperationException("Good type does not belong to this company");
		}
		
		Optional<Warehouse> warehouse_ = warehouseRepository.findByIdAndBranch(storageRequest.getWarehouseId(), branch_.get());
		if(warehouse_.isEmpty()) {
			throw new NotFoundException("Warehouse not found in this branch, please select valid warehouse");
		}
		
		Storage storage = new Storage();
		storage.setNo(String.valueOf(Math.random()));
		storage.setOwnerFirstName(storageRequest.getOwnerFirstName());
		storage.setOwnerMiddleName(storageRequest.getOwnerMiddleName());
		storage.setOwnerLastName(storageRequest.getOwnerLastName());
		storage.setOwnerCompanyName(storageRequest.getOwnerCompanyName());
		storage.setOwnerIdNo(storageRequest.getOwnerIdNo());
		storage.setOwnerIdType(storageRequest.getOwnerIdType());
		storage.setOwnerPhoneNo(storageRequest.getOwnerPhoneNo());
		storage.setOwnerEmail(storageRequest.getOwnerEmail());
		storage.setOwnerAddress(storageRequest.getOwnerAddress());

		storage.setComments(storageRequest.getComments());
		
		storage.setWidth(storageRequest.getWidth());
		storage.setLength(storageRequest.getLength());
		storage.setHeight(storageRequest.getHeight());
		storage.setWeight(storageRequest.getWeight());
		
		if(storageRequest.getBillingAmount() <= 0) throw new InvalidOperationException("Price can not be zero");	
		storage.setBillingAmount(storageRequest.getBillingAmount());
		
		if(storageRequest.getInitialQty() <= 0) throw new InvalidOperationException("Qty can not be zero");
				
//		storage.setBillingType("DAILY");		
		if(storageRequest.getBillingType().equals("DAILY")) {
			storage.setInitialQty(storageRequest.getInitialQty());
			storage.setCurrentQty(storageRequest.getInitialQty());
			storage.setBillingType("DAILY");
		}else if(storageRequest.getBillingType().equals("FLAT-RATE")) {
			storage.setInitialQty(storageRequest.getInitialQty());
			storage.setCurrentQty(storageRequest.getInitialQty());
			storage.setBillingType("FLAT-RATE");
		}else {
			throw new InvalidOperationException("Invalid Billing Type");
		}
		
		
		if(storageRequest.startBillingAt == null) {
			storage.setStartBillingAt(dayService.getTimeStamp()); // You can change this depending on user billing preferences
		}else {			
			//String dateString = "2024-10-26 15:30:45" ;
			String dateString = storageRequest.getStartBillingAt() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
			storage.setStartBillingAt(dateTime);			
			if(dateTime.isAfter(dayService.getTimeStamp())) {
				throw new InvalidOperationException("The selected date cannot be in the future. Please choose today or an earlier date.");
			}				
		}	
		
		//storage.setImage(storageRequest.getImage());
		storage.setStatus("PENDING");
		storage.setGoodType(goodType_.get());
		
		storage.setGoodName(storageRequest.getGoodName()); // Look here later
		storage.setGoodDescription(storageRequest.getGoodDescription());
		
		storage.setBranch(branch_.get());
		
		storage.setWarehouse(warehouse_.get());

		storage.setCreatedByUser(userService.getUser(request));
		storage.setCreatedDateTime(dayService.getTimeStamp());
		
		storage = storageRepository.save(storage);
		/**Create  storage no*/
		storage.setNo("STG/"+ storage.getId().toString());
		storage = storageRepository.save(storage);
		
		
		//storage.setStorageStatus(storageRequest.getStorageStatus() != null ? storageRequest.getStorageStatus() : "PENDING");

//		
//		storage.setWarehouse(storageRequest.getWarehouse());
//		storage.setGoodType(storageRequest.getGoodType());
//		storage.setCreatedByUser(storageRequest.getCreatedByUser());
//		storage.setCreatedDateTime(storageRequest.getCreatedDateTime() != null ? storageRequest.getCreatedDateTime() : LocalDateTime.now());
//		storage.setCheckedInByUser(storageRequest.getCheckedInByUser());
//		storage.setCheckedInDateTime(storageRequest.getCheckedInDateTime());
//		storage.setCheckedOutByUser(storageRequest.getCheckedOutByUser());
//		storage.setCheckedOutDateTime(storageRequest.getCheckedOutDateTime());
//		storage.setCanceledByUser(storageRequest.getCanceledByUser());
//		storage.setCanceledDateTime(storageRequest.getCanceledDateTime());
//		storage.setBranch(storageRequest.getBranch());
//		storage.setCompany(storageRequest.getCompany());

	
		
		return storageResponseDTOMapper(storage);		
	}

	@Override
	public StorageResponseDTO updateStorage(StorageRequestDTO storageRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateStorageData(storageRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		
		Optional<Storage> storage_ = storageRepository.findById(storageRequest.getId());
		if(storage_.isEmpty()) throw new NotFoundException("Storage not found in database");
			
		if(!storage_.get().getStatus().equals("PENDING")) throw new NotFoundException("Can not update, only pending storage can be updated");
			
		if(!validateStorageData(storageRequest)) throw new InvalidEntryException("Could not validate data");
			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
		
		
		Optional<GoodType> goodType_ = goodTypeRepository.findByNameAndCompany(storageRequest.getGoodTypeName(), company_.get());
		if(goodType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");
			
		
		if(goodType_.get().getCompany().getId() != company_.get().getId()) 
			throw new InvalidOperationException("Vehicle or equipment type does not belong to this company");
		
//		Optional<Warehouse> warehouse_ = warehouseRepository.findByNameAndBranch(storageRequest.getWarehouseName(), branch_.get());
//		if(warehouse_.isEmpty())throw new NotFoundException("Storage Zone not found");		
		
		Storage storage = storage_.get();
		storage.setOwnerFirstName(storageRequest.getOwnerFirstName());
		storage.setOwnerMiddleName(storageRequest.getOwnerMiddleName());
		storage.setOwnerLastName(storageRequest.getOwnerLastName());
		storage.setOwnerCompanyName(storageRequest.getOwnerCompanyName());
		storage.setOwnerIdNo(storageRequest.getOwnerIdNo());
		storage.setOwnerIdType(storageRequest.getOwnerIdType());
		storage.setOwnerPhoneNo(storageRequest.getOwnerPhoneNo());
		storage.setOwnerEmail(storageRequest.getOwnerEmail());
		storage.setOwnerAddress(storageRequest.getOwnerAddress());
		
		storage.setGoodType(goodType_.get());
		
		storage.setGoodName(storageRequest.getGoodName()); // Look here later
		storage.setGoodDescription(storageRequest.getGoodDescription());
		
		storage.setComments(storageRequest.getComments());
		
		storage.setWidth(storageRequest.getWidth());
		storage.setLength(storageRequest.getLength());
		storage.setHeight(storageRequest.getHeight());
		storage.setWeight(storageRequest.getWeight());
		
//		storage.setWarehouse(warehouse_.get());
		
		storage.setBillingType(storageRequest.getBillingType());
		storage.setBillingAmount(storageRequest.getBillingAmount());
		
		if(storageRequest.startBillingAt != null && storage.getStatus().equals("PENDING")) {		
			LocalDateTime dateTime;
			String raw = storageRequest.getStartBillingAt();
			if (raw.contains("T")) {
			    dateTime = LocalDateTime.parse(raw);
			} else {
			    String dateString = raw + " 00:00:00";
			    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			    dateTime = LocalDateTime.parse(dateString, formatter);
			}			
			if(dateTime.isAfter(dayService.getTimeStamp())) {
				throw new InvalidOperationException("The selected date cannot be in the future. Please choose today or an earlier date.");
			}
			storage.setStartBillingAt(dateTime);
		}
				
		storage = storageRepository.save(storage);
		
		return storageResponseDTOMapper(storage);			
	}

	private StorageResponseDTO storageResponseDTOMapper(Storage storage) {
		StorageResponseDTO storageResponse = new StorageResponseDTO();
		
		storageResponse.setId(String.valueOf(storage.getId()));
		storageResponse.setNo(storage.getNo());
		storageResponse.setOwnerFirstName(storage.getOwnerFirstName());
		storageResponse.setOwnerMiddleName(storage.getOwnerMiddleName());
		storageResponse.setOwnerLastName(storage.getOwnerLastName());
		storageResponse.setOwnerCompanyName(storage.getOwnerCompanyName());
		storageResponse.setOwnerIdNo(storage.getOwnerIdNo());
		storageResponse.setOwnerIdType(storage.getOwnerIdType());
		storageResponse.setOwnerPhoneNo(storage.getOwnerPhoneNo());
		storageResponse.setOwnerEmail(storage.getOwnerEmail());
		storageResponse.setOwnerAddress(storage.getOwnerAddress());
		
		storageResponse.setWidth(String.valueOf(storage.getWidth()));
		storageResponse.setLength(String.valueOf(storage.getLength()));
		storageResponse.setHeight(String.valueOf(storage.getHeight()));
		storageResponse.setWeight(String.valueOf(storage.getWeight()));
		storageResponse.setInitialQty(String.valueOf(storage.getInitialQty()));
		
		storageResponse.setBillingStartAt(
			    Optional.ofNullable(storage.getStartBillingAt())
			            .map(Object::toString)
			            .orElse("")
			);
		
				
		storageResponse.setWarehouseName(
			    Optional.ofNullable(storage.getWarehouse())
			            .map(Warehouse::getName)
			            .orElse("")
			);
		
		storageResponse.setComments(storage.getComments());
		//storage.setImage(storageRequest.getImage());
		storageResponse.setStatus(storage.getStatus().toString());
		//storageResponse.setCompanyId(storage.getCompany().getId().toString());
		storageResponse.setBranchId(storage.getBranch().getId().toString());
		
		storageResponse.setBillingType(storage.getBillingType());
		storageResponse.setBillingAmount(String.valueOf(storage.getBillingAmount()));
		
		storageResponse.setGoodTypeName(storage.getGoodType().getName());
		storageResponse.setGoodName(storage.getGoodName());
		storageResponse.setGoodDescription(storage.getGoodDescription());
		storageResponse.setInitialQty(String.valueOf(storage.getInitialQty()));
		storageResponse.setCurrentQty(String.valueOf(storage.getCurrentQty()));
		
		storageResponse.setWarehouseName(
				storage.getWarehouse() != null && storage.getWarehouse().getName() != null
		        ? storage.getWarehouse().getName() 
		        : "");
		
		if(storage.getStatus().equals("CHECKED-OUT")) {
			List<ServiceBillItem> items = new ArrayList<>();
		
			List<StorageBillReceivable> pbs = storageBillReceivableRepository.findAllByStorage(storage);
			int sn = 1;
			for(StorageBillReceivable pbr : pbs) {
				ServiceBillItem sbi = new ServiceBillItem();
				sbi.setSn(sn);
				sbi.setItem(pbr.getBillReceivable().getSummary());
				sbi.setQty(pbr.getQty());
				sbi.setAmount(pbr.getBillReceivable().getAmount());
				sbi.setPayStatus(pbr.getBillReceivable().getPayStatus().toString());
				items.add(sbi);
			}
			
			storageResponse.setServiceBillItems(items);
		}
			
		return storageResponse;
	}
	
	boolean validateStorageData(StorageRequestDTO storageRequest) {
		
		if(storageRequest.getOwnerFirstName().isBlank() || storageRequest.getOwnerLastName().isBlank() || storageRequest.getGoodName().isBlank()) {
			throw new InvalidOperationException("First name, Last name, Good name can not be empty");
		}
		if(storageRequest.getBillingAmount() < 0) {
			throw new InvalidEntryException("Invalid billing amount");
		}
		return true;
	}

	@Override
	public StorageResponseDTO checkIn(StorageRequestDTO storageRequest, HttpServletRequest request) {
		
		Optional<Storage> storage_ = storageRepository.findById(storageRequest.getId());
		
		if(storage_.isEmpty()) 
			throw new NotFoundException("Storage not found");			
		if(storage_.get().getBranch().getId() != userService.getUser(request).getBranch().getId()) 
			throw new InvalidOperationException("Checking in can only be done by a branch user");
		if(!storage_.get().getStatus().equals("PENDING")) 
			throw new InvalidOperationException("Can not check in, only a pending storage can be checked in");
		
		if(storage_.get().getWarehouse() == null) throw new InvalidOperationException("Warehouse not assigned");
		
		Storage storage = storage_.get();
		
		storage.setStatus("CHECKED-IN");
		storage.setCheckedInByUser(userService.getUser(request));
		storage.setCheckedInDateTime(dayService.getTimeStamp());
		
//		if(storageRequest.startBillingAt == null) {
//			storage.setStartBillingAt(dayService.getTimeStamp()); // You can change this depending on user billing preferences
//		}else {			
//			//String dateString = "2024-10-26 15:30:45" ;
//			String dateString = storageRequest.getStartBillingAt() + " 00:00:00";
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
//			storage.setStartBillingAt(dateTime);
//		}		
		storage = storageRepository.save(storage);
		
//		//generate bill, for day 1 depending on billing type
//		
//		BillReceivable billReceivable = new BillReceivable();
//		billReceivable.setNo(String.valueOf(Math.random()));
//		billReceivable.setAmount(storage.getBillingAmount());
//		billReceivable.setPaid(0);
//		billReceivable.setDue(storage.getBillingAmount());
//		//billReceivable.setCompany(storage.getCompany());
//		billReceivable.setBranch(storage.getBranch());
//		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
//		
//		billReceivable.setStorageStatus("UNPAID");
//		billReceivable.setSummary("Storage bill for storage#: " + storage.getNo());
//		
//		billReceivable = billReceivableRepository.save(billReceivable);
//		billReceivable.setNo("BR" + billReceivable.getId().toString());
//		billReceivable = billReceivableRepository.save(billReceivable);
//		
//		
//		
//		InvoiceReceivable invoiceReceivable = null;
//		StorageInvoiceReceivable storageInvoiceReceivable = null;
//		
//		List<StorageInvoiceReceivable> storageInvoiceReceivables = storageInvoiceReceivableRepository.findAllByStorage(storage);
//		for(StorageInvoiceReceivable pInvoiceReceivable : storageInvoiceReceivables) {
//			if(pInvoiceReceivable.getInvoiceReceivable().getStorageStatus()
//					.equals("OPEN")) {
//				invoiceReceivable = pInvoiceReceivable.getInvoiceReceivable();
//				break;
//			}
//		}
//		if(invoiceReceivable == null) {
//			invoiceReceivable = new InvoiceReceivable();
//			invoiceReceivable.setNo(String.valueOf(Math.random()));
//			//invoiceReceivable.setCompany(storage.getCompany());
//			invoiceReceivable.setBranch(storage.getBranch());
//			invoiceReceivable.setStorageStatus("OPEN");
//			invoiceReceivable.setSummary("Auto invoice, for storage# " + storage.getNo());
//			
//			invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
//			invoiceReceivable.setNo("RINV" + invoiceReceivable.getId().toString());
//			
//			invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
//			
//			storageInvoiceReceivable = new StorageInvoiceReceivable();
//			storageInvoiceReceivable.setStorage(storage);
//			storageInvoiceReceivable.setInvoiceReceivable(invoiceReceivable);
//			
//			storageInvoiceReceivableRepository.save(storageInvoiceReceivable);
//		}
//		
//		InvoiceReceivableDetail invoiceReceivableDetail = new InvoiceReceivableDetail();
//		invoiceReceivableDetail.setInvoiceReceivable(invoiceReceivable);
//		
//		invoiceReceivableDetail.setBillReceivable(billReceivable);
//		
//		invoiceReceivableDetail.setAmount(storage.getBillingAmount());
//		invoiceReceivableDetail.setDue(storage.getBillingAmount());
//		invoiceReceivableDetail.setPaid(0);
//		invoiceReceivableDetail.setSummary("Payment for storage# " + storage.getNo());
//		
//		invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);
//		
//		StorageBillReceivable storageBillReceivable = new StorageBillReceivable();
//		storageBillReceivable.setBillReceivable(billReceivable);
//		storageBillReceivable.setStartedAt(LocalDateTime.now());
//		storageBillReceivable.setEndedAt(LocalDateTime.now().plusDays(1));
//		storageBillReceivable.setBillingType("DAILY");
//		storageBillReceivable.setQty(1);
//		storageBillReceivable.setPrice(storage.getBillingAmount());
//		storageBillReceivable.setStorage(storage);
//		//storageBillReceivable.setInvoiceReceivableDetail(invoiceReceivableDetail);
//		
//		storageBillReceivableRepository.save(storageBillReceivable);
	
		return storageResponseDTOMapper(storage);
	}

	@Override
	public StorageResponseDTO checkOut(StorageRequestDTO storageRequest, HttpServletRequest request) {
		
		Optional<Storage> storage_ = storageRepository.findById(storageRequest.getId());
		if(storage_.isEmpty()) throw new NotFoundException("Storage not found in database");
			
		if(!storage_.get().getStatus().equals("CHECKED-IN")) throw new NotFoundException("Can not check out, only checked in storage can be checked out");
			
//		if(!validateStorageData(storageRequest)) throw new InvalidEntryException("Could not validate data");
			
		Optional<Company> company_ = companyRepository.findById(userService.getUserCompany(request).getId());
		if(company_.isEmpty()) throw new NotFoundException("Company not found");
			
		Optional<Branch> branch_ = branchRepository.findById(userService.getUserBranch(request).getId());
		if(branch_.isEmpty()) throw new NotFoundException("Branch not found");
			
//		Optional<GoodType> goodType_ = goodTypeRepository.findByNameAndCompany(storageRequest.getGoodTypeName(), company_.get());
//		if(goodType_.isEmpty()) throw new NotFoundException("Vehicle or equipment type not found");
		
		
		List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findAllByStorage(storage_.get());
		LocalDateTime lastDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
		LocalDateTime lastBillDate = LocalDateTime.now().toLocalDate().atStartOfDay();
		double billedQty = 0;
		for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
			billedQty = billedQty + storageBillReceivable.getQty();
			if(storageBillReceivable.getBillReceivable().getPayStatus().equals(PayStatus.UNPAID)) {
				throw new InvalidOperationException("Can not check out, bills  not cleared");
			}
			lastBillDate = storageBillReceivable.getEndedAt();
		}
		if(storage_.get().getCurrentQty() > 0 && (!storage_.get().getBillingType().equals("FLAT-RATE"))) {
			throw new InvalidOperationException("Could not checkout. Some storage days have not been billed. Please generate and clear bills");
		}
		
		if(storage_.get().getBillingType().equals("FLAT-RATE")) {
			if(billedQty < storage_.get().getInitialQty()) {
				throw new InvalidOperationException("Could not check out. Billed qty is less than total qty, for flat rate");
			}
		}
		
		Storage storage = storage_.get();
		storage.setStatus("CHECKED-OUT");
		storage.setCheckedOutByUser(userService.getUser(request));
		storage.setCheckedOutDateTime(dayService.getTimeStamp());
		
		storage = storageRepository.save(storage);
		
		return storageResponseDTOMapper(storage);
	}
	
	private StorageBillReceivableResponseDTO storageBillReceivableDTOMapper(StorageBillReceivable storageBillReceivable) {
		
		StorageBillReceivableResponseDTO storageBillReceivableResponseDTO = new StorageBillReceivableResponseDTO();
		
		storageBillReceivableResponseDTO.setId(storageBillReceivable.getId().toString());
		storageBillReceivableResponseDTO.setStartedAt(storageBillReceivable.getStartedAt().toString());
		storageBillReceivableResponseDTO.setEndedAt(storageBillReceivable.getEndedAt().toString());
		storageBillReceivableResponseDTO.setQty(String.valueOf(storageBillReceivable.getQty()));
		storageBillReceivableResponseDTO.setPrice(String.valueOf(storageBillReceivable.getPrice()));
		storageBillReceivableResponseDTO.setBillingType(storageBillReceivable.getBillingType());
		storageBillReceivableResponseDTO.setDiscount(String.valueOf(storageBillReceivable.getDiscount()));
		storageBillReceivableResponseDTO.setStorageId(storageBillReceivable.getStorage().getId().toString());
		storageBillReceivableResponseDTO.setAmount(String.valueOf(((storageBillReceivable.getPrice() * storageBillReceivable.getQty()) - storageBillReceivable.getDiscount())));
		storageBillReceivableResponseDTO.setPayStatus(storageBillReceivable.getBillReceivable().getPayStatus().toString());
				
		return storageBillReceivableResponseDTO;
		
	}

	@Override
	public StorageBillReceivableResponseDTO createStorageBillReceivable(Long storageId, LocalDateTime startedAt,
			LocalDateTime endedAt, String billingType, double qty, double price, double discount, int autoBilling, HttpServletRequest request) {
		// TODO Auto-generated method stub
		
		/**
		 * Here, get the storage, check for storage billing start datetime, if the billing start datetime is more
		 * than current datetime if the bill is less than current date time, invalidate the bill, ignore previous date time
		 * if billstart datetime is b4 current date time, check if there are previous bills, if yes, check the last datetime
		 * if last date time is beyond bill, invalidate bill
		 * 
		 */
		
		if (autoBilling != 1 && autoBilling != 0) {
		    throw new InvalidOperationException("Invalid billing mode selected. Accepts 1: Autobilling, 0: Manual billing");
		}
		Storage storage = storageRepository.findById(storageId)
		        .orElseThrow(() -> new NotFoundException("Storage not found"));
		
		if(!storage.getStatus().equals("CHECKED-IN")) throw new InvalidOperationException("Only allowed for checked in storages");
		
		if(autoBilling == 0) {
			/**
			 * Here, do manual billing
			 */
			if(storage.getStartBillingAt().isBefore(startedAt)) {
				
				List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findByStorage(storage);
				// Now check for intersection
				for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
					if(startedAt.isAfter(storageBillReceivable.getStartedAt()) && startedAt.isBefore(storageBillReceivable.getEndedAt().plusDays(1))) {
						throw new InvalidOperationException("Bill starting date intersects with current bills");
					}
				}
				
			}
			
			
			
			
			
			
			BillReceivable billReceivable = new BillReceivable();
			billReceivable.setNo(String.valueOf(Math.random()));
			billReceivable.setAmount(storage.getBillingAmount());
			billReceivable.setPaid(0);
			billReceivable.setDue(storage.getBillingAmount());
			//billReceivable.setCompany(storage.getCompany());
			billReceivable.setBranch(storage.getBranch());
			billReceivable.setCreatedDateTime(dayService.getTimeStamp());
			
			billReceivable.setPayStatus(PayStatus.UNPAID);
			billReceivable.setSummary("Storage bill for storage#: " + storage.getNo());
			
			billReceivable = billReceivableRepository.save(billReceivable);
			billReceivable.setNo("BR" + billReceivable.getId().toString());
			billReceivable = billReceivableRepository.save(billReceivable);
			
			
			
			InvoiceReceivable invoiceReceivable = null;
			StorageInvoiceReceivable storageInvoiceReceivable = null;
			
			List<StorageInvoiceReceivable> storageInvoiceReceivables = storageInvoiceReceivableRepository.findAllByStorage(storage);
			for(StorageInvoiceReceivable pInvoiceReceivable : storageInvoiceReceivables) {
				if(pInvoiceReceivable.getInvoiceReceivable().getStatus()
						.equals("OPEN")) {
					invoiceReceivable = pInvoiceReceivable.getInvoiceReceivable();
					break;
				}
			}
			if(invoiceReceivable == null) {
				invoiceReceivable = new InvoiceReceivable();
				invoiceReceivable.setNo(String.valueOf(Math.random()));
				//invoiceReceivable.setCompany(storage.getCompany());
				invoiceReceivable.setBranch(storage.getBranch());
				invoiceReceivable.setStatus("OPEN");
				invoiceReceivable.setSummary("Auto invoice, for storage# " + storage.getNo());
				
				invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
				invoiceReceivable.setNo("RINV" + invoiceReceivable.getId().toString());
				
				invoiceReceivable = invoiceReceivableRepository.save(invoiceReceivable);
				
				storageInvoiceReceivable = new StorageInvoiceReceivable();
				storageInvoiceReceivable.setStorage(storage);
				storageInvoiceReceivable.setInvoiceReceivable(invoiceReceivable);
				
				storageInvoiceReceivableRepository.save(storageInvoiceReceivable);
			}
			
			InvoiceReceivableDetail invoiceReceivableDetail = new InvoiceReceivableDetail();
			invoiceReceivableDetail.setInvoiceReceivable(invoiceReceivable);
			
			invoiceReceivableDetail.setBillReceivable(billReceivable);
			
			invoiceReceivableDetail.setAmount(storage.getBillingAmount());
			invoiceReceivableDetail.setDue(storage.getBillingAmount());
			invoiceReceivableDetail.setPaid(0);
			invoiceReceivableDetail.setSummary("Payment for storage# " + storage.getNo());
			
			invoiceReceivableDetail = invoiceReceivableDetailRepository.save(invoiceReceivableDetail);
			
			StorageBillReceivable storageBillReceivable = new StorageBillReceivable();
			storageBillReceivable.setBillReceivable(billReceivable);
			storageBillReceivable.setStartedAt(startedAt);
			storageBillReceivable.setEndedAt(endedAt.plusDays(1));
			storageBillReceivable.setBillingType("DAILY");
			storageBillReceivable.setQty(1);
			storageBillReceivable.setPrice(storage.getBillingAmount());
			storageBillReceivable.setStorage(storage);
			//storageBillReceivable.setInvoiceReceivableDetail(invoiceReceivableDetail);
			
			storageBillReceivableRepository.save(storageBillReceivable);
			
		}else if(autoBilling == 1) {
			// Find last billing date
			
		}
		
		return null;
	}

	@Override
	public StorageCustomBillDetail showStorageCustomBillDetail(Long storageId, HttpServletRequest request) {
		
		Storage storage = storageRepository.findById(storageId)
		        .orElseThrow(() -> new NotFoundException("Storage not found"));
		
		LocalDateTime startBillingAt = storage.getStartBillingAt();
		double noOfDays = (long) Math.floor((double) Duration.between(startBillingAt, LocalDateTime.now()).toHours() / 24);
		if(noOfDays <=0 ) {
			noOfDays = 1;
		}
		
		List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findByStorage(storage);
		StorageCustomBillDetail storageCustomBillDetail = new StorageCustomBillDetail();
		
		storageCustomBillDetail.setTotalQty(storage.getInitialQty());
		double billedQty = 0;
		
		for(StorageBillReceivable sbr : storageBillReceivables) {
			billedQty = billedQty + sbr.getQty();
		}
		
		storageCustomBillDetail.setBilledQty(billedQty);
		
		storageCustomBillDetail.setUnbilledQty(storage.getInitialQty() - billedQty);
		
		storageCustomBillDetail.setBillingRate(storage.getBillingAmount());
		storageCustomBillDetail.setNoOfDays(noOfDays);
		storageCustomBillDetail.setBillingType(storage.getBillingType());
		
		return storageCustomBillDetail;
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
class StorageCustomBillDetail{
	double totalQty;
	double billedQty;
	double unbilledQty;
	double billingRate;
	double noOfDays;
	String billingType;
}
