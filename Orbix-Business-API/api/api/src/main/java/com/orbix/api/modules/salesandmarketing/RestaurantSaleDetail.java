package com.orbix.api.modules.salesandmarketing;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.inventoryandprocurement.Dineable;
import com.orbix.api.modules.inventoryandprocurement.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "restaurant_sale_details")
public class RestaurantSaleDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;	
	@NotNull
	private double costPriceVatIncl = 0;
	@NotNull
	private double sellingPriceVatIncl = 0;
	@NotNull
	private double vatRate = 0;
	@NotNull
	private double qty;
	@NotNull
	private double discount = 0;
	
	@ManyToOne(targetEntity = RestaurantSale.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "restaurant_sale_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantSale")
    private RestaurantSale restaurantSale;
	
	@ManyToOne(targetEntity = Dineable.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "dineable_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantSalesOrder")
    private Dineable dineable;
}
