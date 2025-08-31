package com.orbix.api.modules.servicebay;

import java.time.LocalDateTime;

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

import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Workshop;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.inventoryandprocurement.Servicel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "machine_services")
public class MachineService {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private double qty = 1;
	
	@NotNull
	private double price;
	
	String status = "PENDING";
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@ManyToOne(targetEntity = Machine.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "machine_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Machine machine;
	
	@ManyToOne(targetEntity = Servicel.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "service_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Servicel service;
}
