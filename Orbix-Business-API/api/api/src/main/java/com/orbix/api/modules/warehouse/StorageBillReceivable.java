package com.orbix.api.modules.warehouse;

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

import com.orbix.api.api.vehicleandequipmentparking.Parking;
import com.orbix.api.api.vehicleandequipmentparking.ParkingBillReceivable;
import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.identityandaccess.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "storage_bill_receivables")
public class StorageBillReceivable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDateTime startedAt;
	private LocalDateTime endedAt;
	
	private String billingType = "";
	private double noOfDays = 1;
	@Column(nullable = false)
	private double qty;
	@Column(nullable = false)
	private double price;
	
	private double discount = 0;
	private String discountStatus;
	
	@ManyToOne(targetEntity = Storage.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "storage_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Storage storage;
	
	@ManyToOne(targetEntity = BillReceivable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "bill_receivable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private BillReceivable billReceivable;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "discount_approved_by_user_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User discountApprovedByUser;
		
	private LocalDateTime discountApprovedDateTime;
}
