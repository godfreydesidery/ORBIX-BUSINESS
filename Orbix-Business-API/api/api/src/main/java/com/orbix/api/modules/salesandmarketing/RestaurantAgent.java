package com.orbix.api.modules.salesandmarketing;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.adminunits.Restaurant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "restaurant_agents", uniqueConstraints = { @UniqueConstraint(columnNames = {"name", "restaurant_id"})})
public class RestaurantAgent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	private String name;
	
	@NotBlank
	private String phoneNo;
	
	private boolean active = false;
	
	@OneToOne(targetEntity = RestaurantBadge.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "restaurant_badge_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantBadges")
    private RestaurantBadge restaurantBadge; 
	
	@ManyToOne(targetEntity = Restaurant.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantBadges")
    private Restaurant restaurant;
}
