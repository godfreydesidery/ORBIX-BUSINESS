package com.orbix.api.modules.servicebay;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.exceptions.InvalidOperationException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class MachineServiceResource {
	
	private final MachineServiceService machineServiceService;
	private final MachineServiceRepository machineServiceRepository;
	
	/**
	 * Create a new MachineService
	 */
	@PostMapping("/machine-services/create")
	public ResponseEntity<MachineServiceResponseDTO> createMachineService(@RequestBody MachineServiceRequestDTO dto,  HttpServletRequest request) {
		MachineServiceResponseDTO response = machineServiceService.createMachineService(dto, request);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/machine-services/confirm")
	public boolean confirm(@RequestParam("machine_id") Long machineId, HttpServletRequest request) {
		machineServiceService.confirm(machineId, request);
		return true;
	}
	
	@GetMapping("/machine-services/remove")
	public boolean remove(@RequestParam Long id) {
		MachineService s = machineServiceRepository.findById(id).orElseThrow();
		if(s.getStatus().equals("PENDING")) {
			machineServiceRepository.delete(s);
		}else {
			throw new InvalidOperationException("Not pending");
		}		
		return true;
	}
}
