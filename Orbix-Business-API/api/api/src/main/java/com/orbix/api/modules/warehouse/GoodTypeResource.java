package com.orbix.api.modules.warehouse;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class GoodTypeResource {
	private final GoodTypeService goodTypeService;
	private final UserService userService;
	private final GoodTypeRepository goodTypeRepository;
	
	@GetMapping("/good_types")
	public ResponseEntity<List<GoodTypeResponseDTO>>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(goodTypeService.getAllGoodTypes(request));
	}
	
	@GetMapping("/good_types/get_all_company_active")
	public ResponseEntity<List<GoodTypeResponseDTO>>getAllCompanyActive(HttpServletRequest request){		
		return ResponseEntity.ok().body(goodTypeService.getAllCompanyActiveGoodTypes(request));
	}
	
	
	@GetMapping("/good_types/get")
	public ResponseEntity<GoodTypeResponseDTO>get(
			Long id,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(goodTypeService.get(id, request));		
	}
	
	@PostMapping("/good_types/create")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<GoodTypeResponseDTO>create(
			@RequestBody GoodTypeRequestDTO goodTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/good_types/create").toUriString());
		return ResponseEntity.created(uri).body(goodTypeService.createGoodType(goodTypeRequest, request));
	}
	
	@PostMapping("/good_types/update")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<GoodTypeResponseDTO>update(
			@RequestBody GoodTypeRequestDTO goodTypeRequest,
			HttpServletRequest request){		
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/good_types/update").toUriString());
		return ResponseEntity.created(uri).body(goodTypeService.updateGoodType(goodTypeRequest, request));
	}
	
	@PostMapping("/good_types/activate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>activate(
			@RequestBody GoodTypeRequestDTO goodTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/good_types/activate").toUriString());
		return ResponseEntity.created(uri).body(goodTypeService.activateGoodType(goodTypeRequest, request));
	}
	
	
	@PostMapping("/good_types/deactivate")
	//@PreAuthorize("hasAnyAuthority('COM-ALL')")
	public ResponseEntity<ApiCustomResponse>deactivate(
			@RequestBody GoodTypeRequestDTO goodTypeRequest,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/good_types/deactivate").toUriString());
		return ResponseEntity.created(uri).body(goodTypeService.deactivateGoodType(goodTypeRequest, request));
	}
}
