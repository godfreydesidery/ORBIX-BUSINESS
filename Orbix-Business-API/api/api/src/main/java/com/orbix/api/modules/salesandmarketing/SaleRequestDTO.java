package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import lombok.Data;

@Data
public class SaleRequestDTO {
	Long id;
	
	List<SaleDetailRequestDTO> saleDetails;
}
