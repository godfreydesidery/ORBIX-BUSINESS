package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "branches", uniqueConstraints = { @UniqueConstraint(columnNames = {"code", "company_id"}), @UniqueConstraint(columnNames = {"name", "company_id"})})
public class Branch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank
	private String code; 		
	@NotBlank
	private String name; 	
	private String level;	
	private String type;
	private boolean active = false;
		
	private String physicalAddress;
	private String postalCode;
	private String postalAddress;
	private String telephone;
	private String mobile;
	private String email;
	private String website;
	private String fax;
	
    private String city;
    private String state;
    private String country;
	
	private String managerName;
	private String openingHours;
	private int numberOfStaff;
	private double salesTargets;
	private LocalDate dateEstablished;
	private String notes;
	
	

	@ManyToOne(targetEntity = Company.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "company_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("branches")
    private Company company;
	
	@ManyToOne(targetEntity = Branch.class, fetch = FetchType.EAGER,  optional = true)
    @JoinColumn(name = "parent_branch_id", nullable = true , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("branches")
    private Branch parentBranch;
	
	@ManyToOne(targetEntity = Currency.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "currency_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("currencies")
    private Currency currency;
	
	@ManyToOne(targetEntity = TimeZone.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "time_zone_id", nullable = false , updatable = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnoreProperties("timeZones")
    private TimeZone timeZone;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	private Collection<Branch> childBranches = new ArrayList<>();
	
	
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER,  optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false , updatable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;
	
	private LocalDateTime createdDateTime = LocalDateTime.now();

}
