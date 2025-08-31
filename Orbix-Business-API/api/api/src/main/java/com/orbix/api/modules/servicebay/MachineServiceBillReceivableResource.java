package com.orbix.api.modules.servicebay;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.identityandaccess.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class MachineServiceBillReceivableResource {
	private final MachineServiceBillReceivableRepository machineServiceBillReceivableRepository;
	private final MachineRepository machineRepository;
	private final DayService dayService;
	private final BillReceivableRepository billReceivableRepository;
	private final UserService userService;
	
	private final MachineServiceBillReceivableService machineServiceBillReceivableService;
	
	@GetMapping("/machine_service_bill_receivables/get_all_by_machine")
	public ResponseEntity<List<MachineServiceBillReceivableResponseDTO>> getAllByMachine(
			@RequestParam(name = "machine_id") Long machineId,
			HttpServletRequest request)
			{			
		return ResponseEntity.ok().body(machineServiceBillReceivableService.getAllByMachine(machineId, request));
	}
}
