package com.orbix.api.api.vehicleandequipmentparking;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.finance.InvoiceReceivable;
import com.orbix.api.modules.finance.InvoiceReceivableDetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "parking_invoice_receivables")
public class ParkingInvoiceReceivable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(targetEntity = Parking.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "parking_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Parking parking;
	
	@ManyToOne(targetEntity = InvoiceReceivable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "invoice_receivable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private InvoiceReceivable invoiceReceivable;	
}
