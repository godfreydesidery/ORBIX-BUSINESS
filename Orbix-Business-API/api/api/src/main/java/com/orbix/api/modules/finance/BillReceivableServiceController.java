package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
	private final WeighRepository weighRepository;
	private final MachineRepository machineRepository;
	private final MaintenanceRepository maintenanceRepository;
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	private final ParkingServiceBillReceivableRepository parkingServiceBillReceivableRepository;
	private final StorageBillReceivableRepository storageBillReceivableRepository;
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
			
			

			Optional<ParkingBillReceivable> parkingBillReceivable = parkingBillReceivableRepository.findByBillReceivable(billReceivable);
			if(parkingBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Vehicle and Equipment/Parking");
				qty = parkingBillReceivable.get().getQty();
			} 
			Optional<ParkingServiceBillReceivable> parkingServiceBillReceivable = parkingServiceBillReceivableRepository.findByBillReceivable(billReceivable);
			if(parkingServiceBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Vehicle and Equipment/Service");
				qty = parkingServiceBillReceivable.get().getQty();
			} 
			
			Optional<StorageBillReceivable> storageBillReceivable = storageBillReceivableRepository.findByBillReceivable(billReceivable);
			if(storageBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Goods Storage");
				qty = storageBillReceivable.get().getQty();
			}
			
			Optional<MaintenanceJobCardIssueBillReceivable> maintenanceJobCardIssueBillReceivable = maintenanceJobCardIssueBillReceivableRepository.findByBillReceivable(billReceivable);
			if(maintenanceJobCardIssueBillReceivable.isPresent()) {
				billReceivableCollection.setReason("V/Eq Maintenance");
				qty = 1; //maintenanceJobCardIssueBillReceivable.get().getQty();
			}
			
			Optional<WeighBillReceivable> weighBillReceivable = weighBillReceivableRepository.findByBillReceivable(billReceivable);
			if(weighBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Weigh Bridge");
				qty = 1;
			}
			
			Optional<RestaurantSaleDetailBillReceivable> restaurantSaleDetailBillReceivable = restaurantSaleDetailBillReceivableRepository.findByBillReceivable(billReceivable);
			if(restaurantSaleDetailBillReceivable.isPresent()) {
				billReceivableCollection.setReason("Restaurant Sales");
				qty = restaurantSaleDetailBillReceivable.get().getQty();
			}
			
			Optional<MachineServiceBillReceivable> machineServiceBillReceivable = machineServiceBillReceivableRepository.findByBillReceivable(billReceivable);
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
		
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParking(parking);
		List<ParkingServiceBillReceivable> parkingServiceBillReceivables = parkingServiceBillReceivableRepository.findAllByParking(parking);
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
		
		List<StorageBillReceivable> storageBillReceivables = storageBillReceivableRepository.findAllByStorage(storage);
		List<BillReceivableResponseDTO> billReceivableResponses = new ArrayList<>();
		for(StorageBillReceivable storageBillReceivable : storageBillReceivables) {
			billReceivableResponses.add(billReceivableResponseDTOMapper(storageBillReceivable.getBillReceivable()));
		}		
		return billReceivableResponses;
	}
	
	@Override
	public List<BillReceivableResponseDTO> getAllByWeigh(Long weighId, HttpServletRequest request) {
		Weigh weigh = weighRepository.findById(weighId)
			    .orElseThrow(() -> new NotFoundException("Weigh with ID " + weighId + " not found"));
		
		List<WeighBillReceivable> weighBillReceivables = weighBillReceivableRepository.findAllByWeigh(weigh);
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
