package com.orbix.api.modules.finance;

import java.util.List;

import lombok.Data;

@Data
public class BillReceivableRequestDTO {
	Long id;
	String no;
	double amount;
	Long companyId;
	String companyName;
	Long branchId;
	String branchName;
	
	double qty;
}