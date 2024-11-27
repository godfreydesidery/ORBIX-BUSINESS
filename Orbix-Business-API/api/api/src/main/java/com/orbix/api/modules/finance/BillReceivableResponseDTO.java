package com.orbix.api.modules.finance;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Company;

import lombok.Data;

@Data
public class BillReceivableResponseDTO {	
	String id;
	String no;
	String amount;
	String paid;
	String due;
	String payStatus;
	String summary;
	String createdDateTime;
	String companyId;
	String companyName;
	String branchId;
	String branchName;	
	
	String qty;
}
