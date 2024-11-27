package com.orbix.api.modules.adminunits;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
	
	
	@PostMapping("/company_profile/save_logo")
	//@PreAuthorize("hasAnyAuthority('ADMIN-ACCESS')")
	public ResponseEntity<SystemProfile> saveCompanyLogo(
			@RequestParam("logo") MultipartFile logo,
			HttpServletRequest request) throws IOException{
		SystemProfile profile = systemProfileService.getSystemProfile(request);
		//profile.setLogo(compressBytes(logo.getBytes())); inazingua
		profile.setLogo(logo.getBytes());
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/system_profile/save_logo").toUriString());
		return ResponseEntity.created(uri).body(systemProfileService.saveSystemProfile(profile));
	}
	
	@GetMapping("/company_profile/get_logo")
	public ResponseEntity<SystemProfile> getLogo(HttpServletRequest request) {
		SystemProfile profile = systemProfileService.getSystemProfile(request);
		//profile.setLogo(decompressBytes(profile.getLogo())); inazingua
		profile.setLogo(profile.getLogo());
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/orbix-business-api/system_profile/get_logo").toUriString());
		return ResponseEntity.created(uri).body(profile);
	}
}
