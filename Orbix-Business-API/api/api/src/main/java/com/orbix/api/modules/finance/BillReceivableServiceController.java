package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayCode;
import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingInvoiceReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingServiceBillReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingServiceBillReceivableRepository;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.bond.BondItem;
import com.orbix.api.modules.bond.BondItemBillReceivable;
import com.orbix.api.modules.bond.BondItemBillReceivableRepository;
import com.orbix.api.modules.bond.BondItemRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.salesandmarketing.RestaurantSaleDetailBillReceivable;
import com.orbix.api.modules.salesandmarketing.RestaurantSaleDetailBillReceivableRepository;
import com.orbix.api.modules.servicebay.Machine;
import com.orbix.api.modules.servicebay.MachineRepository;
import com.orbix.api.modules.servicebay.MachineService;
import com.orbix.api.modules.servicebay.MachineServiceBillReceivable;
import com.orbix.api.modules.servicebay.MachineServiceBillReceivableRepository;
import com.orbix.api.modules.servicebay.MachineServiceRepository;
import com.orbix.api.modules.vehicleandequipmentmaintenance.Maintenance;
import com.orbix.api.modules.vehicleandequipmentmaintenance.MaintenanceJobCardIssueBillReceivable;
import com.orbix.api.modules.vehicleandequipmentmaintenance.MaintenanceJobCardIssueBillReceivableRepository;
import com.orbix.api.modules.vehicleandequipmentmaintenance.MaintenanceRepository;
import com.orbix.api.modules.warehouse.Storage;
import com.orbix.api.modules.warehouse.StorageBillReceivable;
import com.orbix.api.modules.warehouse.StorageBillReceivableRepository;
import com.orbix.api.modules.warehouse.StorageRepository;
import com.orbix.api.modules.weighbridge.Weigh;
import com.orbix.api.modules.weighbridge.WeighBillReceivable;
import com.orbix.api.modules.weighbridge.WeighBillReceivableRepository;
import com.orbix.api.modules.weighbridge.WeighRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BillReceivableServiceController implements BillReceivableService {
	
	private final BillReceivableRepository billReceivableRepository;
	private final ParkingRepository parkingRepository;
	private final StorageRepository storageRepository;
	private final BondItemRepository bondItemRepository;
	private final WeighRepository weighRepository;
	private final MachineRepository machineRepository;
	private final MaintenanceRepository maintenanceRepository;
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	private final ParkingServiceBillReceivableRepository parkingServiceBillReceivableRepository;
	private final StorageBillReceivableRepository storageBillReceivableRepository;
	private final BondItemBillReceivableRepository bondItemBillReceivableRepository;
	private final WeighBillReceivableRepository weighBillReceivableRepository;
	private final MaintenanceJobCardIssueBillReceivableRepository maintenanceJobCardIssueBillReceivableRepository;
	private final InvoiceReceivableDetailRepository invoiceReceivableDetailRepository;
	private final MachineServiceRepository machineServiceRepository;
	private final MachineServiceBillReceivableRepository machineServiceBillReceivableRepository;
	private final RestaurantSaleDetailBillReceivableRepository restaurantSaleDetailBillReceivableRepository;
	
	private final BillReceivableCollectionRepository billReceivableCollectionRepository;
	
	private final CollectionRepository collectionRepository;
	private final UserService userService;
	
	private final DayService dayService;
	
	// Groups the records linked to bills by bill id
	private <T> Map<Long, List<T>> groupByBillReceivable(List<T> records, Function<T, BillReceivable> getBillReceivable) {
		Map<Long, List<T>> recordsByBillReceivable = new HashMap<>();
		for(T record : records) {
			recordsByBillReceivable.computeIfAbsent(getBillReceivable.apply(record).getId(), k -> new ArrayList<>()).add(record);
		}
		return recordsByBillReceivable;
	}
	
	// Same result as the repository findByBillReceivable, taken from the preloaded records
	private <T> Optional<T> getPreloadedByBillReceivable(Map<Long, List<T>> recordsByBillReceivable, BillReceivable billReceivable, Function<BillReceivable, Optional<T>> repositoryFindByBillReceivable) {
		List<T> records = recordsByBillReceivable.getOrDefault(billReceivable.getId(), Collections.emptyList());
		if(records.size() > 1) {
			// More than one linked record is not expected; let the repository handle it exactly as before
			return repositoryFindByBillReceivable.apply(billReceivable);
		}
		return records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
	}
	
	@Override
	public List<BillReceivableResponseDTO> confirmBillPayment(
			List<BillReceivableRequestDTO> billReceivableRequests, 
			PayCode payCode,
			String payRefNo,
			double totalAmount,
			HttpServletRequest request) {
		
		
		LocalDateTime dateTime = dayService.getTimeStamp();
		
		Collection collection = new Collection();
		collection.setPayCode(payCode);
		collection.setCollectionDateTime(dateTime);
		collection.setCollectedByUser(userService.getUser(request));
		
		collection = collectionRepository.save(collection);
		
		double total = 0;

		// Load the bills and the service records linked to them once for the whole payment, instead of eight look-ups per bill
		List<Long> billReceivableIds = new ArrayList<>();
		for(BillReceivableRequestDTO bl : billReceivableRequests) {
			billReceivableIds.add(bl.getId());
		}
		List<BillReceivable> billReceivables = billReceivableRepository.findAllById(billReceivableIds);
		Map<Long, List<ParkingBillReceivable>> parkingBillReceivables = new HashMap<>();
		Map<Long, List<ParkingServiceBillReceivable>> parkingServiceBillReceivables = new HashMap<>();
		Map<Long, List<StorageBillReceivable>> storageBillReceivables = new HashMap<>();
		Map<Long, List<BondItemBillReceivable>> bondItemBillReceivables = new HashMap<>();
		Map<Long, List<MaintenanceJobCardIssueBillReceivable>> maintenanceJobCardIssueBillReceivables = new HashMap<>();
		Map<Long, List<WeighBillReceivable>> weighBillReceivables = new HashMap<>();
		Map<Long, List<RestaurantSaleDetailBillReceivable>> restaurantSaleDetailBillReceivables = new HashMap<>();
		Map<Long, List<MachineServiceBillReceivable>> machineServiceBillReceivables = new HashMap<>();
		if(!billReceivables.isEmpty()) {
			parkingBillReceivables = groupByBillReceivable(parkingBillReceivableRepository.findAllByBillReceivableIn(billReceivables), ParkingBillReceivable::getBillReceivable);
			parkingServiceBillReceivables = groupByBillReceivable(parkingServiceBillReceivableRepository.findAllByBillReceivableIn(billReceivables), ParkingServiceBillReceivable::getBillReceivable);
			storageBillReceivables = groupByBillReceivable(storageBillReceivableRepository.findAllByBillReceivableIn(billReceivables), StorageBillReceivable::getBillReceivable);
			bondItemBillReceivables = groupByBillReceivable(bondItemBillReceivableRepository.findAllByBillReceivableIn(billReceivables), BondItemBillReceivable::getBillReceivable);
			maintenanceJobCardIssueBillReceivables = groupByBillReceivable(maintenanceJobCardIssueBillReceivableRepository.findAllByBillReceivableIn(billReceivables), MaintenanceJobCardIssueBillReceivable::getBillReceivable);
			weighBillReceivables = groupByBillReceivable(weighBillReceivableRepository.findAllByBillReceivableIn(billReceivables), WeighBillReceivable::getBillReceivable);
			restaurantSaleDetailBillReceivables = groupByBillReceivable(restaurantSaleDetailBillReceivableRepository.findAllByBillReceivableIn(billReceivables), RestaurantSaleDetailBillReceivable::getBillReceivable);
			machineServiceBillReceivables = groupByBillReceivable(machineServiceBillReceivableRepository.findAllByBillReceivableIn(billReceivables), MachineServiceBillReceivable::getBillReceivable);
		}

		for(BillReceivableRequestDTO bl : billReceivableRequests) {
			BillReceivable billReceivable = billReceivableRepository.findById(bl.getId()).get();
			total = total + billReceivable.getDue();
			if(bl.getAmount() != billReceivable.getDue()) throw new InvalidOperationException("Can not accept partial bill payment");
			double blAmt = billReceivable.getDue();
			billReceivable.setPaid(blAmt);
			billReceivable.setDue(0);
			billReceivable.setPayStatus(PayStatus.PAID);
			billReceivable.setPaidDateTime(dateTime);
			
			double qty = 1;
			
			billReceivable = billReceivableRepository.save(billReceivable);
			
			BillReceivableCollection billReceivableCollection = new BillReceivableCollection();
			billReceivableCollection.setAmount(blAmt);
			billReceivableCollection.setPartial(false);
			billReceivableCollection.setReason("General Payment");
			billReceivableCollection.setBillReceivable(billReceivable);
			billReceivableCollection.setCollection(collection);
			
			

			Optional<ParkingBillReceivable> parkingBillReceivable = getPreloadedByBillReceivable(parkingBillReceivables, billReceivable, parkingBillReceivableRepository::findByBillReceivable);
			if(parkingBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Vehicle and Equipment/Parking");
				qty = parkingBillReceivable.get().getQty();
			} 
			Optional<ParkingServiceBillReceivable> parkingServiceBillReceivable = getPreloadedByBillReceivable(parkingServiceBillReceivables, billReceivable, parkingServiceBillReceivableRepository::findByBillReceivable);
			if(parkingServiceBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Vehicle and Equipment/Service");
				qty = parkingServiceBillReceivable.get().getQty();
			} 
			
			Optional<StorageBillReceivable> storageBillReceivable = getPreloadedByBillReceivable(storageBillReceivables, billReceivable, storageBillReceivableRepository::findByBillReceivable);
			if(storageBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Goods Storage");
				qty = storageBillReceivable.get().getQty();
			}
			
			Optional<BondItemBillReceivable> bondItemBillReceivable = getPreloadedByBillReceivable(bondItemBillReceivables, billReceivable, bondItemBillReceivableRepository::findByBillReceivable);
			if(bondItemBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Bond");
				qty = bondItemBillReceivable.get().getQty();
			}
			
			Optional<MaintenanceJobCardIssueBillReceivable> maintenanceJobCardIssueBillReceivable = getPreloadedByBillReceivable(maintenanceJobCardIssueBillReceivables, billReceivable, maintenanceJobCardIssueBillReceivableRepository::findByBillReceivable);
			if(maintenanceJobCardIssueBillReceivable.isPresent()) {
				billReceivableCollection.setReason("V/Eq Maintenance");
				qty = 1; //maintenanceJobCardIssueBillReceivable.get().getQty();
			}
			
			Optional<WeighBillReceivable> weighBillReceivable = getPreloadedByBillReceivable(weighBillReceivables, billReceivable, weighBillReceivableRepository::findByBillReceivable);
			if(weighBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Weigh Bridge");
				qty = 1;
			}
			
			Optional<RestaurantSaleDetailBillReceivable> restaurantSaleDetailBillReceivable = getPreloadedByBillReceivable(restaurantSaleDetailBillReceivables, billReceivable, restaurantSaleDetailBillReceivableRepository::findByBillReceivable);
			if(restaurantSaleDetailBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Restaurant Sales");
				qty = restaurantSaleDetailBillReceivable.get().getQty();
			}
			
			Optional<MachineServiceBillReceivable> machineServiceBillReceivable = getPreloadedByBillReceivable(machineServiceBillReceivables, billReceivable, machineServiceBillReceivableRepository::findByBillReceivable);
			if(machineServiceBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Machine/Vehicle Service");
				qty = machineServiceBillReceivable.get().getQty();
			}
			
			billReceivableCollection = billReceivableCollectionRepository.save(billReceivableCollection);
			billReceivable = billReceivableRepository.save(billReceivable);
			billReceivable.setQty(qty); 
			billReceivableRepository.save(billReceivable);
			
		}
		
		if(total != totalAmount) {
			throw new InvalidOperationException("Amount mismatch");
		}
	
	return null;
	}

	@Override
	public List<BillReceivableResponseDTO> getAllByParking(Long parkingId, HttpServletRequest request) {
		Parking parking = parkingRepository.findById(parkingId)
			    .orElseThrow(() -> new NotFoundException("Parking with ID " + parkingId + " not found"));
		
		// Bills are loaded together with their bill receivable instead of one extra query per bill
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking));
		List<ParkingServiceBillReceivable> parkingServiceBillReceivables = parkingServiceBillReceivableRepository.findAllByParkingIn(Collections.singletonList(parking));
		List<BillReceivableResponseDTO> billReceivableResponses = new ArrayList<>();
		for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(parkingBillReceivable.getBillReceivable()));
		}	
		for(ParkingServiceBillReceivable parkingServiceBillReceivable : parkingServiceBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(parkingServiceBillReceivable.getBillReceivable()));
		}
		return billReceivableResponses;
	}
	
	@Override
	public List<BillReceivableResponseDTO> getAllByStorage(Long storageId, HttpServletRequest request) {
		Storage storage = storageRepository.findById(storageId)
			    .orElseThrow(() -> new NotFoundException("Storage with ID " + storageId + " not found"));
		
		// Bills are loaded together with their bill receivable instead of one extra query per bill
		List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findAllByStorageIn(Collections.singletonList(storage));
		List<BillReceivableResponseDTO> billReceivableResponses = new ArrayList<>();
		for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(storageBillReceivable.getBillReceivable()));
		}		
		return billReceivableResponses;
	}
	
	@Override
	public List<BillReceivableResponseDTO> getAllByBondItem(Long bondItemId, HttpServletRequest request) {
		BondItem bondItem = bondItemRepository.findById(bondItemId)
			    .orElseThrow(() -> new NotFoundException("Bond Item with ID " + bondItemId + " not found"));
		
		// Bills are loaded together with their bill receivable instead of one extra query per bill
		List<BondItemBillReceivable> bondItemBillReceivables = bondItemBillReceivableRepository.findAllByBondItemIn(Collections.singletonList(bondItem));
		List<BillReceivableResponseDTO> billReceivableResponses = new ArrayList<>();
		for(BondItemBillReceivable bondItemBillReceivable : bondItemBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(bondItemBillReceivable.getBillReceivable()));
		}		
		return billReceivableResponses;
	}
	
	@Override
	public List<BillReceivableResponseDTO> getAllByWeigh(Long weighId, HttpServletRequest request) {
		Weigh weigh = weighRepository.findById(weighId)
			    .orElseThrow(() -> new NotFoundException("Weigh with ID " + weighId + " not found"));
		
		// Bills are loaded together with their bill receivable instead of one extra query per bill
		List<WeighBillReceivable> weighBillReceivables = weighBillReceivableRepository.findAllByWeighIn(Collections.singletonList(weigh));
		List<BillReceivableResponseDTO> billReceivableResponses = new ArrayList<>();
		for(WeighBillReceivable weighBillReceivable : weighBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(weighBillReceivable.getBillReceivable()));
		}		
		return billReceivableResponses;
	}
	
	@Override
	public List<BillReceivableResponseDTO> getAllByMaintenance(Long maintenanceId, HttpServletRequest request) {
		Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
			    .orElseThrow(() -> new NotFoundException("Maintenance with ID " + maintenanceId + " not found"));
		
		List<MaintenanceJobCardIssueBillReceivable> maintenanceJobCardIssueBillReceivables = maintenanceJobCardIssueBillReceivableRepository.findAllByMaintenanceJobCardIssue_MaintenanceJobCard_Maintenance(maintenance);
		List<BillReceivableResponseDTO> billReceivableResponses = new ArrayList<>();
		for(MaintenanceJobCardIssueBillReceivable maintenanceJobCardIssueBillReceivable : maintenanceJobCardIssueBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(maintenanceJobCardIssueBillReceivable.getBillReceivable()));
		}		
		return billReceivableResponses;
	}
	
	@Override
	public List<BillReceivableResponseDTO> getAllByMachine(Long machineId, HttpServletRequest request) {
		Machine machine = machineRepository.findById(machineId)
			    .orElseThrow(() -> new NotFoundException("Machine with ID " + machineId + " not found"));
		
		List<MachineService> machineServices = machineServiceRepository.findAllByMachine(machine);
		
		List<MachineServiceBillReceivable> machineServiceBillReceivables = machineServiceBillReceivableRepository.findAllByMachineServiceIn(machineServices);
		List<BillReceivableResponseDTO> billReceivableResponses = new ArrayList<>();
		for(MachineServiceBillReceivable machineServiceBillReceivable : machineServiceBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(machineServiceBillReceivable.getBillReceivable()));
		}		
		return billReceivableResponses;
	}
	
	private BillReceivableResponseDTO billReceivableResponseDTOMapper(BillReceivable billReceivable) {
		BillReceivableResponseDTO billReceivableResponse = new BillReceivableResponseDTO();
		billReceivableResponse.setId(billReceivable.getId().toString());
		billReceivableResponse.setAmount(String.valueOf(billReceivable.getAmount()));
		billReceivableResponse.setBranchId(billReceivable.getBranch().getId().toString());
		billReceivableResponse.setCompanyId("");
		billReceivableResponse.setBranchName(billReceivable.getBranch().getName());
		billReceivableResponse.setCreatedDateTime(billReceivable.getCreatedDateTime().toString());
		billReceivableResponse.setNo(billReceivable.getNo());
		billReceivableResponse.setQty(String.valueOf(billReceivable.getQty()));
		billReceivableResponse.setPayStatus(billReceivable.getPayStatus().toString());

		billReceivableResponse.setSummary(billReceivable.getSummary());
		billReceivableResponse.setDue(String.valueOf(billReceivable.getDue()));
		billReceivableResponse.setPaid(String.valueOf(billReceivable.getPaid()));
		
		return billReceivableResponse;
		
		
		
	}

}
