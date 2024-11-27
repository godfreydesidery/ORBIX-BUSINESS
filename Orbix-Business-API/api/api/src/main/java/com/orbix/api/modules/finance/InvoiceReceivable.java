package com.orbix.api.modules.finance;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "invoice_receivables")
public class InvoiceReceivable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true, nullable = false)
	private String no;
	@Column(nullable = false)
	private String status = "UNPAID";
	
	private String summary = "";
	
	// Add created by, approved by etc before using
		
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = Branch.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "branch_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Branch branch;

	@OneToMany(targetEntity = InvoiceReceivableDetail.class, mappedBy = "invoiceReceivable", fetch = FetchType.EAGER, orphanRemoval = true)
    @Valid
    @JsonIgnoreProperties("invoiceReceivable")
	@Fetch(value = FetchMode.SUBSELECT)
    private List<InvoiceReceivableDetail> invoiceReceivableDetails;
}
