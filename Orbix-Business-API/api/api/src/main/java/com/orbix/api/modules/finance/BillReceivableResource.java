package com.orbix.api.modules.finance;

import java.util.List;

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

import com.orbix.api.api.commons.PayCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orbix-business-api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Transactional
public class BillReceivableResource {
	
	private final BillReceivableService billReceivableService;
	
	@PostMapping("/bill_receivables/confirm_bills_payment")
	//@PreAuthorize("hasAnyAuthority('BILL-A')")
	public ResponseEntity<List<BillReceivableResponseDTO>> confirmBillPayment(
			@RequestBody List<BillReceivableRequestDTO> billReceivableRequests,
			@RequestParam(name = "total_amount") double totalAmount,
			@RequestParam(name = "pay_code") PayCode payCode,
			@RequestParam(name = "pay_ref_no") String payRefNo,
			HttpServletRequest request){		
		return ResponseEntity.ok().body(billReceivableService.confirmBillPayment(billReceivableRequests, payCode, payRefNo, totalAmount, request));		
	}
	
	
	@GetMapping("/bill_receivables/get_all_by_parking")
	public ResponseEntity<List<BillReceivableResponseDTO>>getAllByParking(
			@RequestParam(name = "parking_id") Long parkingId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(billReceivableService.getAllByParking(parkingId, request));
	}
	
	@GetMapping("/bill_receivables/get_all_by_maintenance")
	public ResponseEntity<List<BillReceivableResponseDTO>>getAllByMaintenance(
			@RequestParam(name = "maintenance_id") Long maintenanceId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(billReceivableService.getAllByMaintenance(maintenanceId, request));
	}
	
	@GetMapping("/bill_receivables/get_all_by_storage")
	public ResponseEntity<List<BillReceivableResponseDTO>>getAllByStorage(
			@RequestParam(name = "storage_id") Long storageId,
			HttpServletRequest request){
		return ResponseEntity.ok().body(billReceivableService.getAllByStorage(storageId, request));
	}
	
	
	
	
//	
//	@PostMapping("/bills/confirm_bills_payment")
//	//@PreAuthorize("hasAnyAuthority('BILL-A')")
//	public ResponseEntity<PatientBill> confirmBillsPayment(
//			@RequestBody List<PatientBill> bills,
//			@RequestParam(name = "total_amount") double totalAmount,
//			HttpServletRequest request){
//		
//		double amount = 0;
//		PatientPayment payment = new PatientPayment();
//		payment.setAmount(totalAmount);
//		
//		payment.setCreatedBy(userService.getUser(request).getId());
//		payment.setCreatedOn(dayService.getDay().getId());
//		payment.setCreatedAt(dayService.getTimeStamp());
//		payment.setStatus("RECEIVED");
//		
//		payment = patientPaymentRepository.save(payment);
//		
//		boolean isPharmacySaleOrder = false;
//		PharmacySaleOrder pharmacySaleOrder = new PharmacySaleOrder();
//		
//		for(PatientBill bill : bills) {
//			Optional<PatientBill> b = patientBillRepository.findById(bill.getId());
//			if(!b.isPresent()) {
//				throw new NotFoundException("Bill not found; Bill ID :"+bill.getId().toString());
//			}
//			if(!(b.get().getStatus().equals("UNPAID") || b.get().getStatus().equals("VERIFIED"))) {
//				throw new InvalidOperationException("One or more bills have been paid/covered/canceled. Only unpaid or verified bills can be paid");
//			}
//			
//			Optional<PharmacySaleOrderDetail> detail_ = pharmacySaleOrderDetailRepository.findByPatientBill(bill);
//			if(detail_.isPresent() && isPharmacySaleOrder == false) {
//				isPharmacySaleOrder = true;
//				pharmacySaleOrder = detail_.get().getPharmacySaleOrder();
//			}
//			if(b.get().getStatus().equals("UNPAID") || b.get().getStatus().equals("VERIFIED")) {
//				b.get().setBalance(0);
//				b.get().setPaid(b.get().getAmount());
//				b.get().setStatus("PAID");
//				
//				patientBillRepository.save(b.get());
//				PatientPaymentDetail pd = new PatientPaymentDetail();
//				pd.setPatientBill(bill);
//				pd.setPatientPayment(payment);
//				pd.setDescription(b.get().getDescription());
//				pd.setStatus("RECEIVED");
//				
//				pd.setCreatedBy(userService.getUser(request).getId());
//				pd.setCreatedOn(dayService.getDay().getId());
//				pd.setCreatedAt(dayService.getTimeStamp());
//				
//				patientPaymentDetailRepository.save(pd);
//				
//				if(bill.getBillItem() == null) {
//					b.get().setBillItem("NA");
//					//b.get() = patientBillRepository.save(b.get());
//				}
//				
//				Collection collection = new Collection();
//				collection.setPatientBill(bill);
//				collection.setAmount(b.get().getAmount());
//				collection.setItemName(b.get().getBillItem());
//				collection.setPaymentChannel("Cash");
//				collection.setPaymentReferenceNo("NA");
//				collection.setPatient(b.get().getPatient());
//				collection.setCreatedBy(userService.getUser(request).getId());
//				collection.setCreatedOn(dayService.getDay().getId());
//				collection.setCreatedAt(dayService.getTimeStamp());
//				collectionRepository.save(collection);
//				
//				amount = amount + b.get().getAmount();
//				
//				List<PatientInvoiceDetail> invds = patientInvoiceDetailRepository.findAllByPatientBill(b.get());
//				if(!invds.isEmpty()) {
//					for(PatientInvoiceDetail invd : invds) {
//						PatientInvoice invoice = invd.getPatientInvoice();
//						invd.setStatus("PAID");
//						patientInvoiceDetailRepository.save(invd);
//						invoice.setAmountPaid(invoice.getAmountPaid() + invd.getAmount());
//						patientInvoiceRepository.save(invoice);
//					}
//				}
//				
//				List<Admission> adms = admissionRepository.findAllByPatientAndStatus(b.get().getPatient(), "PENDING");
//				List<Consultation> cons = consultationRepository.findAllByPatientAndStatus(b.get().getPatient(), "IN-PROCESS");
//				if(!adms.isEmpty()) {
//					for(Admission adm : adms) {
//						adm.setStatus("IN-PROCESS");
//						adm = admissionRepository.save(adm);
//						adm.getWardBed().setStatus("OCCUPIED");
//						wardBedRepository.save(adm.getWardBed());
//					}
//					for(Consultation con : cons) {
//						con.setStatus("SIGNED-OUT");
//						consultationRepository.save(con);
//					}
//				}
//			}
//		}
//		
//		if(isPharmacySaleOrder == true) {
//			pharmacySaleOrder.setStatus("APPROVED");
//			pharmacySaleOrder.setApprovedBy(userService.getUser(request).getId());
//			pharmacySaleOrder.setApprovedOn(dayService.getDay().getId());
//			pharmacySaleOrder.setApprovedAt(dayService.getTimeStamp());
//			
//			pharmacySaleOrder = pharmacySaleOrderRepository.save(pharmacySaleOrder);
//			
//			for(PharmacySaleOrderDetail det : pharmacySaleOrder.getPharmacySaleOrderDetails()) {
//				det.setPayStatus("PAID");
//				
//				det.setSoldBy(userService.getUser(request).getId());
//				det.setSoldOn(dayService.getDay().getId());
//				det.setSoldAt(dayService.getTimeStamp());
//				
//				pharmacySaleOrderDetailRepository.save(det);
//			}
//			
//		}
//		
//		if(amount != totalAmount) {
//			throw new InvalidOperationException("Could not confirm payment. Insufficient payment/ amount mismatch");
//		}		
//		return ResponseEntity.ok().body(null);
//	}
//	
	
	
}
