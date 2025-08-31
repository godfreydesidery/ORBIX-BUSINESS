package com.orbix.api.modules.servicebay;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.modules.warehouse.WarehouseResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class MachineResource {
	
	private final MachineServiceInterface machineService;
	
	@GetMapping("/machines/get")
	public ResponseEntity<MachineResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(machineService.get(id));		
	}

    @GetMapping("/machines/by_workshop")
    public ResponseEntity<List<MachineResponseDTO>> getByWorkshop(
            @RequestParam(name = "workshop_id") Long workshopId) {
        return ResponseEntity.ok().body(machineService.getMachinesByWorkshop(workshopId));
    }

    @PostMapping("/machines/create")
    public ResponseEntity<MachineResponseDTO> create(
            @RequestBody MachineRequestDTO machineRequest,
            HttpServletRequest request) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/orbix-business-api/machines/create").toUriString());
        return ResponseEntity.created(uri).body(machineService.createMachine(machineRequest, request));
    }

    @PostMapping("/machines/update")
    public ResponseEntity<MachineResponseDTO> update(
            @RequestBody MachineRequestDTO machineRequest,
            HttpServletRequest request) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/orbix-business-api/machines/update").toUriString());
        return ResponseEntity.created(uri).body(machineService.updateMachine(machineRequest, request));
    }
}
