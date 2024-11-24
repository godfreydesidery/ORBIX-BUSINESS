package com.orbix.api.modules.finance;

import java.util.List;

import lombok.Data;

@Data
public class BillReceivableSummaryDTO {
	List<BillReceivableRequestDTO> billReceivables;
	List<BillReceivableCollectionRequestDTO> billReceivableCollections;
}
