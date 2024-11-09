package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class ReportResource {
	
	private final ParkingRepository parkingRepository;
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	
	
	@PostMapping("/parking_reports/get_totals_by_dates")
	public ResponseEntity<ParkingTotalsResponseDTO>getTotalsByDates(
			@RequestBody DateRange dateRange,
			HttpServletRequest request){
		
		ParkingTotalsResponseDTO parkingTotalsResponse = new ParkingTotalsResponseDTO();
		parkingTotalsResponse.setFrom(dateRange.getFrom().toString());
		parkingTotalsResponse.setTo(dateRange.getTo().toString());
		parkingTotalsResponse.setRegistered("0");
		parkingTotalsResponse.setPaid("0");
		parkingTotalsResponse.setCheckedOut("0");
		parkingTotalsResponse.setCurrentUnpaid("0");
		parkingTotalsResponse.setCurrentTotalInYards("0");
		
		// Count registered vehicles
		
//		@Query("SELECT COUNT(p) FROM Parking p WHERE p.checkedInDateTime BETWEEN :startDate AND :endDate AND status IN :statuses")
//	    long countByDateRangeAndRegistered(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, List<String> statuses);

		List<String> regStatuses = new ArrayList<>();
		regStatuses.add("CHECKED-IN");
		regStatuses.add("CHECKED-OUT");
		parkingTotalsResponse.setRegistered(String.valueOf(parkingRepository.countByDateRangeAndRegistered(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atStartOfDay().plusDays(1), regStatuses)));
		
		parkingTotalsResponse.setPaid(String.valueOf(parkingBillReceivableRepository.countByStatusAndDateRange(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atStartOfDay().plusDays(1))));
		
		List<String> checkOutStatuses = new ArrayList<>();
		checkOutStatuses.add("CHECKED-OUT");
		parkingTotalsResponse.setCheckedOut(String.valueOf(parkingRepository.countByDateRangeAndCheckedOut(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atStartOfDay().plusDays(1), checkOutStatuses)));
		
		parkingTotalsResponse.setCurrentUnpaid(String.valueOf(parkingRepository.countRegistered()));
		parkingTotalsResponse.setCurrentTotalInYards(String.valueOf(parkingRepository.countRegistered()));
		
		
//		public long countPaidOrVerifiedBillsWithinDateRange(LocalDateTime startDate, LocalDateTime endDate) {
//	        return parkingBillReceivableRepository.countByStatusAndDateRange(startDate, endDate);
//	    }
		
		return ResponseEntity.ok().body(parkingTotalsResponse);
	}
}

@Data
class ParkingTotalsResponseDTO{
	String from;
	String to;
	String registered;
	String paid;
	String checkedOut;
	String currentUnpaid;
	String currentTotalInYards;	
}

@Data
class DateRange {
	LocalDate from;
	LocalDate to;
}
