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
	
	private final CollectionRepository collectionRepository;
	private final BillReceivableCollectionRepository billReceivableCollectionRepository;
	
	
	@PostMapping("/finance_reports/get_cash_collections_by_dates")
	public ResponseEntity<List<IBillReceivableCollection>>getCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<IBillReceivableCollection> collections;
		
		collections = collectionRepository.findTotalCollectionByDateRangeAndCashier(
                dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1),
                nickname
        );

	    return ResponseEntity.ok().body(collections);

	}
	
	@PostMapping("/finance_reports/get_cashier_collections_by_dates")
	public ResponseEntity<List<ICashierCollection>>getCashierCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<ICashierCollection> collections;
		
		collections = billReceivableCollectionRepository.getCashierCollectionsByDateAndCashier(
                dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1),
                nickname
        );

	    return ResponseEntity.ok().body(collections);

	}
	
	
	@PostMapping("/finance_reports/get_parking_detailed_collections_by_dates")
	public ResponseEntity<List<IParkingCollection>>getParkingDetailedCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<IParkingCollection> collections;
		
		collections = collectionRepository.findParkingCollectionsBetweenDates(
				dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1)
				);
	    return ResponseEntity.ok().body(collections);

	}
	
	@PostMapping("/finance_reports/get_parking_service_detailed_collections_by_dates")
	public ResponseEntity<List<IParkingServiceCollection>>getParkingServiceDetailedCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<IParkingServiceCollection> collections;
		
		collections = collectionRepository.findParkingServiceCollectionsBetweenDates(
				dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1)
				);
	    return ResponseEntity.ok().body(collections);
	}
	
	@PostMapping("/finance_reports/get_sales_detailed_collections_by_dates")
	public ResponseEntity<List<ISalesCollection>>getSalesDetailedCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<ISalesCollection> collections;
		
		collections = collectionRepository.findSalesCollectionsBetweenDates(
				dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1)
				);
	    return ResponseEntity.ok().body(collections);

	}
	
	@PostMapping("/finance_reports/get_restaurant_sales_detailed_collections_by_dates")
	public ResponseEntity<List<IRestaurantSalesCollection>>getRestaurantSalesDetailedCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<IRestaurantSalesCollection> collections;
		
		collections = collectionRepository.findRestaurantSalesCollectionsBetweenDates(
				dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1)
				);
	    return ResponseEntity.ok().body(collections);

	}
	
	@PostMapping("/finance_reports/get_storage_detailed_collections_by_dates")
	public ResponseEntity<List<IStorageCollection>>getStorageDetailedCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<IStorageCollection> collections;
		
		collections = collectionRepository.findStorageCollectionsBetweenDates(
				dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1)
				);
	    return ResponseEntity.ok().body(collections);

	}
	
	@PostMapping("/finance_reports/get_maintenance_detailed_collections_by_dates")
	public ResponseEntity<List<IMaintenanceCollection>>getMaintenanceDetailedCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<IMaintenanceCollection> collections;
		
		collections = collectionRepository.findMaintenanceCollectionsBetweenDates(
				dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1)
				);
	    return ResponseEntity.ok().body(collections);

	}
	
	@PostMapping("/finance_reports/get_weigh_detailed_collections_by_dates")
	public ResponseEntity<List<IWeighCollection>>getWeighDetailedCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<IWeighCollection> collections;
		
		collections = collectionRepository.findWeighCollectionsBetweenDates(
				dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1),
                dateRange.getNickname()
				);
	    return ResponseEntity.ok().body(collections);

	}
	
	@PostMapping("/finance_reports/get_workshop_detailed_collections_by_dates")
	public ResponseEntity<List<IWorkshopCollection>>getWorkshopDetailedCollectionByDates(
			@RequestBody DateRange dateRange,
	        @RequestParam(name = "nickname", required = false) String nickname,
			HttpServletRequest request){
		
		List<IWorkshopCollection> collections;
		
		collections = collectionRepository.findWorkshopCollectionsBetweenDates(
				dateRange.getFrom().atStartOfDay(),
                dateRange.getTo().atStartOfDay().plusDays(1),
                dateRange.getNickname()
				);
	    return ResponseEntity.ok().body(collections);
	}
	
}

@Data
class CollectionResponseDTO{
	String reason;
	String amount;
	String paymentType;
	String cashierName;
}

@Data
class DateRange {
	LocalDate from;
	LocalDate to;
	String nickname;
}

