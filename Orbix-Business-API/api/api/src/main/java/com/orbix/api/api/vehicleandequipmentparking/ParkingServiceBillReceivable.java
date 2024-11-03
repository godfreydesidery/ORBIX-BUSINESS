package com.orbix.api.api.vehicleandequipmentparking;

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

import com.orbix.api.modules.finance.BillReceivable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "parking_service_bill_receivables")
public class ParkingServiceBillReceivable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String description;
	
	private LocalDateTime serviceDate = LocalDateTime.now();
	
	private String billingType = "";
	@Column(nullable = false)
	private double qty;
	@Column(nullable = false)
	private double price;
	
	private double discount = 0;
	
	@ManyToOne(targetEntity = Parking.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "parking_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Parking parking;
	
	@ManyToOne(targetEntity = BillReceivable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "bill_receivable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private BillReceivable billReceivable;
}
