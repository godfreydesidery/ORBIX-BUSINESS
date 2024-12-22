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
import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.Shop;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.inventoryandprocurement.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "shop_sales_orders", uniqueConstraints = { @UniqueConstraint(columnNames = {"no", "shop_id"})})
public class ShopSalesOrder {
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
	
	@ManyToOne(targetEntity = Shop.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "shop_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("shopSalesOrders")
    private Shop shop;
	
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
	
	@OneToMany(targetEntity = ShopSalesOrderDetail.class, mappedBy = "shopSalesOrder", fetch = FetchType.EAGER, orphanRemoval = true)
    @Valid
    @JsonIgnoreProperties("shopSalesOrder")
	@Fetch(FetchMode.SUBSELECT)
    private List<ShopSalesOrderDetail> shopSalesOrderDetails;
	
	
}
