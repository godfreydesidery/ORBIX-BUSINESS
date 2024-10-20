package com.orbix.api.modules.finance;

import java.time.LocalDateTime;

import javax.persistence.Column;
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

import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "invoice_receivable_details")
public class InvoiceReceivableDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	double amount;
	@Column(nullable = false)
	double paid;
	@Column(nullable = false)
	double due;
	@Column(nullable = false)
	private String status = "UNPAID";
	
	private String summary = "";
		
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = InvoiceReceivable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "invoice_receivable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private InvoiceReceivable invoiceReceivable;
	
	@ManyToOne(targetEntity =BillReceivable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "bill_receivable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private BillReceivable billReceivable;
}
