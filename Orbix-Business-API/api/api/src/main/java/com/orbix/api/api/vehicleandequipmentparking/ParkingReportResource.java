package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.warehouse.RemovedGood;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class ParkingReportResource {
	
	private final ParkingRepository parkingRepository;
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	private final UserService userService;
	private final UserRepository userRepository;
	
	private final RemovedVehicleEquipmentRepository removedVehicleEquipmentRepository;
	
	
	
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
		parkingTotalsResponse.setRegistered(String.valueOf(parkingRepository.countByDateRangeAndRegistered(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atTime(LocalTime.MAX), regStatuses)));
		
		parkingTotalsResponse.setPaid(String.valueOf(parkingBillReceivableRepository.countByPayStatusAndDateRange(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atTime(LocalTime.MAX))));
		
		List<String> checkOutStatuses = new ArrayList<>();
		checkOutStatuses.add("CHECKED-OUT");
		parkingTotalsResponse.setCheckedOut(String.valueOf(parkingRepository.countByDateRangeAndCheckedOut(dateRange.getFrom().atStartOfDay(), dateRange.getTo().atTime(LocalTime.MAX), checkOutStatuses)));
		
		parkingTotalsResponse.setCurrentUnpaid(String.valueOf(parkingRepository.countRegistered()));
		parkingTotalsResponse.setCurrentTotalInYards(String.valueOf(parkingRepository.countRegistered()));
		
		
//		public long countPaidOrVerifiedBillsWithinDateRange(LocalDateTime startDate, LocalDateTime endDate) {
//	        return parkingBillReceivableRepository.countByStatusAndDateRange(startDate, endDate);
//	    }
		
		return ResponseEntity.ok().body(parkingTotalsResponse);
	}
	
	@PostMapping("/parking_reports/get_registration_report")
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
		
		List<Parking> parkings = new ArrayList<>();
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		statuses.add("CHECKED-OUT");
		if(user != null) {
			
			parkings = parkingRepository.findAllByCreatedByUserAndCreatedDateTimeBetweenAndStatusIn(
			        user, 
			        dateRange.getFrom().atStartOfDay(),
			        dateRange.getTo().atTime(LocalTime.MAX),
			        statuses
			    );		
					
		}else {
			parkings = parkingRepository.findAllByCreatedDateTimeBetweenAndStatusIn(
			        dateRange.getFrom().atStartOfDay(),
			        dateRange.getTo().atTime(LocalTime.MAX),
			        statuses
			    );	
		}
		
		List<RegistrationResponseDTO> registrationResponses = new ArrayList<>();
		int sn = 1;
		for(Parking parking : parkings) {
			RegistrationResponseDTO registrationResponse = new RegistrationResponseDTO();
			registrationResponse.setChassisNo(parking.getChasisNo());
			registrationResponse.setVehicleType(parking.getVehicleEquipmentType().getName());
			registrationResponse.setRegisteredDate(parking.getCreatedDateTime().toString());
			registrationResponse.setRegisteredBy(parking.getCreatedByUser().getNickname());
			registrationResponse.setKeyStatus(parking.isHasKeys() ? "YES" : "NO");
			registrationResponse.setSn(String.valueOf(sn));
			registrationResponses.add(registrationResponse);
			sn++;
		}
		return ResponseEntity.ok().body(registrationResponses);
		
	}
	
	@PostMapping("/parking_reports/get_parking_report")
	public ResponseEntity<List<ParkingResponseDTO>>getParkingReportByDateAndReceptionist(
			@RequestBody DateRange dateRange,
			@RequestParam(name = "nickname") String cashierName,
			@RequestParam(name = "payment_status") String paymentStatus,
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
		
		List<Parking> parkings = new ArrayList<>();
		List<String> statuses = new ArrayList<>();
		statuses.add("CHECKED-IN");
		statuses.add("CHECKED-OUT");
		if(user != null) {
			
			parkings = parkingRepository.findAllByCheckedInByUserAndCheckedInDateTimeBetweenAndStatusIn(
			        user, 
			        dateRange.getFrom().atStartOfDay(),
			        dateRange.getTo().atTime(LocalTime.MAX),
			        statuses
			    );		
					
		}else {
			parkings = parkingRepository.findAllByCheckedInDateTimeBetweenAndStatusIn(
			        dateRange.getFrom().atStartOfDay(),
			        dateRange.getTo().atTime(LocalTime.MAX),
			        statuses
			    );	
		}
		
		List<ParkingResponseDTO> parkingResponses = new ArrayList<>();
		int sn = 1;
		for(Parking parking : parkings) {
			ParkingResponseDTO parkingResponse = new ParkingResponseDTO();
			parkingResponse.setVehicleEquipmentCategory(parking.getVehicleEquipmentCategory());
			parkingResponse.setVehicleEquipmentTypeName(parking.getVehicleEquipmentType().getName());
			parkingResponse.setOwnerFirstName(parking.getOwnerFirstName());
			parkingResponse.setOwnerLastName(parking.getOwnerLastName());
			parkingResponse.setOwnerPhoneNo(parking.getOwnerPhoneNo());
			parkingResponse.setCardNo(parking.getCardNo());
			parkingResponse.setChasisNo(parking.getChasisNo());
			parkingResponse.setTformNumber(parking.getTformNumber());
			parkingResponse.setDeviceStatus(parking.isDeviceStatus() ? "YES" : "NO");
			parkingResponse.setKeyStatus(parking.isDeviceStatus() ? "YES" : "NO");
			parkingResponse.setBillingAmount(String.valueOf(parking.getBillingAmount()));
			parkingResponse.setCheckedInAt(
				    Optional.ofNullable(parking.getCheckedInDateTime())
				            .map(Object::toString)
				            .orElse("")
				);
				parkingResponse.setCheckedOutAt(
				    Optional.ofNullable(parking.getCheckedOutDateTime())
				            .map(Object::toString)
				            .orElse("")
				);
			parkingResponse.setSn(String.valueOf(sn));
			parkingResponse.setStatus(parking.getStatus());
			parkingResponse.setCreatedBy(parking.getCreatedByUser().getNickname());
			
			
			// Get pay status
			List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParking(parking);
			
			boolean inStartLimit = false;
			boolean inEndLimit = false;
			String payStatus = "Unpaid";
			parkingResponse.setPayStatus("Unpaid");
			double paidAmount = 0;
			for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
				if(parkingBillReceivable.getBillReceivable().getPayStatus().toString().equals("PAID")) {
					paidAmount = paidAmount + parkingBillReceivable.getBillReceivable().getPaid();
					if((dateRange.getFrom().atStartOfDay().isBefore(parkingBillReceivable.getEndedAt())) && dateRange.getTo().atTime(LocalTime.MAX).isAfter(parkingBillReceivable.getStartedAt())) {	
						inStartLimit = true;
						payStatus = "Partial";
					}
					if(parking.getStatus().equals("CHECKED-OUT")) {
						inEndLimit = true;
					}else {
						if(dateRange.getTo().atTime(LocalTime.MAX).isAfter(parkingBillReceivable.getStartedAt()) && dateRange.getTo().atTime(LocalTime.MAX).isBefore(parkingBillReceivable.getEndedAt())) {
							inEndLimit = true;
						}
					}
				}
			}			
			if(inStartLimit && inEndLimit) {
				payStatus = "Paid";
			}
			parkingResponse.setPayStatus(payStatus);
			parkingResponse.setPaidAmount(String.valueOf(paidAmount));
			
			if(paymentStatus.equals("") || paymentStatus.equals("--All--")) {
				parkingResponses.add(parkingResponse);
			}else {
				if(paymentStatus.equals(payStatus)){
					parkingResponses.add(parkingResponse);
				}
			}
			
			sn++;
			
		}
		return ResponseEntity.ok().body(parkingResponses);
		
	}
	
	
	@PostMapping("/parking_reports/get_vehicle_equipment_removed_report")
	public ResponseEntity<List<VehicleEquipmentRemovedResponseDTO>>getVehicleEquipmentRemovedReport(
			@RequestBody DateRange dateRange,
			HttpServletRequest request){
		
		List<RemovedVehicleEquipment> removedVehicleEquipments = removedVehicleEquipmentRepository.findAllByCreatedDateTimeBetween(dateRange.getFrom().atStartOfDay(),
		        dateRange.getTo().atTime(LocalTime.MAX));
		
		List<VehicleEquipmentRemovedResponseDTO> removedVehicleEquipmentResponses = new ArrayList<>();
		
		int sn = 1;
		for(RemovedVehicleEquipment removedVehicleEquipment : removedVehicleEquipments) {
			VehicleEquipmentRemovedResponseDTO removedVehicleEquipmentResponse = new VehicleEquipmentRemovedResponseDTO();
			removedVehicleEquipmentResponse.setSn(String.valueOf(sn));
			removedVehicleEquipmentResponse.setOwnerName(removedVehicleEquipment.getParking().getOwnerFirstName() + " " + removedVehicleEquipment.getParking().getOwnerLastName());
			if(removedVehicleEquipment.getParking().getOwnerPhoneNo() != null) {
				removedVehicleEquipmentResponse.setPhoneNo(removedVehicleEquipment.getParking().getOwnerPhoneNo());
			}else {
				removedVehicleEquipmentResponse.setPhoneNo("");
			}
			
			if(removedVehicleEquipment.getParking().getRegistrationNo() != null) {
				removedVehicleEquipmentResponse.setRegNo(removedVehicleEquipment.getParking().getRegistrationNo());
			}else {
				removedVehicleEquipmentResponse.setRegNo("");
			}
			
			if(removedVehicleEquipment.getParking().getVehicleEquipmentName() != null) {
				removedVehicleEquipmentResponse.setVehicleName(removedVehicleEquipment.getParking().getVehicleEquipmentName());
			}else {
				removedVehicleEquipmentResponse.setVehicleName("");
			}
			
			
			
			removedVehicleEquipmentResponse.setPrice(String.valueOf(removedVehicleEquipment.getParking().getBillingAmount()));
			removedVehicleEquipmentResponse.setReason(removedVehicleEquipment.getReason());
			removedVehicleEquipmentResponse.setDateTime(removedVehicleEquipment.getCreatedDateTime().toString());
			removedVehicleEquipmentResponse.setRegisteredBy(removedVehicleEquipment.getParking().getCheckedInByUser().getNickname());
			removedVehicleEquipmentResponse.setRemovedBy(removedVehicleEquipment.getCreatedByUser().getNickname());
			removedVehicleEquipmentResponses.add(removedVehicleEquipmentResponse);
			sn++;
		}
		return ResponseEntity.ok().body(removedVehicleEquipmentResponses);
		
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
class VehicleEquipmentRemovedResponseDTO{
	String sn;
	String ownerName;
	String phoneNo;
	String vehicleName;
	String price;
	String regNo;
	String reason;
	String dateTime;
	String registeredBy;
	String removedBy;
}
