package com.orbix.api.modules.adminunits;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class SystemProfileResource {
	
	private final SystemProfileService systemProfileService;
	
	@GetMapping("/system")
	public ResponseEntity<SystemProfile>getAll(HttpServletRequest request){
		return ResponseEntity.ok().body(systemProfileService.getSystemProfile(request));
	}
}
