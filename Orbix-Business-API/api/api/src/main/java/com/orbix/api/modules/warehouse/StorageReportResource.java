package com.orbix.api.modules.warehouse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class StorageReportResource {
	private final StorageRepository storageRepository;
	private final StorageBillReceivableRepository storageBillReceivableRepository;
	private final UserService userService;
	private final UserRepository userRepository;
	
	
	@PostMapping("/storage_reports/get_totals_by_dates")
	public ResponseEntity<StorageTotalsResponseDTO>getTotalsByDates(
			@RequestBody DateRange dateRange,
			HttpServletRequest request){
		
		StorageTotalsResponseDTO storageTotalsResponse = new StorageTotalsResponseDTO();
		storageTotalsResponse.setCheckedIn(String.valueOf(storageRepository.countByStatus("CHECKED-IN")));
		
		return ResponseEntity.ok().body(storageTotalsResponse);
	}
	
	@PostMapping("/storage_reports/get_registration_report")
	public ResponseEntity<List<RegistrationResponseDTO>>getRegistrationReportByDateAndReceptionist(
			@RequestBody DateRange dateRange,
			@RequestParam(name = "nickname") String cashierName,
			HttpServletRequest request){
		
		User user = null;
		if(!cashierName.equals("")) {
			Optional<User> user_ = userRepository.findByNickname(cashierName);
			if(user_.isPresent()) {
				user = user_.get();
			}else {
				throw new NotFoundException("User not found");
			}
		}
		
		List<Storage> storages = new ArrayList<>();
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		statuses.add("CHECKED-OUT");
		if(user != null) {
			
			storages = storageRepository.findAllByCreatedByUserAndCreatedDateTimeBetweenAndStatusIn(
			        user, 
			        dateRange.getFrom().atStartOfDay(),
			        dateRange.getTo().atTime(LocalTime.MAX),
			        statuses
			    );		
					
		}else {
			storages = storageRepository.findAllByCreatedDateTimeBetweenAndStatusIn(
			        dateRange.getFrom().atStartOfDay(),
			        dateRange.getTo().atTime(LocalTime.MAX),
			        statuses
			    );	
		}
		
		List<RegistrationResponseDTO> registrationResponses = new ArrayList<>();
		int sn = 1;
		for(Storage storage : storages) {
			RegistrationResponseDTO registrationResponse = new RegistrationResponseDTO();
			registrationResponse.setRegisteredDate(storage.getCreatedDateTime().toString());
			registrationResponse.setRegisteredBy(storage.getCreatedByUser().getNickname());
			registrationResponse.setSn(String.valueOf(sn));
			registrationResponses.add(registrationResponse);
			sn++;
		}
		return ResponseEntity.ok().body(registrationResponses);
		
	}
	
	@PostMapping("/storage_reports/get_storage_report")
	public ResponseEntity<List<StorageResponseDTO>>getStorageReportByDateAndReceptionist(
			@RequestBody DateRange dateRange,
			@RequestParam(name = "status") String status,
			HttpServletRequest request){
		
		
		List<Storage> storages = new ArrayList<>();
		List<String> statuses = new ArrayList<>();
		if(status.equals("") || status.equals("--All--")) {
			statuses.add("CHECKED-IN");
			statuses.add("CHECKED-OUT");
		}else if(status.equals("Checked In")) {
			statuses.add("CHECKED-IN");
		}else if(status.equals("Checked Out")) {
			statuses.add("CHECKED-OUT");
		}else {
			throw new InvalidOperationException("Invalid Option selected");
		}
		
		storages = storageRepository.findAllByCheckedInDateTimeBetweenAndStatusIn(
		        dateRange.getFrom().atStartOfDay(),
		        dateRange.getTo().atTime(LocalTime.MAX),
		        statuses
		    );
		
		List<StorageResponseDTO> storageResponses = new ArrayList<>();
		int sn = 1;
		for(Storage storage : storages) {
			StorageResponseDTO storageResponse = new StorageResponseDTO();
			storageResponse.setNo(storage.getNo());
			storageResponse.setGoodName(storage.getGoodName());
			storageResponse.setOwnerFirstName(storage.getOwnerFirstName());
			storageResponse.setOwnerLastName(storage.getOwnerLastName());
			storageResponse.setOwnerPhoneNo(storage.getOwnerPhoneNo());
			storageResponse.setBillingAmount(String.valueOf(storage.getBillingAmount()));
			storageResponse.setInitialQty(String.valueOf(storage.getInitialQty()));
			storageResponse.setBillingType(storage.getBillingType());
			storageResponse.setCheckedInAt(
				    Optional.ofNullable(storage.getCheckedInDateTime())
				            .map(Object::toString)
				            .orElse("")
				);
				storageResponse.setCheckedOutAt(
				    Optional.ofNullable(storage.getCheckedOutDateTime())
				            .map(Object::toString)
				            .orElse("")
				);
			storageResponse.setSn(String.valueOf(sn));
			storageResponse.setStatus(storage.getStatus());
			storageResponse.setCreatedBy(storage.getCreatedByUser().getNickname());
			
			if(storage.getCheckedInByUser() != null) {
				storageResponse.setCheckedInBy(storage.getCheckedInByUser().getNickname());
			}
			
			if(storage.getCheckedOutByUser() != null) {
				storageResponse.setCheckedOutBy(storage.getCheckedOutByUser().getNickname());
			}
			
			storageResponses.add(storageResponse);
						
			sn++;
			
		}
		return ResponseEntity.ok().body(storageResponses);
		
	}	
}

@Data
class StorageTotalsResponseDTO{
	String checkedIn;	
}

@Data
class DateRange {
	LocalDate from;
	LocalDate to;
}

@Data
class RegistrationResponseDTO{
	String sn;
	String chassisNo;
	String vehicleType;
	String keyStatus;
	String registeredDate;
	String registeredBy;	
}
