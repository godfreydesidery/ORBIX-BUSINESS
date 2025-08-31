package com.orbix.api.modules.servicebay;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Workshop;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.salesandmarketing.SaleDetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Table(name = "machines")
public class Machine {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true, nullable = false)
	private String no;
	@Column(nullable = false)
	private String ownerName;
	private String ownerPhoneNo;
	@Column(nullable = false)
	private String regNo;
	@Column(nullable = false)
	private String name;

	String status = "PENDING";

	
	@ManyToOne(targetEntity = Workshop.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "workshop_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Workshop workshop;
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
	private LocalDateTime createdDateTime = LocalDateTime.now();	
		
	@ManyToOne(targetEntity = Branch.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "branch_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Branch branch;
	
	@OneToMany(targetEntity = MachineService.class, mappedBy = "machine", fetch = FetchType.LAZY, orphanRemoval = true)
    @Valid
    @JsonIgnoreProperties("machine")
	@Fetch(FetchMode.SUBSELECT)
    private List<MachineService> machineServices;
}
