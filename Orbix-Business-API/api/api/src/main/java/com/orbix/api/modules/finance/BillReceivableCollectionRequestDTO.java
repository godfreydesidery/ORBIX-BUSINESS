package com.orbix.api.modules.finance;

import com.orbix.api.api.commons.PayCode;

import lombok.Data;

@Data
public class BillReceivableCollectionRequestDTO {
	double amount;
	PayCode payCode;
	String refNo;

}
