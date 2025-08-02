package com.orbix.api.modules.weighbridge;

import java.net.URI;
import java.util.List;

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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.vehicleandequipmentmaintenance.ServiceSpecialistRepository;
import com.orbix.api.modules.vehicleandequipmentmaintenance.ServiceSpecialistRequestDTO;
import com.orbix.api.modules.vehicleandequipmentmaintenance.ServiceSpecialistResponseDTO;
import com.orbix.api.modules.vehicleandequipmentmaintenance.ServiceSpecialistService;
import com.orbix.api.modules.warehouse.StorageResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class WeighResource {
	
	private final WeighService weighService;
	
	@GetMapping("/weighs/recent")
	public ResponseEntity<List<WeighResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(weighService.getAllWeighs(request));
	}
	
	@GetMapping("/weighs/get")
	public ResponseEntity<WeighResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(weighService.get(id, request));		
	}
	
	@PostMapping("/weighs/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<WeighResponseDTO>create(
			@RequestBody WeighRequestDTO weighRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/weighs/create").toUriString());
		return ResponseEntity.created(uri).body(weighService.createWeigh(weighRequest, request));
	}
	
	@PostMapping("/weighs/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<WeighResponseDTO>update(
			@RequestBody WeighRequestDTO weighRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/weighs/update").toUriString());
		return ResponseEntity.created(uri).body(weighService.updateWeigh(weighRequest, request));
	}
	
	@PostMapping("/weighs/recheck")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public boolean recheck(
			@RequestParam Long id,
			HttpServletRequest request){		
		return weighService.recheck(id);
	}
	
	
//	private final ServiceSpecialistService serviceSpecialistService;
//	private final UserService userService;
//	private final ServiceSpecialistRepository serviceSpecialistRepository;
//	
//	@GetMapping("/service_specialists")
//	public ResponseEntity<List<ServiceSpecialistResponseDTO>>getAll(HttpServletRequest request){
//		return ResponseEntity.ok().body(serviceSpecialistService.getAllServiceSpecialists(request));
//	}
//	
//	@GetMapping("/service_specialists/get_all_branch_active")
//	public ResponseEntity<List<ServiceSpecialistResponseDTO>>getAllCompanyActive(HttpServletRequest request){		
//		return ResponseEntity.ok().body(serviceSpecialistService.getAllBranchActiveServiceSpecialists(request));
//	}
//	
//	
//	@GetMapping("/service_specialists/get")
//	public ResponseEntity<ServiceSpecialistResponseDTO>get(
//			Long id,
//			HttpServletRequest request){		
//		return ResponseEntity.ok().body(serviceSpecialistService.get(id, request));		
//	}
//	
//	@PostMapping("/service_specialists/create")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ServiceSpecialistResponseDTO>create(
//			@RequestBody ServiceSpecialistRequestDTO serviceSpecialistRequest,
//			HttpServletRequest request){		
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/service_specialists/create").toUriString());
//		return ResponseEntity.created(uri).body(serviceSpecialistService.createServiceSpecialist(serviceSpecialistRequest, request));
//	}
//	
//	@PostMapping("/service_specialists/update")
//	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
//	public ResponseEntity<ServiceSpecialistResponseDTO>update(
//			@RequestBody ServiceSpecialistRequestDTO serviceSpecialistRequest,
//			HttpServletRequest request){		
//		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/service_specialists/update").toUriString());
//		return ResponseEntity.created(uri).body(serviceSpecialistService.updateServiceSpecialist(serviceSpecialistRequest, request));
//	}
}
