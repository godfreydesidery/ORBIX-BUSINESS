/**
 * 
 */
package com.orbix.api;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.orbix.api.repositories.DayRepository;
import com.orbix.api.repositories.UserRepository;
import com.orbix.api.service.DayService;
import com.orbix.api.service.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Godfrey
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@Data
public class UpdatePatient implements Runnable{

	/**
	 * These beans should appear in this order
	 * Please do not change order as parameter list order will change.
	 * To add a bean, add bean at the end of this list
	 * then add respective parameters to the calling class
	 */
	
	private final DayService dayService;	
	
	
	/**
	 * To add bean, add above this comment
	 */
	
	@Override
	public void run() {
		
		
	}
}
