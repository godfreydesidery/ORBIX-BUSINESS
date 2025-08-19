package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.Shop;
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
@Table(name = "restaurant_sales_orders", uniqueConstraints = { @UniqueConstraint(columnNames = {"no", "restaurant_id"})})
public class RestaurantSalesOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank
	private String no;
	
	private String customerName;
	
	private String summary;
	
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
	WorkFlowStatus status = WorkFlowStatus.PENDING;
	
	@ManyToOne(targetEntity = RestaurantAgent.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "restaurant_agent_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantSalesOrders")
    private RestaurantAgent restaurantAgent;
	
	@ManyToOne(targetEntity = RestaurantBadge.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "restaurant_badge_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantSalesOrders")
    private RestaurantBadge restaurantBadge;
	
	@ManyToOne(targetEntity = Restaurant.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("restaurantSalesOrders")
    private Restaurant restaurant;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
	
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "confirmed_by_user_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User confirmedByUser;
	
	private LocalDateTime confirmedDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "canceled_by_user_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User canceledByUser;
	
	private LocalDateTime canceledDateTime = LocalDateTime.now();
	
	@OneToMany(targetEntity = RestaurantSalesOrderDetail.class, mappedBy = "restaurantSalesOrder", fetch = FetchType.EAGER, orphanRemoval = true)
    @Valid
    @JsonIgnoreProperties("restaurantSalesOrder")
	@Fetch(FetchMode.SUBSELECT)
    private List<RestaurantSalesOrderDetail> restaurantSalesOrderDetails;
}
