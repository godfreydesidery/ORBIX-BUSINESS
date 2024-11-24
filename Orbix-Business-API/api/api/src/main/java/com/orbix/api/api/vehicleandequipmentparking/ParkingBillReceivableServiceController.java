package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ParkingBillReceivableServiceController implements ParkingBillReceivableService {
	
	private final UserService userService;
	
	private final ParkingBillReceivableRepository parkingBillReceivableRepository;
	private final ParkingServiceBillReceivableRepository parkingServiceBillReceivableRepository;
	private final ParkingRepository parkingRepository;
	private final BillReceivableRepository billReceivableRepository;
	private final DayService dayService;

	@Override
	public List<ParkingBillReceivableResponseDTO> getAllByParking(Long parkingId, HttpServletRequest request) {
		Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new NotFoundException("Parking with ID " + parkingId + " not found."));
		
		List<ParkingBillReceivable> parkingBillReceivables = parkingBillReceivableRepository.findAllByParking(parking);
		
		List<ParkingBillReceivableResponseDTO> parkingBillReceivableResponses = new ArrayList<>();
		for(ParkingBillReceivable parkingBillReceivable : parkingBillReceivables) {
			parkingBillReceivableResponses.add(parkingBillReceivableDTOMapper(parkingBillReceivable));
		}
		
		return parkingBillReceivableResponses;
	}
	
	@Override
	public List<ParkingServiceBillReceivableResponseDTO> getAllServiceByParking(Long parkingId, HttpServletRequest request) {
		Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new NotFoundException("Parking with ID " + parkingId + " not found."));
		
		List<ParkingServiceBillReceivable> parkingServiceBillReceivables = parkingServiceBillReceivableRepository.findAllByParking(parking);
		
		List<ParkingServiceBillReceivableResponseDTO> parkingServiceBillReceivableResponses = new ArrayList<>();
		for(ParkingServiceBillReceivable parkingServiceBillReceivable : parkingServiceBillReceivables) {
			parkingServiceBillReceivableResponses.add(parkingServiceBillReceivableDTOMapper(parkingServiceBillReceivable));
		}
		
		return parkingServiceBillReceivableResponses;
	}
	
	@Override
	public ParkingBillReceivableResponseDTO getParkingBillReceivable(Long id, HttpServletRequest request) {
		ParkingBillReceivable parkingBillReceivable = parkingBillReceivableRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Parking bill with ID " + id + " not found."));
		
		return parkingBillReceivableDTOMapper(parkingBillReceivable);
	}
	
	@Override
	public ParkingServiceBillReceivableResponseDTO getServiceBillReceivable(Long id, HttpServletRequest request) {
		ParkingServiceBillReceivable parkingServiceBillReceivable = parkingServiceBillReceivableRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Parking Service with ID " + id + " not found."));
		
		return parkingServiceBillReceivableDTOMapper(parkingServiceBillReceivable);
	}
	
	@Override
	public ParkingBillReceivableResponseDTO createParkingBillReceivable(
			ParkingBillReceivableRequestDTO parkingBillReceivableRequest,
			HttpServletRequest request) {
		
		Parking parking = parkingRepository.findById(parkingBillReceivableRequest.getParkingId())
                .orElseThrow(() -> new NotFoundException("Parking not found."));
		
		// if(!validateParkingBill(parkingBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		// Check if is first bill
		
		List<ParkingBillReceivable> rcvs = parkingBillReceivableRepository.findAllByParking(parking);
		
		LocalDateTime fromDate = null;
		LocalDateTime toDate = null;
		double qty = 0;
		
		if(parkingBillReceivableRequest.getEndedAt() != null) {
			String dateString = parkingBillReceivableRequest.getEndedAt() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			toDate = LocalDateTime.parse(dateString, formatter).plusDays(1).toLocalDate().atStartOfDay();
		}
		
		if(rcvs.isEmpty()) {			
			// Check for first billing date		
			fromDate = parking.getStartBillingAt().toLocalDate().atStartOfDay();
			
			if(toDate == null) toDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();	
			
			if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is before bill starting date");
			
			long dayCount = ChronoUnit.DAYS.between(fromDate, toDate);
			
			qty = dayCount;
			
		}else {
			// Take the last bill
			fromDate = rcvs.get(rcvs.size() - 1).getEndedAt().plusDays(1).toLocalDate().atStartOfDay();
			
			if(toDate == null) toDate = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
			
			if(!toDate.isAfter(fromDate)) throw new InvalidOperationException("Current date is invalid" + toDate.toString() + fromDate.toString());
			
			long dayCount = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
			
			qty = dayCount;
			
		}
		
		if(qty > 1) qty = qty - 1;
			
		BillReceivable billReceivable = new BillReceivable();
		billReceivable.setNo(String.valueOf(Math.random()));
		billReceivable.setAmount((parking.getBillingAmount() * qty) - parkingBillReceivableRequest.getDiscount());
		billReceivable.setPaid(0);
		billReceivable.setDue((parking.getBillingAmount() * qty) - parkingBillReceivableRequest.getDiscount());
		billReceivable.setBranch(parking.getBranch());
		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
		
		billReceivable.setPayStatus(PayStatus.UNPAID);
		billReceivable.setSummary("Parking bill for parking#: " + parking.getNo());
		
		billReceivable = billReceivableRepository.save(billReceivable);
		billReceivable.setNo("BR" + billReceivable.getId().toString());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		ParkingBillReceivable parkingBillReceivable = new ParkingBillReceivable();
		
		parkingBillReceivable.setStartedAt(fromDate);
		parkingBillReceivable.setEndedAt(toDate);
		
//		if(parkingBillReceivableRequest.getStartedAt() == null) {
//			parkingBillReceivable.setStartedAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this depending on user billing preferences
//		}else {
//			//String dateString = "2024-10-26 15:30:45" ;
//			String dateString = parkingBillReceivableRequest.getStartedAt() + " 00:00:00";
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
//			parkingBillReceivable.setStartedAt(dateTime);
//		}
		
//		if(parkingBillReceivableRequest.getEndedAt() == null) {
//			parkingBillReceivable.setEndedAt(dayService.getTimeStamp().toLocalDate().atStartOfDay()); // You can change this depending on user billing preferences
//		}else {
//			//String dateString = "2024-10-26 15:30:45" ;
//			String dateString = parkingBillReceivableRequest.getEndedAt() + " 00:00:00";
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
//			parkingBillReceivable.setEndedAt(dateTime);
//		}
		
		parkingBillReceivable.setPrice(parking.getBillingAmount());
		parkingBillReceivable.setQty(qty);
		parkingBillReceivable.setDiscount(parkingBillReceivableRequest.getDiscount());
		parkingBillReceivable.setBillReceivable(billReceivable);
		parkingBillReceivable.setParking(parking);
		
		parkingBillReceivable = parkingBillReceivableRepository.save(parkingBillReceivable);

		return parkingBillReceivableDTOMapper(parkingBillReceivable);
	}
	
	@Override
	public ParkingBillReceivableResponseDTO updateParkingBillReceivable(
			ParkingBillReceivableRequestDTO parkingBillReceivableRequest,
			HttpServletRequest request) {
		
		ParkingBillReceivable parkingBillReceivable = parkingBillReceivableRepository.findById(parkingBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
		
		if(!validateParkingBill(parkingBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		BillReceivable billReceivable = parkingBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be edited");
		
		billReceivable.setAmount(parkingBillReceivableRequest.getPrice() * parkingBillReceivableRequest.getQty() - parkingBillReceivableRequest.getDiscount());
		billReceivable.setDue(parkingBillReceivableRequest.getPrice() * parkingBillReceivableRequest.getQty() - parkingBillReceivableRequest.getDiscount());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		parkingBillReceivable.setPrice(parkingBillReceivableRequest.getPrice());
		parkingBillReceivable.setQty(parkingBillReceivableRequest.getQty());
		parkingBillReceivable.setDiscount(parkingBillReceivableRequest.getDiscount());
		parkingBillReceivable = parkingBillReceivableRepository.save(parkingBillReceivable);
		
		return parkingBillReceivableDTOMapper(parkingBillReceivable);
	}
	
	@Override
	public ParkingServiceBillReceivableResponseDTO createServiceBillReceivable(
			ParkingServiceBillReceivableRequestDTO parkingServiceBillReceivableRequest,
			HttpServletRequest request) {
		
		Parking parking = parkingRepository.findById(parkingServiceBillReceivableRequest.getParkingId())
                .orElseThrow(() -> new NotFoundException("Parking not found."));
		
		if(!validateServiceBill(parkingServiceBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		BillReceivable billReceivable = new BillReceivable();
		billReceivable.setNo(String.valueOf(Math.random()));
		billReceivable.setAmount(parkingServiceBillReceivableRequest.getPrice() * parkingServiceBillReceivableRequest.getQty() - parkingServiceBillReceivableRequest.getDiscount());
		billReceivable.setPaid(0);
		billReceivable.setDue(parkingServiceBillReceivableRequest.getPrice() * parkingServiceBillReceivableRequest.getQty() - parkingServiceBillReceivableRequest.getDiscount());
		billReceivable.setBranch(parking.getBranch());
		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
		
		billReceivable.setPayStatus(PayStatus.UNPAID);
		billReceivable.setSummary("Service bill for parking#: " + parking.getNo());
		
		billReceivable = billReceivableRepository.save(billReceivable);
		billReceivable.setNo("BR" + billReceivable.getId().toString());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		
		
		
		ParkingServiceBillReceivable parkingServiceBillReceivable = new ParkingServiceBillReceivable();
		parkingServiceBillReceivable.setDescription(parkingServiceBillReceivableRequest.getDescription());
		
		
		if(parkingServiceBillReceivableRequest.getServiceDate() == null) {
			parkingServiceBillReceivable.setServiceDate(dayService.getTimeStamp()); // You can change this depending on user billing preferences
		}else {
			//String dateString = "2024-10-26 15:30:45" ;
			String dateString = parkingServiceBillReceivableRequest.getServiceDate() + " 00:00:00";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
			parkingServiceBillReceivable.setServiceDate(dateTime);
		}	
		parkingServiceBillReceivable.setPrice(parkingServiceBillReceivableRequest.getPrice());
		parkingServiceBillReceivable.setQty(parkingServiceBillReceivableRequest.getQty());
		parkingServiceBillReceivable.setDiscount(parkingServiceBillReceivableRequest.getDiscount());
		parkingServiceBillReceivable.setBillReceivable(billReceivable);
		parkingServiceBillReceivable.setParking(parking);
		
		parkingServiceBillReceivable = parkingServiceBillReceivableRepository.save(parkingServiceBillReceivable);

		return parkingServiceBillReceivableDTOMapper(parkingServiceBillReceivable);
	}
	
	@Override
	public ParkingServiceBillReceivableResponseDTO updateServiceBillReceivable(
			ParkingServiceBillReceivableRequestDTO parkingServiceBillReceivableRequest,
			HttpServletRequest request) {
		
		ParkingServiceBillReceivable parkingServiceBillReceivable = parkingServiceBillReceivableRepository.findById(parkingServiceBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
		
		if(!validateServiceBill(parkingServiceBillReceivableRequest)) throw new InvalidOperationException("Invalid entries");
		
		BillReceivable billReceivable = parkingServiceBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be edited");
		
		billReceivable.setAmount(parkingServiceBillReceivableRequest.getPrice() * parkingServiceBillReceivableRequest.getQty() - parkingServiceBillReceivableRequest.getDiscount());
		billReceivable.setDue(parkingServiceBillReceivableRequest.getPrice() * parkingServiceBillReceivableRequest.getQty() - parkingServiceBillReceivableRequest.getDiscount());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		parkingServiceBillReceivable.setDescription(parkingServiceBillReceivableRequest.getDescription());
		parkingServiceBillReceivable.setPrice(parkingServiceBillReceivableRequest.getPrice());
		parkingServiceBillReceivable.setQty(parkingServiceBillReceivableRequest.getQty());
		parkingServiceBillReceivable.setDiscount(parkingServiceBillReceivableRequest.getDiscount());
		parkingServiceBillReceivable = parkingServiceBillReceivableRepository.save(parkingServiceBillReceivable);
		
		return parkingServiceBillReceivableDTOMapper(parkingServiceBillReceivable);
	}
	
	@Override
	public boolean deleteServiceBillReceivable(
			ParkingServiceBillReceivableRequestDTO parkingServiceBillReceivableRequest,
			HttpServletRequest request) {
		
		ParkingServiceBillReceivable parkingServiceBillReceivable = parkingServiceBillReceivableRepository.findById(parkingServiceBillReceivableRequest.getId())
                .orElseThrow(() -> new NotFoundException("Bill not found."));
				
		BillReceivable billReceivable = parkingServiceBillReceivable.getBillReceivable();
		
		if(!billReceivable.getPayStatus().equals(PayStatus.UNPAID)) throw new InvalidOperationException("Only unpaid bill can be deleted");
		
		parkingServiceBillReceivableRepository.delete(parkingServiceBillReceivable);
		
		billReceivableRepository.delete(billReceivable);

		return true;
	}
	
	private ParkingBillReceivableResponseDTO parkingBillReceivableDTOMapper(ParkingBillReceivable parkingBillReceivable) {
		
		ParkingBillReceivableResponseDTO parkingBillReceivableResponseDTO = new ParkingBillReceivableResponseDTO();
		
		parkingBillReceivableResponseDTO.setId(parkingBillReceivable.getId().toString());
		parkingBillReceivableResponseDTO.setDescription("Parking bill " + parkingBillReceivable.getStartedAt().toString() + " to "  + parkingBillReceivable.getEndedAt().toString());
		parkingBillReceivableResponseDTO.setPrice(String.valueOf(parkingBillReceivable.getPrice()));
		parkingBillReceivableResponseDTO.setQty(String.valueOf(parkingBillReceivable.getQty()));
		parkingBillReceivableResponseDTO.setStartedAt(String.valueOf(parkingBillReceivable.getStartedAt()));
		parkingBillReceivableResponseDTO.setEndedAt(String.valueOf(parkingBillReceivable.getEndedAt()));
		parkingBillReceivableResponseDTO.setPayStatus(parkingBillReceivable.getBillReceivable().getPayStatus().toString());
		parkingBillReceivableResponseDTO.setParkingId(String.valueOf(parkingBillReceivable.getParking().getId()));
		parkingBillReceivableResponseDTO.setDiscount(String.valueOf(parkingBillReceivable.getDiscount()));
		parkingBillReceivableResponseDTO.setAmount(String.valueOf(parkingBillReceivable.getBillReceivable().getAmount()));
		
		return parkingBillReceivableResponseDTO;
		
	}
	
	private ParkingServiceBillReceivableResponseDTO parkingServiceBillReceivableDTOMapper(ParkingServiceBillReceivable parkingServiceBillReceivable) {
		
		ParkingServiceBillReceivableResponseDTO parkingServiceBillReceivableResponseDTO = new ParkingServiceBillReceivableResponseDTO();
		
		parkingServiceBillReceivableResponseDTO.setId(parkingServiceBillReceivable.getId().toString());
		parkingServiceBillReceivableResponseDTO.setPrice(String.valueOf(parkingServiceBillReceivable.getPrice()));
		parkingServiceBillReceivableResponseDTO.setQty(String.valueOf(parkingServiceBillReceivable.getQty()));
		if (parkingServiceBillReceivable.getServiceDate() != null) {
		    parkingServiceBillReceivableResponseDTO.setServiceDate(parkingServiceBillReceivable.getServiceDate().toString());
		} else {
		    parkingServiceBillReceivableResponseDTO.setServiceDate(""); // or provide a default value
		}
		parkingServiceBillReceivableResponseDTO.setDescription(String.valueOf(parkingServiceBillReceivable.getDescription()));
		parkingServiceBillReceivableResponseDTO.setPayStatus(parkingServiceBillReceivable.getBillReceivable().getPayStatus().toString());
		parkingServiceBillReceivableResponseDTO.setParkingId(String.valueOf(parkingServiceBillReceivable.getParking().getId()));
		parkingServiceBillReceivableResponseDTO.setDiscount(String.valueOf(parkingServiceBillReceivable.getDiscount()));
		parkingServiceBillReceivableResponseDTO.setAmount(String.valueOf(parkingServiceBillReceivable.getBillReceivable().getAmount()));
		
		
		return parkingServiceBillReceivableResponseDTO;
		
	}
	
	private boolean validateServiceBill(ParkingServiceBillReceivableRequestDTO parkingServiceBillReceivableRequest) {
		boolean valid = true;
		
		if(parkingServiceBillReceivableRequest.getPrice() < 0) throw new InvalidEntryException("Price must not be negative");
		if(parkingServiceBillReceivableRequest.getQty() <= 0) throw new InvalidEntryException("Qty must not be negative");
		if(parkingServiceBillReceivableRequest.getDiscount() < 0) throw new InvalidEntryException("Discount must not be negative");
		if((parkingServiceBillReceivableRequest.getQty() * parkingServiceBillReceivableRequest.getPrice() - parkingServiceBillReceivableRequest.getDiscount()) < 0) throw new InvalidEntryException("Discount must not exceed total amount");
		
		return valid;		
	}
	
	private boolean validateParkingBill(ParkingBillReceivableRequestDTO parkingBillReceivableRequest) {
		boolean valid = true;
		
		if(parkingBillReceivableRequest.getPrice() < 0) throw new InvalidEntryException("Price must not be negative");
		if(parkingBillReceivableRequest.getQty() <= 0) throw new InvalidEntryException("Qty must not be negative");
		if(parkingBillReceivableRequest.getDiscount() < 0) throw new InvalidEntryException("Discount must not be negative");
		if((parkingBillReceivableRequest.getQty() * parkingBillReceivableRequest.getPrice() - parkingBillReceivableRequest.getDiscount()) < 0) throw new InvalidEntryException("Discount must not exceed total amount");
		
		return valid;		
	}


	

}
