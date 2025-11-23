package com.orbix.api.modules.servicebay;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.identityandaccess.UserService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MachineServiceBillReceivableServiceController implements MachineServiceBillReceivableService {
	private final UserService userService;

	private final MachineServiceBillReceivableRepository machineServiceBillReceivableRepository;
	private final MachineServiceRepository machineServiceRepository;
	private final MachineRepository machineRepository;
	private final BillReceivableRepository billReceivableRepository;
//	private final MachineServiceGoodReleaseRepository machineServiceGoodReleaseRepository;
	private final DayService dayService;

	@Override
	public List<MachineServiceBillReceivableResponseDTO> getAllByMachine(Long machineId, HttpServletRequest request) {
		Machine machine = machineRepository.findById(machineId)
				.orElseThrow(() -> new NotFoundException("Machine with ID " + machineId + " not found."));
		
		List<MachineService> machineServices = machineServiceRepository.findAllByMachine(machine);
		
		List<MachineServiceBillReceivable> machineServiceBillReceivables = machineServiceBillReceivableRepository.findAllByMachineServiceIn(machineServices);

		List<MachineServiceBillReceivableResponseDTO> machineServiceBillReceivableResponses = new ArrayList<>();
		for (MachineServiceBillReceivable machineServiceBillReceivable : machineServiceBillReceivables) {
			machineServiceBillReceivableResponses.add(machineServiceBillReceivableDTOMapper(machineServiceBillReceivable));
		}

		return machineServiceBillReceivableResponses;
	}
	
	private MachineServiceBillReceivableResponseDTO machineServiceBillReceivableDTOMapper(
			MachineServiceBillReceivable machineServiceBillReceivable) {

		MachineServiceBillReceivableResponseDTO machineServiceBillReceivableResponseDTO = new MachineServiceBillReceivableResponseDTO();

		machineServiceBillReceivableResponseDTO.setId(machineServiceBillReceivable.getId().toString());
		machineServiceBillReceivableResponseDTO
				.setDescription(machineServiceBillReceivable.getDescription());
		machineServiceBillReceivableResponseDTO.setPrice(String.valueOf(machineServiceBillReceivable.getPrice()));
		machineServiceBillReceivableResponseDTO.setQty(String.valueOf(machineServiceBillReceivable.getQty()));
		
		machineServiceBillReceivableResponseDTO
				.setPayStatus(machineServiceBillReceivable.getBillReceivable().getPayStatus().toString());
		machineServiceBillReceivableResponseDTO.setMachineServiceId(String.valueOf(machineServiceBillReceivable.getMachineService().getId()));
		machineServiceBillReceivableResponseDTO
				.setAmount(String.valueOf(machineServiceBillReceivable.getBillReceivable().getAmount()));

		return machineServiceBillReceivableResponseDTO;

	}

	@Override
	public MachineServiceBillReceivableResponseDTO getMachineServiceBillReceivable(Long id, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
}
