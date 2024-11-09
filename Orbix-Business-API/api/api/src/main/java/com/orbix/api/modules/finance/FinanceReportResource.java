package com.orbix.api.modules.finance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class FinanceReportResource {
	private final ParkingRepository parkingRepository;
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	
	private final CashCollectionRepository cashCollectionRepository;
	
	
	@PostMapping("/finance_reports/get_cash_collections_by_dates")
	public ResponseEntity<List<ICashCollection>>getCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<ICashCollection> cashCollections;

	    if (nickname == null || nickname.isEmpty()) {
	        cashCollections = cashCollectionRepository.findTotalCollectionByDateRange(
	                dateRange.getFrom().atStartOfDay(),
	                dateRange.getTo().atStartOfDay().plusDays(1)
	        );
	    } else {
	        cashCollections = cashCollectionRepository.findTotalCollectionByDateRangeAndCashier(
	                dateRange.getFrom().atStartOfDay(),
	                dateRange.getTo().atStartOfDay().plusDays(1),
	                nickname
	        );
	    }

	    return ResponseEntity.ok().body(cashCollections);

	}
}

@Data
class CashCollectionResponseDTO{
	String reason;
	String amount;
	String paymentType;
	String cashierName;
}

@Data
class DateRange {
	LocalDate from;
	LocalDate to;
}

interface ICashCollection {
	String getReason();
	double getAmount();
	String getPaymentType();
	String getCashierName();
}