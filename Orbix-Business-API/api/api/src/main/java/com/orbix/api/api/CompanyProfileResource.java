/**
 * 
 */
package com.orbix.api.api;

import java.io.IOException;
import java.net.URI;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.orbix.api.domain.CompanyProfile;
import com.orbix.api.service.CompanyProfileService;
import com.orbix.api.service.DayService;
import com.orbix.api.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * @author GODFREY
 *
 */
@RestController
@RequestMapping("/zana-hmis-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class CompanyProfileResource {
	
	private final 	CompanyProfileService companyProfileService;
	

	private final UserService userService;
	private final DayService dayService;
	
	@GetMapping("/company_profile/get")
	public ResponseEntity<CompanyProfile> getCompanyProfile(HttpServletRequest request){		
		return ResponseEntity.ok().body(companyProfileService.getCompanyProfile(request));
	}
	
	@PostMapping("/company_profile/save")
	@PreAuthorize("hasAnyAuthority('ADMIN-ACCESS')")
	public ResponseEntity<CompanyProfile>saveCompanyProfile(
			@RequestBody CompanyProfile profile,
			HttpServletRequest request){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/company_profile/save").toUriString());
		return ResponseEntity.created(uri).body(companyProfileService.saveCompanyProfile(profile));
	}
	
	@PostMapping("/company_profile/save_logo")
	@PreAuthorize("hasAnyAuthority('ADMIN-ACCESS')")
	public ResponseEntity<CompanyProfile> saveCompanyLogo(
			@RequestParam("logo") MultipartFile logo,
			HttpServletRequest request) throws IOException{
		CompanyProfile profile = companyProfileService.getCompanyProfile(request);
		//profile.setLogo(compressBytes(logo.getBytes())); inazingua
		profile.setLogo(logo.getBytes());
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/zana-hmis-api/company_profile/save_logo").toUriString());
		return ResponseEntity.created(uri).body(companyProfileService.saveCompanyProfile(profile));
	}
	
	@GetMapping("/company_profile/get_logo")
	public ResponseEntity<CompanyProfile> getLogo(HttpServletRequest request) {
		CompanyProfile profile = companyProfileService.getCompanyProfile(request);
		//profile.setLogo(decompressBytes(profile.getLogo())); inazingua
		profile.setLogo(profile.getLogo());
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/zana-hmis-api/company_profile/get_logo").toUriString());
		return ResponseEntity.created(uri).body(profile);
	}
	
	
	// compress the image bytes before storing it in the database

    public static byte[] compressBytes(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
        }
        System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);
        return outputStream.toByteArray();
    }


    // uncompress the image bytes before returning it to the angular application

    public static byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException ioe) {
        } catch (DataFormatException e) {
        }
        return outputStream.toByteArray();
    }

}
