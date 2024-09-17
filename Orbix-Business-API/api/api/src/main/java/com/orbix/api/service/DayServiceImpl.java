/**
 * 
 */
package com.orbix.api.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.domain.Day;
import com.orbix.api.repositories.DayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author GODFREY
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DayServiceImpl implements DayService{
	
	private final DayRepository dayRepository;
	
	@Override
	public String getBussinessDate() {		
		Day day = dayRepository.getCurrentBussinessDay();
		return day.getBussinessDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	@Override
	public Day saveDay(Day day) {
		// TODO Auto-generated method stub
		return dayRepository.save(day);
	}

	@Override
	public boolean hasData() {
		return dayRepository.hasData();
	}

	@Override
	public boolean endDay() {
		// TODO Auto-generated method stub		
		log.info("Ending the day");
		Day oldDay = dayRepository.getCurrentBussinessDay();
		Day newDay = new Day();
		oldDay.setEndedAt(new Date());
		oldDay.setStatus("ENDED");
		dayRepository.saveAndFlush(oldDay);
		newDay.setStartedAt(new Date());
		newDay.setBussinessDate(oldDay.getBussinessDate());
		if(newDay.getBussinessDate().equals(LocalDate.now())) {
			newDay.setBussinessDate(LocalDate.now().plusDays(1));
		}else if(newDay.getBussinessDate().isAfter(LocalDate.now())) {
			//do nothing
		}else if(newDay.getBussinessDate().isBefore(LocalDate.now())) {
			newDay.setBussinessDate(LocalDate.now());
		}
		dayRepository.saveAndFlush(newDay);		
		return true;
	}

	@Override
	public Long getDayId() {
		return dayRepository.getLastId();
	}
	
	@Override
	public Day getDay() {
		return dayRepository.findById(dayRepository.getLastId()).get();
	}
	
	@Override
	public LocalDateTime getTimeStamp() {
		return LocalDateTime.now().plusHours(3);
	}

}
