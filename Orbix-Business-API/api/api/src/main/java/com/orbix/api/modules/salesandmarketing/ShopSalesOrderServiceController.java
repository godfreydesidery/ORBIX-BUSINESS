package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Shop;
import com.orbix.api.modules.adminunits.ShopRepository;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.inventoryandprocurement.Product;
import com.orbix.api.modules.inventoryandprocurement.ProductRepository;
import com.orbix.api.modules.inventoryandprocurement.ShopProduct;
import com.orbix.api.modules.inventoryandprocurement.ShopProductLog;
import com.orbix.api.modules.inventoryandprocurement.ShopProductLogRepository;
import com.orbix.api.modules.inventoryandprocurement.ShopProductRepository;
import com.orbix.api.modules.inventoryandprocurement.ShopProductResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShopSalesOrderServiceController implements ShopSalesOrderService {
	
	private final ShopSalesOrderRepository shopSalesOrderRepository;
	private final ShopSalesOrderDetailRepository shopSalesOrderDetailRepository;
	private final ShopRepository shopRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final ShopProductRepository shopProductRepository;
	private final ProductRepository productRepository;
	
	private final SaleService saleService;
	private final ShopProductLogRepository shopProductLogRepository;
	
	
	@Override
	public List<ShopSalesOrderResponseDTO> getAllShopSalesOrders(Long shopId, HttpServletRequest request) {
		
		
		
		return null;
	}
	
	@Override
	public List<ShopSalesOrderResponseDTO> getAllPendingShopSalesOrders(Long shopId, HttpServletRequest request) {
		
		Shop shop = shopRepository.findById(shopId)
			    .orElseThrow(() -> new NotFoundException("Shop not found, with id " + shopId));

			List<ShopSalesOrder> shopSalesOrders = shopSalesOrderRepository.findAllByShopAndStatus(shop, WorkFlowStatus.PENDING);

			return shopSalesOrders.stream()
			    .map(this::shopSalesOrderResponseDTOMapper)
			    .collect(Collectors.toList());
	}

	@Override
	public ShopSalesOrderResponseDTO get(Long id, HttpServletRequest request) {
		return shopSalesOrderRepository.findById(id)
			    .map(this::shopSalesOrderResponseDTOMapperWithDetails)
			    .orElseThrow(() -> new NotFoundException("Sales Order not found, with ID " + id));
	}

	@Override
	public ShopSalesOrderResponseDTO createShopSalesOrder(ShopSalesOrderRequestDTO shopSalesOrderRequest,
			HttpServletRequest request) {
		
		Shop shop = shopRepository.findById(shopSalesOrderRequest.getShopId())
			    .orElseThrow(() -> new NotFoundException("Shop not found, with id " + shopSalesOrderRequest.getShopId()));
		
		ShopSalesOrder shopSalesOrder = new ShopSalesOrder();
		
		shopSalesOrder.setNo(String.valueOf(Math.random()));
		shopSalesOrder.setShop(shop);
		shopSalesOrder.setStatus(WorkFlowStatus.PENDING);
		shopSalesOrder.setSummary(shopSalesOrderRequest.getSummary());
		shopSalesOrder.setCreatedByUser(userService.getUser(request));
		shopSalesOrder.setCreatedDateTime(dayService.getTimeStamp());
		shopSalesOrder = shopSalesOrderRepository.save(shopSalesOrder);
		shopSalesOrder.setNo(shopSalesOrder.getId().toString());
		
		shopSalesOrder = shopSalesOrderRepository.save(shopSalesOrder);
		
		return shopSalesOrderResponseDTOMapper(shopSalesOrder);
	}

	@Override
	public ShopSalesOrderResponseDTO updateShopSalesOrder(ShopSalesOrderRequestDTO shopSalesOrder,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private ShopSalesOrderResponseDTO shopSalesOrderResponseDTOMapper(ShopSalesOrder shopSalesOrder) {
		ShopSalesOrderResponseDTO shopSalesOrderResponse = new ShopSalesOrderResponseDTO();
		
		shopSalesOrderResponse.setId(shopSalesOrder.getId().toString());
		shopSalesOrderResponse.setNo(shopSalesOrder.getNo());
		shopSalesOrderResponse.setShopId(shopSalesOrder.getShop().getId().toString());
		shopSalesOrderResponse.setStatus(shopSalesOrder.getStatus().toString());
		shopSalesOrderResponse.setSummary(shopSalesOrder.getSummary());
		
		return shopSalesOrderResponse;
	}
	
	private ShopSalesOrderResponseDTO shopSalesOrderResponseDTOMapperWithDetails(ShopSalesOrder shopSalesOrder) {
		ShopSalesOrderResponseDTO shopSalesOrderResponse = new ShopSalesOrderResponseDTO();
		List<ShopSalesOrderDetailResponseDTO> shopSalesOrderDetailResponses = new ArrayList<>();
		
		shopSalesOrderResponse.setId(shopSalesOrder.getId().toString());
		shopSalesOrderResponse.setNo(shopSalesOrder.getNo());
		shopSalesOrderResponse.setShopId(shopSalesOrder.getShop().getId().toString());
		shopSalesOrderResponse.setStatus(shopSalesOrder.getStatus().toString());
		shopSalesOrderResponse.setSummary(shopSalesOrder.getSummary());
		
		for(ShopSalesOrderDetail shopSalesOrderDetail : shopSalesOrder.getShopSalesOrderDetails()) {
			shopSalesOrderDetailResponses.add(shopSalesOrderDetailResponseDTOMapper(shopSalesOrderDetail));
		}
		shopSalesOrderResponse.setShopSalesOrderDetails(shopSalesOrderDetailResponses);
		
		return shopSalesOrderResponse;
	}
	
	private ShopSalesOrderDetailResponseDTO shopSalesOrderDetailResponseDTOMapper(ShopSalesOrderDetail shopSalesOrderDetail) {
		ShopSalesOrderDetailResponseDTO shopSalesOrderDetailResponse = new ShopSalesOrderDetailResponseDTO();
		
		shopSalesOrderDetailResponse.setId(shopSalesOrderDetail.getId().toString());
		shopSalesOrderDetailResponse.setCostPriceVatIncl(String.valueOf(shopSalesOrderDetail.getCostPriceVatIncl()));
		shopSalesOrderDetailResponse.setSellingPriceVatIncl(String.valueOf(shopSalesOrderDetail.getSellingPriceVatIncl()));
		shopSalesOrderDetailResponse.setVatRate(String.valueOf(shopSalesOrderDetail.getVatRate()));
		shopSalesOrderDetailResponse.setQty(String.valueOf(shopSalesOrderDetail.getQty()));
		shopSalesOrderDetailResponse.setProductCode(shopSalesOrderDetail.getProduct().getCode());
		shopSalesOrderDetailResponse.setProductName(shopSalesOrderDetail.getProduct().getName());
		shopSalesOrderDetailResponse.setProductDescription(shopSalesOrderDetail.getProduct().getDescription());
		shopSalesOrderDetailResponse.setBaseUom(shopSalesOrderDetail.getProduct().getBaseUom());
		shopSalesOrderDetailResponse.setAmount(String.valueOf(shopSalesOrderDetail.getSellingPriceVatIncl() * shopSalesOrderDetail.getQty()));
		
		
		return shopSalesOrderDetailResponse;
	}

	@Override
	public List<ShopSalesOrderDetailResponseDTO> getAllShopSalesOrderDetails(Long salesOrderId,
			HttpServletRequest request) {
		ShopSalesOrder shopSalesOrder = shopSalesOrderRepository.findById(salesOrderId)
			    .orElseThrow(() -> new NotFoundException("Sales Order not found, with id " + salesOrderId));
		
		List<ShopSalesOrderDetail> shopSalesOrderDetails = shopSalesOrderDetailRepository.findAllByShopSalesOrder(shopSalesOrder);
		
		List<ShopSalesOrderDetailResponseDTO> shopSalesOrderDetailResponses = new ArrayList<>();
		
		for(ShopSalesOrderDetail shopSalesOrderDetail : shopSalesOrderDetails) {
			shopSalesOrderDetailResponses.add(shopSalesOrderDetailResponseDTOMapper(shopSalesOrderDetail));
		}
		
		return shopSalesOrderDetailResponses;
	}
	
	@Override
	public void createShopSalesOrderDetail(ShopSalesOrderDetailRequestDTO shopSalesOrderDetailRequest,
			HttpServletRequest request) {
		
		ShopSalesOrder shopSalesOrder = shopSalesOrderRepository.findById(shopSalesOrderDetailRequest.getShopSalesOrderId())
			    .orElseThrow(() -> new NotFoundException("Order not found, with id " + shopSalesOrderDetailRequest.getShopSalesOrderId()));
		
		if(!String.valueOf(shopSalesOrder.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		Product product = productRepository.findById(shopSalesOrderDetailRequest.getProductId()).get();
		
		ShopProduct shopProduct = shopProductRepository.findByShopAndProduct(shopSalesOrder.getShop(), product).get();
		
		ShopSalesOrderDetail shopSalesOrderDetail = new ShopSalesOrderDetail();
		
		shopSalesOrderDetail.setCostPriceVatIncl(shopProduct.getCostPriceVatIncl());
		shopSalesOrderDetail.setSellingPriceVatIncl(shopProduct.getSellingPriceVatIncl());
		shopSalesOrderDetail.setVatRate(shopProduct.getVatRate());
		shopSalesOrderDetail.setProduct(product);
		if(shopSalesOrderDetailRequest.getQty() <= 0) {
			throw new InvalidOperationException("Invalid quantiy selected");
		}
		if(shopSalesOrderDetailRepository.existsByShopSalesOrderAndProduct(shopSalesOrder, product)) {
			throw new InvalidOperationException("Product already present in order");
		}
		shopSalesOrderDetail.setQty(shopSalesOrderDetailRequest.getQty());
		shopSalesOrderDetail.setShopSalesOrder(shopSalesOrder);
		

		shopSalesOrderDetail.setCreatedByUser(userService.getUser(request));
		shopSalesOrderDetail.setCreatedDateTime(dayService.getTimeStamp());

		
		shopSalesOrderDetail = shopSalesOrderDetailRepository.save(shopSalesOrderDetail);
		
		
	}

	@Override
	public void removeShopOrderDetail(Long shopOrderDetailId, Long shopOrderId, HttpServletRequest request) {
		ShopSalesOrder shopSalesOrder = shopSalesOrderRepository.findById(shopOrderId)
			    .orElseThrow(() -> new NotFoundException("Order not found, with id " + shopOrderId));
		if(!String.valueOf(shopSalesOrder.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		ShopSalesOrderDetail shopSalesOrderDetail = shopSalesOrderDetailRepository.findById(shopOrderDetailId)
			    .orElseThrow(() -> new NotFoundException("Detail not found, with id " + shopOrderDetailId));
		
		if(shopSalesOrderDetail.getShopSalesOrder().getId() != shopOrderId) {
			throw new InvalidOperationException("Detail do not belong to this sales order");
		}
		
		shopSalesOrderDetailRepository.delete(shopSalesOrderDetail);		
	}

	@Override
	public boolean confirmShopSalesOrder(Long shopOrderId, HttpServletRequest request) {
		ShopSalesOrder shopSalesOrder = shopSalesOrderRepository.findById(shopOrderId)
			    .orElseThrow(() -> new NotFoundException("Order not found, with id " + shopOrderId));
		if(!String.valueOf(shopSalesOrder.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		SaleRequestDTO saleRequest = new SaleRequestDTO();
		
		List<SaleDetailRequestDTO> saleDetails = new ArrayList<>();
		
		for(ShopSalesOrderDetail shopSalesOrderDetail : shopSalesOrder.getShopSalesOrderDetails()) {
			SaleDetailRequestDTO saleDetailRequest = new SaleDetailRequestDTO();
			saleDetailRequest.setCostPriceVatIncl(shopSalesOrderDetail.getCostPriceVatIncl());
			saleDetailRequest.setSellingPriceVatIncl(shopSalesOrderDetail.getSellingPriceVatIncl());
			saleDetailRequest.setVatRate(shopSalesOrderDetail.getVatRate());
			saleDetailRequest.setQty(shopSalesOrderDetail.getQty());
			saleDetailRequest.setProduct(shopSalesOrderDetail.getProduct());
			saleDetails.add(saleDetailRequest);
			// Review this
			
			ShopProduct shopProduct = shopProductRepository.findByProductAndShop(shopSalesOrderDetail.getProduct(), shopSalesOrder.getShop()).orElseThrow();
			
			this.createShopProductLog(shopSalesOrder.getShop(), shopSalesOrderDetail.getProduct(), 0, shopSalesOrderDetail.getQty(), (shopProduct.getCurrentStock() - shopSalesOrderDetail.getQty()), userService.getUser(request), dayService.getTimeStamp(), "Shop sale");

		}
		saleRequest.setSaleDetails(saleDetails);
		
		saleService.createSale(saleRequest, request);
		
		shopSalesOrder.setStatus(WorkFlowStatus.CONFIRMED);
		
		shopSalesOrder.setConfirmedByUser(userService.getUser(request));
		shopSalesOrder.setConfirmedDateTime(dayService.getTimeStamp());
		
		shopSalesOrderRepository.save(shopSalesOrder);
		
		return true;
	}
	
	@Override
	public boolean cancelShopSalesOrder(Long shopOrderId, HttpServletRequest request) {
		ShopSalesOrder shopSalesOrder = shopSalesOrderRepository.findById(shopOrderId)
			    .orElseThrow(() -> new NotFoundException("Order not found, with id " + shopOrderId));
		if(!String.valueOf(shopSalesOrder.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		shopSalesOrder.setStatus(WorkFlowStatus.CANCELED);
		
		shopSalesOrder.setCanceledByUser(userService.getUser(request));
		shopSalesOrder.setCanceledDateTime(dayService.getTimeStamp());
		
		shopSalesOrderRepository.save(shopSalesOrder);
		
		return true;
	}
	
	private boolean createShopProductLog(Shop shop, Product product, double qtyIn, double qtyOut, double balance, User createdByUser, LocalDateTime createdDateTime, String reference) {
		// Create a shop product log
		ShopProductLog shopProductLog = new ShopProductLog();
		shopProductLog.setShop(shop);
		shopProductLog.setProduct(product);
		shopProductLog.setQtyIn(qtyIn);
		shopProductLog.setQtyOut(qtyOut);
		shopProductLog.setBalance(balance);
		shopProductLog.setReference(reference);
		shopProductLog.setCreatedByUser(createdByUser);
		shopProductLog.setCreatedDateTime(createdDateTime);
		shopProductLogRepository.save(shopProductLog);
		return true;
	}

}
