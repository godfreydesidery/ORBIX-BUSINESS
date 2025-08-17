package com.orbix.api.modules.inventoryandprocurement;

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
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.adminunits.Restaurant;
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
@Table(name = "restaurant_dineables")
public class RestaurantDineable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	// Prices
	@Column(nullable = false)
	@DecimalMin("0.0")
	@DecimalMax("1.0")
	private double vatRate = 0; // Fractional rate, e.g., 0.18 for 18%
	private double costPriceVatIncl = 0;
    private double costPriceVatExcl = 0;
    private double sellingPriceVatIncl = 0;
    private double sellingPriceVatExcl = 0;
	
	
	private boolean active = false;
	
	@ManyToOne(targetEntity = Dineable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "dineable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantDineables")
    private Dineable dineable;
	
	@ManyToOne(targetEntity = Restaurant.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantDineables")
    private Restaurant restaurant;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
		
	private LocalDateTime createdDateTime = LocalDateTime.now();
}
