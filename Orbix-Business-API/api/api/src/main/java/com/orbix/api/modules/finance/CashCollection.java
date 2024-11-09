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
import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "cash_collections")
public class CashCollection {	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private double amount;
	
	String paymentType = "CASH";
	String reason;
	
	LocalDateTime collectionDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = BillReceivable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "bill_receivable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private BillReceivable billReceivable;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "collected_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private User collectedByUser;
}
