package com.orbix.api.modules.finance;

import lombok.Data;

@Data
public class InvoiceReceivableDetailResponseDTO {	
	String id;
	String amount;
	String paid;
	String due;
	String status;
	String summary;
	String createdDateTime;
	String invoiceReceivableId;
	
	BillReceivableResponseDTO billReceivable;
}
