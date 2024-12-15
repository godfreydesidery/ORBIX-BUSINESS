package com.orbix.api.modules.salesandmarketing;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.inventoryandprocurement.Product;
import com.orbix.api.modules.inventoryandprocurement.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SaleServiceController implements SaleService {
	
	private final SaleRepository saleRepository;
	private final SaleDetailRepository saleDetailRepository;
	private final ProductRepository productRepository;
	
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public boolean createSale(SaleRequestDTO saleRequest, HttpServletRequest request) {
		
		Sale sale = new Sale();
		
		sale.setCreatedByUser(userService.getUser(request));
		sale.setCreatedDateTime(dayService.getTimeStamp());
		
		sale = saleRepository.save(sale);
		
		if (saleRequest.getSaleDetails() == null || saleRequest.getSaleDetails().isEmpty()) {
		    throw new IllegalArgumentException("Sale details cannot be empty.");
		}
		
		for(SaleDetailRequestDTO saleDetailRequest : saleRequest.getSaleDetails()) {
			SaleDetail saleDetail = new SaleDetail();
			saleDetail.setSale(sale);
			saleDetail.setProduct(saleDetailRequest.getProduct());
			saleDetail.setCostPriceVatIncl(saleDetailRequest.getCostPriceVatIncl());
			saleDetail.setSellingPriceVatIncl(saleDetailRequest.getSellingPriceVatIncl());
			saleDetail.setVatRate(saleDetailRequest.getVatRate());
			saleDetail.setQty(saleDetailRequest.getQty());
			saleDetailRepository.save(saleDetail);
			
			
		}	
		return true;
	}
	
	

}
