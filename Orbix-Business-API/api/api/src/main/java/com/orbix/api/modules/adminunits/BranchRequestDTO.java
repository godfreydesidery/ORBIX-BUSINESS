package com.orbix.api.modules.adminunits;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orbix.api.modules.identityandaccess.User;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class BranchRequestDTO {
	private Long id;
	private String code;
	private String name;	
	private String level;
	private String type;
	private boolean active;
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
	
	private Long companyId;
	private Long parentBranchId;
	
}
