package com.orbix.api.modules.bond;

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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipmentType;
import com.orbix.api.modules.adminunits.Company;
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
@Table(name = "bond_item_types", uniqueConstraints = { @UniqueConstraint(columnNames = {"code", "company_id"}), @UniqueConstraint(columnNames = {"name", "company_id"})})
public class BondItemType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String code;
	
	@Column(nullable = false)
	private String name;
	
	@Column(name = "currency")
	java.util.Currency currency;
	
	double dailyPrice = 0;
	boolean dailyPriceActive = true;
	double hourlyPrice = 0;
	boolean hourlyPriceActive = false;
	
	private boolean active = false;
		
	@ManyToOne(targetEntity = Company.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "company_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Company company;
		
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
		
	private LocalDateTime createdDateTime = LocalDateTime.now();
}
