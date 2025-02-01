package com.orbix.api.modules.inventoryandprocurement;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
public class ReportResource {
	
	private final ShopProductLogRepository shopProductLogRepository;
	
//	private final ParkingRepository parkingRepository;
//	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
//	private final UserService userService;
//	private final UserRepository userRepository;
//	
//	
//	@PostMapping("/parking_reports/get_sales_listing_report_by_dates")
//	public ResponseEntity<List<SalesProdoductResponseDTO>> getSalesListingReportByDates(
//	        @RequestBody DateRange dateRange,
//	        HttpServletRequest request) {
//
//	    List<SalesProdoductResponseDTO> report = new ArrayList<>();
//	    
//
//	    return ResponseEntity.ok(report);
//	}
	
//	public List<StockLogReportProjection> getStockLogReport(Long shopId) {
//        return shopProductLogRepository.getStockLogReportByShop(shopId);
//    }
	
	@PostMapping("/shop_stock_logs/get_stock_logs_report_by_dates")
	public ResponseEntity<List<StockLogReportProjection>> getSalesListingReportByDates(
			@RequestParam(name = "shop_id") Long shopId,
	        @RequestBody DateRange dateRange,
	        HttpServletRequest request) {

	    List<StockLogReportProjection> report = shopProductLogRepository.getStockLogReportByShop(shopId, dateRange.getFrom().atStartOfDay(), dateRange.getTo().atStartOfDay().plusDays(1));
	    
	    return ResponseEntity.ok(report);
	}
}

//@Data
//class SalesProdoductResponseDTO {
//	public String sn;
//	public String productName;
//	public String qty;
//	public String amount;
//	public String soldBy;
//	public String dateTime;
//}
//
@Data
class DateRange {
	LocalDate from;
	LocalDate to;
}

