package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivableRepository;
import com.orbix.api.api.vehicleandequipmentparking.ParkingRepository;
import com.orbix.api.modules.identityandaccess.UserRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class RestaurantSalesReportResource {
//	private final ParkingRepository parkingRepository;
//	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
//	private final UserService userService;
//	private final UserRepository userRepository;
//	
//	private final SaleRepository saleRepository;
//	
//	
//	@PostMapping("/restaurant_sales_reports/get_sales_listing_report_by_dates")
//	public ResponseEntity<List<ISalesListing>> getSalesListingReportByDates(
//	        @RequestBody DateRange dateRange,
//	        HttpServletRequest request) {
//
//	    List<ISalesListing> report = saleRepository.getSalesListingReportByDates(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atStartOfDay().plusDays(1));
//	    
//
//	    return ResponseEntity.ok(report);
//	}
//	
//	@PostMapping("/restaurant_sales_reports/get_fast_moving_products_report_by_dates")
//	public ResponseEntity<List<IFastMovingProducts>> getFastMovingProductsReportByDates(
//	        @RequestBody DateRange dateRange,
//	        HttpServletRequest request) {
//
//	    List<IFastMovingProducts> report = saleRepository.getFastMovingProductsByDates(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atStartOfDay().plusDays(1));
//	    
//
//	    return ResponseEntity.ok(report);
//	}
}

//@Data
//class DateRange {
//	LocalDate from;
//	LocalDate to;
//}