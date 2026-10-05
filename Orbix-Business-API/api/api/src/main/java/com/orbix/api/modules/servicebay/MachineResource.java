package com.orbix.api.modules.servicebay;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.modules.warehouse.WarehouseResponseDTO;
import com.orbix.api.api.commons.PageResponseDTO;

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
    
    @GetMapping("/machines/by_branch")
    public ResponseEntity<List<MachineResponseDTO>> getByBranch(
    		HttpServletRequest request) {
        return ResponseEntity.ok().body(machineService.getMachinesByBranch(request));
    }

	@GetMapping("/machines/by_workshop_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<MachineResponseDTO>>getByWorkshopPage(
			@RequestParam(name = "workshop_id") Long workshopId,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(machineService.getMachinePageByWorkshop(workshopId, page, size, search, request));
	}

	@GetMapping("/machines/by_branch_page")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ResponseEntity<PageResponseDTO<MachineResponseDTO>>getByBranchPage(
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size,
			@RequestParam(name = "search", defaultValue = "") String search,
			HttpServletRequest request){
		return ResponseEntity.ok().body(machineService.getMachinePageByBranch(page, size, search, request));
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
