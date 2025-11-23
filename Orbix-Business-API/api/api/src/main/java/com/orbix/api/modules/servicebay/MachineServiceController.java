package com.orbix.api.modules.servicebay;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Workshop;
import com.orbix.api.modules.adminunits.WorkshopRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.warehouse.Warehouse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MachineServiceController implements MachineServiceInterface {
	
	private final MachineRepository machineRepository;
	private final WorkshopRepository workshopRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public MachineResponseDTO get(Long id) {
		Optional<Machine> machine_ = machineRepository.findById(id);
		if (machine_.isEmpty()) {
			throw new NotFoundException("Machine not found");
		}
		return toDtoWithDetails(machine_.get());
	}

	@Override
	public MachineResponseDTO createMachine(MachineRequestDTO machineRequest, HttpServletRequest request) {
		
		// Validate required fields
	    if (machineRequest.getRegNo() == null || machineRequest.getRegNo().isBlank()) {
	        throw new InvalidOperationException("Registration number must not be empty");
	    }
	    if (machineRequest.getName() == null || machineRequest.getName().isBlank()) {
	        throw new InvalidOperationException("Machine name must not be empty");
	    }
	    if (machineRequest.getOwnerName() == null || machineRequest.getOwnerName().isBlank()) {
	        throw new InvalidOperationException("Owner name must not be empty");
	    }
		
		Workshop workshop = workshopRepository.findById(machineRequest.getWorkshopId()).orElseThrow(() -> 
			new NotFoundException("Workshop not found with id " + machineRequest.getWorkshopId()) 
		);
		
		Machine machine = new Machine();
		
		machine.setNo(String.valueOf(Math.random()));
		machine.setRegNo(machineRequest.getRegNo());
		machine.setName(machineRequest.getName());
		machine.setOwnerName(machineRequest.getOwnerName());
		machine.setOwnerPhoneNo(machineRequest.getOwnerPhoneNo());
		machine.setWorkshop(workshop);
		machine.setBranch(userService.getUserBranch(request));
		machine.setCreatedByUser(userService.getUser(request));
		machine.setCreatedDateTime(dayService.getTimeStamp());
		machine.setStatus("PENDING");
		
		machine = machineRepository.save(machine);
		
		machine.setNo(machine.getId().toString());
		
		machine = machineRepository.save(machine);
		
		return toDtoWithDetails(machine);
	}
	
	@Override
	public MachineResponseDTO updateMachine(MachineRequestDTO machineRequest, HttpServletRequest request) {
	    Machine machine = machineRepository.findById(machineRequest.getId()).orElseThrow(() ->
	        new NotFoundException("Machine not found with id " + machineRequest.getId())
	    );

	    // Update editable fields
	    machine.setOwnerName(machineRequest.getOwnerName());
	    machine.setOwnerPhoneNo(machineRequest.getOwnerPhoneNo());
	    machine.setRegNo(machineRequest.getRegNo());
	    machine.setName(machineRequest.getName());
	    
	    // Save changes
	    machine = machineRepository.save(machine);

	    return toDtoWithDetails(machine);
	}
	
//	@Override
//	public List<MachineResponseDTO> getMachinesByWorkshop(Long workshopId) {
//	    return machineRepository.findByWorkshopId(workshopId)
//	            .stream()
//	            .map(this::toDto)
//	            .toList(); // returns an unmodifiable List in Java 21
//	}
	
	@Override
	public List<MachineResponseDTO> getMachinesByWorkshop(Long workshopId) {
		LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

	    return machineRepository.findRecentByWorkshopId(workshopId, twentyFourHoursAgo)
	            .stream()
	            .map(this::toDto)
	            .toList();
	}
	
	private MachineResponseDTO toDto(Machine machine) {
		if (machine == null) {
			return null;
		}

		MachineResponseDTO dto = new MachineResponseDTO();
		dto.setId(machine.getId() != null ? machine.getId().toString() : null);
		dto.setNo(machine.getNo());
		dto.setOwnerName(machine.getOwnerName());
		dto.setOwnerPhoneNo(machine.getOwnerPhoneNo());
		dto.setMachineRegNo(machine.getRegNo());
		dto.setName(machine.getName());
		dto.setMachineName(machine.getName());
		dto.setStatus(machine.getStatus());

		// Workshop
		if (machine.getWorkshop() != null) {
			dto.setWorkshopId(machine.getWorkshop().getId() != null ? machine.getWorkshop().getId().toString() : null);
			dto.setWorkshopName(machine.getWorkshop().getName());
		}

		// Created by User
		if (machine.getCreatedByUser() != null) {
			dto.setCreatedBy(machine.getCreatedByUser().getNickname()); // Or username/email depending on your User
																		// entity
		}

		// Created date formatting
		if (machine.getCreatedDateTime() != null) {
			dto.setCreatedAt(machine.getCreatedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		}
		
		return dto;
	}
	
	private MachineResponseDTO toDtoWithDetails(Machine machine) {
		if (machine == null) {
			return null;
		}

		MachineResponseDTO dto = new MachineResponseDTO();
		dto.setId(machine.getId() != null ? machine.getId().toString() : null);
		dto.setNo(machine.getNo());
		dto.setOwnerName(machine.getOwnerName());
		dto.setOwnerPhoneNo(machine.getOwnerPhoneNo());
		dto.setMachineRegNo(machine.getRegNo());
		dto.setName(machine.getName());
		dto.setMachineName(machine.getName());
		dto.setStatus(machine.getStatus());

		// Workshop
		if (machine.getWorkshop() != null) {
			dto.setWorkshopId(machine.getWorkshop().getId() != null ? machine.getWorkshop().getId().toString() : null);
			dto.setWorkshopName(machine.getWorkshop().getName());
		}

		// Created by User
		if (machine.getCreatedByUser() != null) {
			dto.setCreatedBy(machine.getCreatedByUser().getNickname()); // Or username/email depending on your User
																		// entity
		}

		// Created date formatting
		if (machine.getCreatedDateTime() != null) {
			dto.setCreatedAt(machine.getCreatedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		}
		
		List<MachineServiceResponseDTO> services = new ArrayList<>();
		
		List<MachineService> machineServices = machine.getMachineServices();
		
		if(machineServices != null) {
			int sn = 0;
			for(MachineService machineService : machineServices) {
				MachineServiceResponseDTO service = new MachineServiceResponseDTO();
				service.setSn(String.valueOf(++sn));
				service.setId(machineService.getId().toString());
				service.setServiceName(machineService.getService().getName());
				service.setQty(String.valueOf(machineService.getQty()));
				service.setPrice(String.valueOf(machineService.getPrice()));
				service.setStatus(machineService.getStatus());
				
				services.add(service);
			}
		}
		
		dto.setMachineServices(services);
		
		return dto;
	}

	@Override
	public List<MachineResponseDTO> getMachinesByBranch(HttpServletRequest request) {
		LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
		
		Branch branch = userService.getUserBranch(request);

	    return machineRepository.findRecentByBranch(branch, twentyFourHoursAgo)
	            .stream()
	            .map(this::toDto)
	            .toList();
	}

	

	

}
