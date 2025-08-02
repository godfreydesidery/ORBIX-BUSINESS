package com.orbix.api.modules.weighbridge;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivable;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.warehouse.StorageBillReceivableResponseDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class WeighBillReceivableResource {
	
	private final WeighBillReceivableRepository weighBillReceivableRepository;
	private final WeighRepository weighRepository;
	private final DayService dayService;
	private final BillReceivableRepository billReceivableRepository;
	private final UserService userService;
	
	private final WeighBillReceivableService weighBillReceivableService;
	
	@GetMapping("/weigh_bill_receivables/get_all_by_weigh")
	public ResponseEntity<List<WeighBillReceivableResponseDTO>> getAllByWeigh(
			@RequestParam(name = "weigh_id") Long weighId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(weighBillReceivableService.getAllByWeigh(weighId, request));
	}
	
	@GetMapping("/weigh_bills/get_by_weigh_id")
	public ResponseEntity<List<WeighBillReceivableResponseDTO>>getAllByWeighId(@RequestParam(name = "weigh_id") Long weighId, HttpServletRequest request){
		
		Optional<Weigh> weigh_ = weighRepository.findById(weighId);
		if(weigh_.isEmpty()) {
			throw new NotFoundException("Weigh not found");
		}
		
		List<WeighBillReceivable> bills = weighBillReceivableRepository.findByWeigh(weigh_.get());
		
		List<WeighBillReceivableResponseDTO> list = new ArrayList<>();
		
		for(WeighBillReceivable bill : bills) {
			list.add(toDto(bill));
		}
		
		return ResponseEntity.ok().body(list);
	}
	
	
	
	@PostMapping("/weigh_bills/add_bill")
	public void addBill(@RequestBody Bill b, HttpServletRequest request) {
		
		Optional<Weigh> weigh_ = weighRepository.findById(b.getWeighId());
		if(weigh_.isEmpty()) {
			throw new NotFoundException("Weigh not found");
		}
		
		BillReceivable billReceivable = new BillReceivable();
		billReceivable.setNo(String.valueOf(Math.random()));
		billReceivable.setAmount(b.getAmount());
		billReceivable.setPaid(0);
		billReceivable.setDue(b.getAmount());
		billReceivable.setBranch(weigh_.get().getBranch());
		billReceivable.setCreatedDateTime(dayService.getTimeStamp());
		
		billReceivable.setPayStatus(PayStatus.UNPAID);
		billReceivable.setSummary(b.getDescription());
		
		billReceivable = billReceivableRepository.save(billReceivable);
		billReceivable.setNo("BR" + billReceivable.getId().toString());
		billReceivable = billReceivableRepository.save(billReceivable);
		
		WeighBillReceivable weighBillReceivable = new WeighBillReceivable();	
		
		weighBillReceivable.setPrice(b.getAmount());
		weighBillReceivable.setQty(1);
		weighBillReceivable.setDescription(b.getDescription());
		weighBillReceivable.setDiscount(0);
		weighBillReceivable.setBillReceivable(billReceivable);
		weighBillReceivable.setWeigh(weigh_.get());
		weighBillReceivable.setWeightOne(b.getWeightOne());
		weighBillReceivable.setWeightTwo(b.getWeightTwo());
		weighBillReceivable.setWeightThree(b.getWeightThree());
		weighBillReceivable.setWeightFour(b.getWeightFour());
		weighBillReceivable.setWeighStatus(b.getWeighStatus());
		weighBillReceivable.setCreatedByUser(userService.getUser(request));
		
		weighBillReceivable = weighBillReceivableRepository.save(weighBillReceivable);
	
	}
	
	@PostMapping("/weigh_bills/remove")
	public void addBill(@RequestBody RemoveBill removeBill, HttpServletRequest request) {
		
		Optional<Weigh> weigh_ = weighRepository.findById(removeBill.getWeighId());
		if(weigh_.isEmpty()) {
			throw new NotFoundException("Weigh not found");
		}
		
		if(!removeBill.getWeighNo().equals(weigh_.get().getNo())) {
			throw new InvalidOperationException("Could not remove");
		}
		
		Optional<WeighBillReceivable> wbr_ = weighBillReceivableRepository.findById(removeBill.getBillId());
		if(wbr_.isEmpty()) {
			throw new NotFoundException("Bill not found");
		}
		
		BillReceivable br = wbr_.get().getBillReceivable();
		if(!br.getPayStatus().equals(PayStatus.UNPAID)) {
			throw new InvalidOperationException("Could not remove, not in unpaid status");
		}
		
		if(br.getCreatedDateTime().isBefore(LocalDateTime.now().minusHours(24))) {
			throw new InvalidOperationException("Could not remove, already past 24 hrs");
		}
		
		User user = wbr_.get().getCreatedByUser();
		if(user == null) {
			throw new InvalidOperationException("Created user is not available");
		}
		
		Long requestUserId = userService.getUser(request).getId();
		if(user.getId() != requestUserId) {
			throw new InvalidOperationException("Bill can not be removed by a different user");
		}
		
		// Tobe removed in future
		weighBillReceivableRepository.delete(wbr_.get());
		billReceivableRepository.delete(br);
	}
	
	private WeighBillReceivableResponseDTO toDto(WeighBillReceivable bill) {
		WeighBillReceivableResponseDTO response = new WeighBillReceivableResponseDTO();
		
		response.setId(bill.getId().toString());
		response.setWeighId(bill.getWeigh().getId().toString());
		response.setDescription(bill.getDescription());
		response.setAmount(String.valueOf(bill.getPrice()));
		response.setWeightOne(String.valueOf(bill.getWeightOne()));
		response.setWeightTwo(String.valueOf(bill.getWeightTwo()));
		response.setWeightThree(String.valueOf(bill.getWeightThree()));
		response.setWeightFour(String.valueOf(bill.getWeightFour()));
		response.setWeighStatus(String.valueOf(bill.getWeighStatus()));
		if(bill.getWeighStatus() != null) {
			response.setWeighStatus(bill.getWeighStatus());
		}else {
			response.setWeighStatus("");
		}
		response.setQty("1");
		response.setPayStatus(bill.getBillReceivable().getPayStatus().toString());
		
		if(bill.getCreatedByUser() != null) {
			response.setCreatedBy(userService.getNicknameByUserId(bill.getCreatedByUser().getId()));
		}else {
			response.setCreatedBy("");
		}
		
		return response;
	}
}

@Data
class Bill{
	Long weighId;
	String description;
	double amount;
	double weightOne;
	double weightTwo;
	double weightThree;
	double weightFour;
	String weighStatus;
	int recheck;
}

@Data
class RemoveBill{
	// Very sensitive
	Long weighId;
	String weighNo;
	Long billId;
}
