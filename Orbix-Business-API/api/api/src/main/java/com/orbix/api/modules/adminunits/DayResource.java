/**
 * 
 */
package com.orbix.api.modules.adminunits;

import java.time.LocalDate;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.audit.Audited;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author GODFREY
 *
 */
@RestController
@RequestMapping("/zana-hmis-api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class DayResource {
	
	private final UserService userService;
	private final DayService dayService;
	
	@GetMapping("/days/get_bussiness_date")
	public DayData getBussinessDate(HttpServletRequest request){
		DayData dayData = new DayData();
		dayData.setBussinessDate(dayService.getBussinessDate());
		return dayData;
	}
	
	// POST is the intended method; GET still works so that nothing calling it today breaks
	@RequestMapping(value = "/days/end_day", method = {RequestMethod.GET, RequestMethod.POST})
	@Audited(category = "FINANCE", action = "DAY_ENDED", entityType = "Day", entityId = "", summary = "Ended the business day")
	@PreAuthorize("hasAnyAuthority('ADMIN-ACCESS','DAY-ACCESS')")
	public boolean endDay(HttpServletRequest request){		
		return dayService.endDay();
	}
}
@Data
class DayData{
	public String bussinessDate;	
}
