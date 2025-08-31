package com.orbix.api.modules.servicebay;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivable;
import com.orbix.api.api.vehicleandequipmentparking.ParkingInvoiceReceivable;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.WorkshopRepository;
import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.finance.InvoiceReceivable;
import com.orbix.api.modules.finance.InvoiceReceivableDetail;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.inventoryandprocurement.ServiceRepository;
import com.orbix.api.modules.weighbridge.WeighBillReceivable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MachineServiceServiceController implements MachineServiceService {
	private final MachineServiceRepository machineServiceRepository;
	private final MachineRepository machineRepository;
	private final ServiceRepository serviceRepository;
	private final UserRepository userRepository;
	private final DayService dayService;
	private final BillReceivableRepository billReceivableRepository;
	private final MachineServiceBillReceivableRepository machineServiceBillReceivableRepository;
	private final UserService userService;

	@Override
	@Transactional
	public MachineServiceResponseDTO createMachineService(MachineServiceRequestDTO dto,  HttpServletRequest request) {
		MachineService machineService = new MachineService();

		machineService.setQty(dto.getQty());
		machineService.setPrice(dto.getPrice());

		machineService.setMachine(machineRepository.findById((long) dto.getMachineId())
				.orElseThrow(() -> new RuntimeException("Machine not found")));

		machineService.setService(serviceRepository.findById((long) dto.getServiceId())
				.orElseThrow(() -> new RuntimeException("Service not found")));

		// Example: fetch authenticated user here instead of hardcoded
		User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
		machineService.setCreatedByUser(user);

		MachineService saved = machineServiceRepository.save(machineService);
		return mapToResponse(saved);
	}

	@Override
	@Transactional
	public MachineServiceResponseDTO updateMachineService(MachineServiceRequestDTO dto,  HttpServletRequest request) {
		MachineService machineService = machineServiceRepository.findById(dto.getId())
				.orElseThrow(() -> new RuntimeException("MachineService not found"));

		machineService.setQty(dto.getQty());
		machineService.setPrice(dto.getPrice());

		if (dto.getMachineId() > 0) {
			machineService.setMachine(machineRepository.findById((long) dto.getMachineId())
					.orElseThrow(() -> new RuntimeException("Machine not found")));
		}

		if (dto.getServiceId() > 0) {
			machineService.setService(serviceRepository.findById((long) dto.getServiceId())
					.orElseThrow(() -> new RuntimeException("Service not found")));
		}

		MachineService updated = machineServiceRepository.save(machineService);
		return mapToResponse(updated);
	}

	@Override
	public boolean confirm(Long id,  HttpServletRequest request) {
		Machine machine = machineRepository.findById(id).orElseThrow();

		List<MachineService> machineServices = machineServiceRepository.findAllByMachineAndStatus(machine, "PENDING");

		for (MachineService machineService : machineServices) {
			if (machineService.getStatus().equals("PENDING")) {
				
				machineService.setStatus("APPROVED");
				machineService = machineServiceRepository.save(machineService);

				double amount = machineService.getPrice() * machineService.getQty();

				BillReceivable billReceivable = new BillReceivable();
				billReceivable.setNo(String.valueOf(Math.random()));
				billReceivable.setAmount(amount);
				billReceivable.setPaid(0);
				billReceivable.setDue(amount);
				billReceivable.setBranch(machineService.getMachine().getBranch());
				billReceivable.setCreatedDateTime(dayService.getTimeStamp());

				billReceivable.setPayStatus(PayStatus.UNPAID);
				billReceivable.setSummary("Machine/Vehicle Service");

				billReceivable = billReceivableRepository.save(billReceivable);
				billReceivable.setNo("BR" + billReceivable.getId().toString());
				billReceivable = billReceivableRepository.save(billReceivable);
				
				MachineServiceBillReceivable machineServiceBillReceivable = new MachineServiceBillReceivable();
				 
				machineServiceBillReceivable.setPrice(machineService.getPrice()); 
				machineServiceBillReceivable.setQty( machineService.getQty());
				machineServiceBillReceivable.setDescription("Machine/Vehicle Service");
				machineServiceBillReceivable.setBillReceivable(billReceivable);
				machineServiceBillReceivable.setMachineService(machineService);

				machineServiceBillReceivable.setCreatedByUser(userService.getUser(request));
				
				machineServiceBillReceivable = machineServiceBillReceivableRepository.save(machineServiceBillReceivable);

			}
		}
		
		return true;
	}

	private MachineServiceResponseDTO mapToResponse(MachineService entity) {
		MachineServiceResponseDTO dto = new MachineServiceResponseDTO();
		dto.setId(entity.getId().toString());
		dto.setQty(String.valueOf(entity.getQty()));
		dto.setPrice(String.valueOf(entity.getPrice()));
		dto.setStatus(entity.getStatus());
		dto.setCreatedBy(entity.getCreatedByUser().getUsername());
		dto.setCreatedAt(entity.getCreatedDateTime().toString());
		dto.setMachineId(entity.getMachine().getId().toString());
		dto.setMachineName(entity.getMachine().getName());
		dto.setServiceId(entity.getService().getId().toString());
		dto.setServiceName(entity.getService().getName());
		dto.setStatus(entity.getStatus());
		return dto;
	}

	/**
	 * BillReceivable billReceivable = new BillReceivable();
	 * billReceivable.setNo(String.valueOf(Math.random()));
	 * billReceivable.setAmount(b.getAmount()); billReceivable.setPaid(0);
	 * billReceivable.setDue(b.getAmount());
	 * billReceivable.setBranch(weigh.getBranch());
	 * billReceivable.setCreatedDateTime(dayService.getTimeStamp());
	 * 
	 * billReceivable.setPayStatus(PayStatus.UNPAID);
	 * billReceivable.setSummary(b.getDescription());
	 * 
	 * billReceivable = billReceivableRepository.save(billReceivable);
	 * billReceivable.setNo("BR" + billReceivable.getId().toString());
	 * billReceivable = billReceivableRepository.save(billReceivable);
	 * 
	 * WeighBillReceivable weighBillReceivable = new WeighBillReceivable();
	 * 
	 * weighBillReceivable.setPrice(b.getAmount()); weighBillReceivable.setQty(1);
	 * weighBillReceivable.setDescription(b.getDescription());
	 * weighBillReceivable.setDiscount(0);
	 * weighBillReceivable.setBillReceivable(billReceivable);
	 * weighBillReceivable.setWeigh(weigh);
	 * weighBillReceivable.setWeightOne(b.getWeightOne());
	 * weighBillReceivable.setWeightTwo(b.getWeightTwo());
	 * weighBillReceivable.setWeightThree(b.getWeightThree());
	 * weighBillReceivable.setWeightFour(b.getWeightFour());
	 * weighBillReceivable.setWeighStatus(b.getWeighStatus());
	 * weighBillReceivable.setCreatedByUser(userService.getUser(request));
	 * 
	 * weighBillReceivable =
	 * weighBillReceivableRepository.save(weighBillReceivable);
	 * 
	 * weigh.setWeighStatus(weighBillReceivable.getWeighStatus());
	 * 
	 * weighRepository.saveAndFlush(weigh);
	 * 
	 */
}
