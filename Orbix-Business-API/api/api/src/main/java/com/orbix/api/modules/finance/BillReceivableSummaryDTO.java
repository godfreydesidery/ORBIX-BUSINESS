package com.orbix.api.modules.finance;

import java.util.List;

import com.orbix.api.api.commons.PayCode;

import lombok.Data;

@Data
public class BillReceivableSummaryDTO {
	List<BillReceivableRequestDTO> billReceivables;
//	List<BillReceivableCollectionRequestDTO> billReceivableCollections;
	
	PayCode payCode;
}
