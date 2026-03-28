package com.orbix.api.modules.bond;

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

import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.ParkingInvoiceReceivable;
import com.orbix.api.modules.finance.InvoiceReceivable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "bond_item_invoice_receivables")
public class BondItemInvoiceReceivable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(targetEntity = BondItem.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "bond_item_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private BondItem bondItem;
	
	@ManyToOne(targetEntity = InvoiceReceivable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "invoice_receivable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private InvoiceReceivable invoiceReceivable;
}
