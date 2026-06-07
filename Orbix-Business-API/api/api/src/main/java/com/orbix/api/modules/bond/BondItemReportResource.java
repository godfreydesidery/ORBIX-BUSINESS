package com.orbix.api.modules.bond;

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
public class BondItemReportResource {
	private final BondItemRepository bondItemRepository;
	private final BondItemBillReceivableRepository bondItemBillReceivableRepository;
	private final UserService userService;
	private final UserRepository userRepository;
	
	@PostMapping("/bond_item_reports/get_totals_by_dates")
	public ResponseEntity<BondItemTotalsResponseDTO>getTotalsByDates(
			@RequestBody DateRange dateRange,
			HttpServletRequest request){
		
		BondItemTotalsResponseDTO bondItemTotalsResponse = new BondItemTotalsResponseDTO();
		bondItemTotalsResponse.setCheckedIn(String.valueOf(bondItemRepository.countByStatus("CHECKED-IN")));
		
		return ResponseEntity.ok().body(bondItemTotalsResponse);
	}
	
	@PostMapping("/bond_item_reports/get_registration_report")
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
		
		List<BondItem> bondItems = new ArrayList<>();
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		statuses.add("CHECKED-OUT");
		if(user != null) {
			
			bondItems = bondItemRepository.findAllByCreatedByUserAndCreatedDateTimeBetweenAndStatusIn(
			        user, 
			        dateRange.getFrom().atStartOfDay(),
			        dateRange.getTo().atTime(LocalTime.MAX),
			        statuses
			    );		
					
		}else {
			bondItems = bondItemRepository.findAllByCreatedDateTimeBetweenAndStatusIn(
			        dateRange.getFrom().atStartOfDay(),
			        dateRange.getTo().atTime(LocalTime.MAX),
			        statuses
			    );	
		}
		
		List<RegistrationResponseDTO> registrationResponses = new ArrayList<>();
		int sn = 1;
		for(BondItem bondItem : bondItems) {
			RegistrationResponseDTO registrationResponse = new RegistrationResponseDTO();
			registrationResponse.setRegisteredDate(bondItem.getCreatedDateTime().toString());
			registrationResponse.setRegisteredBy(bondItem.getCreatedByUser().getNickname());
			registrationResponse.setSn(String.valueOf(sn));
			registrationResponses.add(registrationResponse);
			sn++;
		}
		return ResponseEntity.ok().body(registrationResponses);
		
	}
	
	@PostMapping("/bond_item_reports/get_bond_item_report")
	public ResponseEntity<List<BondItemResponseDTO>>getBondItemReportByDateAndReceptionist(
			@RequestBody DateRange dateRange,
			@RequestParam(name = "status") String status,
			HttpServletRequest request){
		
		
		List<BondItem> bondItems = new ArrayList<>();
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
		
		bondItems = bondItemRepository.findAllByCheckedInDateTimeBetweenAndStatusIn(
		        dateRange.getFrom().atStartOfDay(),
		        dateRange.getTo().atTime(LocalTime.MAX),
		        statuses
		    );
		
		List<BondItemResponseDTO> bondItemResponses = new ArrayList<>();
		int sn = 1;
		for(BondItem bondItem : bondItems) {
			BondItemResponseDTO bondItemResponse = new BondItemResponseDTO();
			bondItemResponse.setNo(bondItem.getNo());
			bondItemResponse.setBondItemName(bondItem.getBondItemName());
			bondItemResponse.setOwnerFirstName(bondItem.getOwnerFirstName());
			bondItemResponse.setOwnerLastName(bondItem.getOwnerLastName());
			bondItemResponse.setOwnerPhoneNo(bondItem.getOwnerPhoneNo());
			bondItemResponse.setBillingAmount(String.valueOf(bondItem.getBillingAmount()));
			bondItemResponse.setInitialQty(String.valueOf(bondItem.getInitialQty()));
			bondItemResponse.setBillingType(bondItem.getBillingType());
			bondItemResponse.setCheckedInAt(
				    Optional.ofNullable(bondItem.getCheckedInDateTime())
				            .map(Object::toString)
				            .orElse("")
				);
				bondItemResponse.setCheckedOutAt(
				    Optional.ofNullable(bondItem.getCheckedOutDateTime())
				            .map(Object::toString)
				            .orElse("")
				);
			bondItemResponse.setSn(String.valueOf(sn));
			bondItemResponse.setStatus(bondItem.getStatus());
			bondItemResponse.setCreatedBy(bondItem.getCreatedByUser().getNickname());
			
			if(bondItem.getCheckedInByUser() != null) {
				bondItemResponse.setCheckedInBy(bondItem.getCheckedInByUser().getNickname());
			}
			
			if(bondItem.getCheckedOutByUser() != null) {
				bondItemResponse.setCheckedOutBy(bondItem.getCheckedOutByUser().getNickname());
			}
			
			bondItemResponses.add(bondItemResponse);
						
			sn++;
			
		}
		return ResponseEntity.ok().body(bondItemResponses);
		
	}
	
	
}

@Data
class BondItemTotalsResponseDTO{
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

@Data
class BondItemsRemovedResponseDTO{
	String sn;
	String customerName;
	String phoneNo;
	String goodName;
	String qty;
	String reason;
	String dateTime;
	String registeredBy;
	String removedBy;
}
