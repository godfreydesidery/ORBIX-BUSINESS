package com.orbix.api.modules.salesandmarketing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.PayCode;
import com.orbix.api.api.commons.PayStatus;
import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Restaurant;
import com.orbix.api.modules.adminunits.RestaurantRepository;
import com.orbix.api.modules.finance.BillReceivable;
import com.orbix.api.modules.finance.BillReceivableCollection;
import com.orbix.api.modules.finance.BillReceivableCollectionRepository;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.finance.Collection;
import com.orbix.api.modules.finance.CollectionRepository;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.inventoryandprocurement.Dineable;
import com.orbix.api.modules.inventoryandprocurement.DineableRepository;
import com.orbix.api.modules.inventoryandprocurement.RestaurantDineable;
import com.orbix.api.modules.inventoryandprocurement.RestaurantDineableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RestaurantSalesOrderServiceController implements RestaurantSalesOrderService {
	private final RestaurantSalesOrderRepository restaurantSalesOrderRepository;
	private final RestaurantSalesOrderDetailRepository restaurantSalesOrderDetailRepository;
	private final RestaurantRepository restaurantRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final RestaurantDineableRepository restaurantDineableRepository;
	private final DineableRepository dineableRepository;
	
	private final RestaurantSaleService restaurantSaleService;
//	private final RestaurantDineableLogRepository restaurantDineableLogRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	private final RestaurantSaleDetailBillReceivableRepository restaurantSaleDetailBillReceivableRepository;
	
	private final CollectionRepository collectionRepository;
	private final BillReceivableCollectionRepository billReceivableCollectionRepository;
	
	private final SaleRepository saleRepository;
	
	
	@Override
	public List<RestaurantSalesOrderResponseDTO> getAllRestaurantSalesOrders(Long restaurantId, HttpServletRequest request) {
		
		
		
		return null;
	}
	
	@Override
	public List<RestaurantSalesOrderResponseDTO> getAllPendingRestaurantSalesOrders(Long restaurantId, HttpServletRequest request) {
		
		Restaurant restaurant = restaurantRepository.findById(restaurantId)
			    .orElseThrow(() -> new NotFoundException("Restaurant not found, with id " + restaurantId));

			List<RestaurantSalesOrder> restaurantSalesOrders = restaurantSalesOrderRepository.findAllByRestaurantAndStatus(restaurant, WorkFlowStatus.PENDING);

			return restaurantSalesOrders.stream()
			    .map(this::restaurantSalesOrderResponseDTOMapper)
			    .collect(Collectors.toList());
	}

	@Override
	public RestaurantSalesOrderResponseDTO get(Long id, HttpServletRequest request) {
		return restaurantSalesOrderRepository.findById(id)
			    .map(this::restaurantSalesOrderResponseDTOMapperWithDetails)
			    .orElseThrow(() -> new NotFoundException("Sales Order not found, with ID " + id));
	}

	@Override
	public RestaurantSalesOrderResponseDTO createRestaurantSalesOrder(RestaurantSalesOrderRequestDTO restaurantSalesOrderRequest,
			HttpServletRequest request) {
		
		Restaurant restaurant = restaurantRepository.findById(restaurantSalesOrderRequest.getRestaurantId())
			    .orElseThrow(() -> new NotFoundException("Restaurant not found, with id " + restaurantSalesOrderRequest.getRestaurantId()));
		
		RestaurantSalesOrder restaurantSalesOrder = new RestaurantSalesOrder();
		
		restaurantSalesOrder.setNo(String.valueOf(Math.random()));
		restaurantSalesOrder.setRestaurant(restaurant);
		restaurantSalesOrder.setStatus(WorkFlowStatus.PENDING);
		restaurantSalesOrder.setSummary(restaurantSalesOrderRequest.getSummary());
		restaurantSalesOrder.setCustomerName(restaurantSalesOrderRequest.getCustomerName());
		restaurantSalesOrder.setCreatedByUser(userService.getUser(request));
		restaurantSalesOrder.setCreatedDateTime(dayService.getTimeStamp());
		restaurantSalesOrder = restaurantSalesOrderRepository.save(restaurantSalesOrder);
		restaurantSalesOrder.setNo(restaurantSalesOrder.getId().toString());
		
		restaurantSalesOrder = restaurantSalesOrderRepository.save(restaurantSalesOrder);
		
		return restaurantSalesOrderResponseDTOMapper(restaurantSalesOrder);
	}

	@Override
	public RestaurantSalesOrderResponseDTO updateRestaurantSalesOrder(RestaurantSalesOrderRequestDTO restaurantSalesOrder,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private RestaurantSalesOrderResponseDTO restaurantSalesOrderResponseDTOMapper(RestaurantSalesOrder restaurantSalesOrder) {
		RestaurantSalesOrderResponseDTO restaurantSalesOrderResponse = new RestaurantSalesOrderResponseDTO();
		
		restaurantSalesOrderResponse.setId(restaurantSalesOrder.getId().toString());
		restaurantSalesOrderResponse.setNo(restaurantSalesOrder.getNo());
		restaurantSalesOrderResponse.setRestaurantId(restaurantSalesOrder.getRestaurant().getId().toString());
		restaurantSalesOrderResponse.setStatus(restaurantSalesOrder.getStatus().toString());
		restaurantSalesOrderResponse.setSummary(restaurantSalesOrder.getSummary());
		restaurantSalesOrderResponse.setCustomerName(restaurantSalesOrder.getCustomerName());
		
		return restaurantSalesOrderResponse;
	}
	
	private RestaurantSalesOrderResponseDTO restaurantSalesOrderResponseDTOMapperWithDetails(RestaurantSalesOrder restaurantSalesOrder) {
		RestaurantSalesOrderResponseDTO restaurantSalesOrderResponse = new RestaurantSalesOrderResponseDTO();
		List<RestaurantSalesOrderDetailResponseDTO> restaurantSalesOrderDetailResponses = new ArrayList<>();
		
		restaurantSalesOrderResponse.setId(restaurantSalesOrder.getId().toString());
		restaurantSalesOrderResponse.setNo(restaurantSalesOrder.getNo());
		restaurantSalesOrderResponse.setRestaurantId(restaurantSalesOrder.getRestaurant().getId().toString());
		restaurantSalesOrderResponse.setStatus(restaurantSalesOrder.getStatus().toString());
		restaurantSalesOrderResponse.setSummary(restaurantSalesOrder.getSummary());
		restaurantSalesOrderResponse.setCustomerName(restaurantSalesOrder.getCustomerName());
		
		for(RestaurantSalesOrderDetail restaurantSalesOrderDetail : restaurantSalesOrder.getRestaurantSalesOrderDetails()) {
			restaurantSalesOrderDetailResponses.add(restaurantSalesOrderDetailResponseDTOMapper(restaurantSalesOrderDetail));
		}
		restaurantSalesOrderResponse.setRestaurantSalesOrderDetails(restaurantSalesOrderDetailResponses);
		
		return restaurantSalesOrderResponse;
	}
	
	private RestaurantSalesOrderDetailResponseDTO restaurantSalesOrderDetailResponseDTOMapper(RestaurantSalesOrderDetail restaurantSalesOrderDetail) {
		RestaurantSalesOrderDetailResponseDTO restaurantSalesOrderDetailResponse = new RestaurantSalesOrderDetailResponseDTO();
		
		restaurantSalesOrderDetailResponse.setId(restaurantSalesOrderDetail.getId().toString());
		restaurantSalesOrderDetailResponse.setCostPriceVatIncl(String.valueOf(restaurantSalesOrderDetail.getCostPriceVatIncl()));
		restaurantSalesOrderDetailResponse.setSellingPriceVatIncl(String.valueOf(restaurantSalesOrderDetail.getSellingPriceVatIncl()));
		restaurantSalesOrderDetailResponse.setVatRate(String.valueOf(restaurantSalesOrderDetail.getVatRate()));
		restaurantSalesOrderDetailResponse.setQty(String.valueOf(restaurantSalesOrderDetail.getQty()));
		restaurantSalesOrderDetailResponse.setDiscount(String.valueOf(restaurantSalesOrderDetail.getDiscount()));
		restaurantSalesOrderDetailResponse.setDineableCode(restaurantSalesOrderDetail.getDineable().getCode());
		restaurantSalesOrderDetailResponse.setDineableName(restaurantSalesOrderDetail.getDineable().getName());
		restaurantSalesOrderDetailResponse.setDineableDescription(restaurantSalesOrderDetail.getDineable().getDescription());
		restaurantSalesOrderDetailResponse.setBaseUom(restaurantSalesOrderDetail.getDineable().getBaseUom());
		restaurantSalesOrderDetailResponse.setAmount(String.valueOf(restaurantSalesOrderDetail.getSellingPriceVatIncl() * restaurantSalesOrderDetail.getQty() - restaurantSalesOrderDetail.getDiscount()));
		
		
		return restaurantSalesOrderDetailResponse;
	}

	@Override
	public List<RestaurantSalesOrderDetailResponseDTO> getAllRestaurantSalesOrderDetails(Long salesOrderId,
			HttpServletRequest request) {
		RestaurantSalesOrder restaurantSalesOrder = restaurantSalesOrderRepository.findById(salesOrderId)
			    .orElseThrow(() -> new NotFoundException("Sales Order not found, with id " + salesOrderId));
		
		List<RestaurantSalesOrderDetail> restaurantSalesOrderDetails = restaurantSalesOrderDetailRepository.findAllByRestaurantSalesOrder(restaurantSalesOrder);
		
		List<RestaurantSalesOrderDetailResponseDTO> restaurantSalesOrderDetailResponses = new ArrayList<>();
		
		for(RestaurantSalesOrderDetail restaurantSalesOrderDetail : restaurantSalesOrderDetails) {
			restaurantSalesOrderDetailResponses.add(restaurantSalesOrderDetailResponseDTOMapper(restaurantSalesOrderDetail));
		}
		
		return restaurantSalesOrderDetailResponses;
	}
	
	@Override
	public void createRestaurantSalesOrderDetail(RestaurantSalesOrderDetailRequestDTO restaurantSalesOrderDetailRequest,
			HttpServletRequest request) {
		
		RestaurantSalesOrder restaurantSalesOrder = restaurantSalesOrderRepository.findById(restaurantSalesOrderDetailRequest.getRestaurantSalesOrderId())
			    .orElseThrow(() -> new NotFoundException("Order not found, with id " + restaurantSalesOrderDetailRequest.getRestaurantSalesOrderId()));
		
		if(!String.valueOf(restaurantSalesOrder.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		Dineable dineable = dineableRepository.findById(restaurantSalesOrderDetailRequest.getDineableId()).get();
		
		RestaurantDineable restaurantDineable = restaurantDineableRepository.findByRestaurantAndDineable(restaurantSalesOrder.getRestaurant(), dineable).get();
		
		RestaurantSalesOrderDetail restaurantSalesOrderDetail = new RestaurantSalesOrderDetail();
		
		restaurantSalesOrderDetail.setCostPriceVatIncl(restaurantDineable.getCostPriceVatIncl());
		restaurantSalesOrderDetail.setSellingPriceVatIncl(restaurantDineable.getSellingPriceVatIncl());
		restaurantSalesOrderDetail.setVatRate(restaurantDineable.getVatRate());
		restaurantSalesOrderDetail.setDineable(dineable);
		if(restaurantSalesOrderDetailRequest.getQty() <= 0) {
			throw new InvalidOperationException("Invalid quantiy selected");
		}
		
		if(restaurantSalesOrderDetailRepository.existsByRestaurantSalesOrderAndDineable(restaurantSalesOrder, dineable)) {
			throw new InvalidOperationException("Dineable already present in order");
		}
		restaurantSalesOrderDetail.setQty(restaurantSalesOrderDetailRequest.getQty());
		restaurantSalesOrderDetail.setRestaurantSalesOrder(restaurantSalesOrder);
		
		if(restaurantSalesOrderDetailRequest.getDiscount() > (restaurantSalesOrderDetail.getSellingPriceVatIncl() * restaurantSalesOrderDetail.getQty())) {
			throw new InvalidOperationException("Invalid discount. Discount is more than amount");
		}
		restaurantSalesOrderDetail.setDiscount(restaurantSalesOrderDetailRequest.getDiscount());
		

		restaurantSalesOrderDetail.setCreatedByUser(userService.getUser(request));
		restaurantSalesOrderDetail.setCreatedDateTime(dayService.getTimeStamp());

		
		restaurantSalesOrderDetail = restaurantSalesOrderDetailRepository.save(restaurantSalesOrderDetail);
		
		
	}

	@Override
	public void removeRestaurantOrderDetail(Long restaurantOrderDetailId, Long restaurantOrderId, HttpServletRequest request) {
		RestaurantSalesOrder restaurantSalesOrder = restaurantSalesOrderRepository.findById(restaurantOrderId)
			    .orElseThrow(() -> new NotFoundException("Order not found, with id " + restaurantOrderId));
		if(!String.valueOf(restaurantSalesOrder.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		RestaurantSalesOrderDetail restaurantSalesOrderDetail = restaurantSalesOrderDetailRepository.findById(restaurantOrderDetailId)
			    .orElseThrow(() -> new NotFoundException("Detail not found, with id " + restaurantOrderDetailId));
		
		if(restaurantSalesOrderDetail.getRestaurantSalesOrder().getId() != restaurantOrderId) {
			throw new InvalidOperationException("Detail do not belong to this sales order");
		}
		
		restaurantSalesOrderDetailRepository.delete(restaurantSalesOrderDetail);		
	}

	@Override
	public boolean confirmRestaurantSalesOrder(
			Long restaurantOrderId, 
			PayCode payCode,
			String payRefNo,
			HttpServletRequest request) {
		RestaurantSalesOrder restaurantSalesOrder = restaurantSalesOrderRepository.findById(restaurantOrderId)
			    .orElseThrow(() -> new NotFoundException("Order not found, with id " + restaurantOrderId));
		if(!String.valueOf(restaurantSalesOrder.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		RestaurantSaleRequestDTO restaurantSaleRequest = new RestaurantSaleRequestDTO();
		
		List<RestaurantSaleDetailRequestDTO> restaurantSaleDetails = new ArrayList<>();
		
		for(RestaurantSalesOrderDetail restaurantSalesOrderDetail : restaurantSalesOrder.getRestaurantSalesOrderDetails()) {
			RestaurantSaleDetailRequestDTO restaurantSaleDetailRequest = new RestaurantSaleDetailRequestDTO();
			restaurantSaleDetailRequest.setCostPriceVatIncl(restaurantSalesOrderDetail.getCostPriceVatIncl());
			restaurantSaleDetailRequest.setSellingPriceVatIncl(restaurantSalesOrderDetail.getSellingPriceVatIncl());
			restaurantSaleDetailRequest.setVatRate(restaurantSalesOrderDetail.getVatRate());
			restaurantSaleDetailRequest.setQty(restaurantSalesOrderDetail.getQty());
			restaurantSaleDetailRequest.setDiscount(restaurantSalesOrderDetail.getDiscount());
			restaurantSaleDetailRequest.setDineable(restaurantSalesOrderDetail.getDineable());
			restaurantSaleDetails.add(restaurantSaleDetailRequest);
			// Review this
			
			RestaurantDineable restaurantDineable = restaurantDineableRepository.findByDineableAndRestaurant(restaurantSalesOrderDetail.getDineable(), restaurantSalesOrder.getRestaurant()).orElseThrow();
			
//			if(restaurantDineable.getCurrentStock() < restaurantSalesOrderDetail.getQty()) {
//				//throw new InvalidOperationException("Exceeds available stock in dineable " + restaurantDineable.getDineable().getName());
//			}
			
//			double newStock = restaurantDineable.getCurrentStock() - restaurantSalesOrderDetail.getQty();
			
//			restaurantDineable.setCurrentStock(newStock);
			
			restaurantDineable = restaurantDineableRepository.save(restaurantDineable);
			
//			this.createRestaurantDineableLog(restaurantSalesOrder.getRestaurant(), restaurantSalesOrderDetail.getDineable(), 0, restaurantSalesOrderDetail.getQty(), newStock, userService.getUser(request), dayService.getTimeStamp(), "Restaurant sale");

		}
		restaurantSaleRequest.setRestaurantSaleDetails(restaurantSaleDetails);
		
		RestaurantSale restaurantSale = restaurantSaleService.createRestaurantSale(restaurantSaleRequest, request);
		
		restaurantSalesOrder.setStatus(WorkFlowStatus.CONFIRMED);
		
		restaurantSalesOrder.setConfirmedByUser(userService.getUser(request));
		restaurantSalesOrder.setConfirmedDateTime(dayService.getTimeStamp());
		
		restaurantSalesOrderRepository.save(restaurantSalesOrder);
		
		for(RestaurantSaleDetail restaurantSaleDetail : restaurantSale.getRestaurantSaleDetails()) {
			
			double amount = (restaurantSaleDetail.getCostPriceVatIncl() * restaurantSaleDetail.getQty()) - restaurantSaleDetail.getDiscount();
			
			// Create a bill receivable
			
			BillReceivable billReceivable = new BillReceivable();
			billReceivable.setAmount(amount);
			billReceivable.setBranch(restaurantSalesOrder.getRestaurant().getBranch());
			billReceivable.setPaid(amount);
			billReceivable.setDue(0);
			billReceivable.setSummary("Dineable sale");
			billReceivable.setQty(restaurantSaleDetail.getQty());
			billReceivable.setPaidDateTime(dayService.getTimeStamp());
			billReceivable.setPayStatus(PayStatus.PAID);
			billReceivable.setNo(String.valueOf(Math.random()));
			
			billReceivable = billReceivableRepository.save(billReceivable);
			
			RestaurantSaleDetailBillReceivable restaurantSaleDetailBillReceivable = new RestaurantSaleDetailBillReceivable();
			
			restaurantSaleDetailBillReceivable.setBillReceivable(billReceivable);
			restaurantSaleDetailBillReceivable.setRestaurantSaleDetail(restaurantSaleDetail);
			restaurantSaleDetailBillReceivable.setDiscount(0);
			restaurantSaleDetailBillReceivable.setQty(restaurantSaleDetail.getQty());
			restaurantSaleDetailBillReceivable.setPrice(restaurantSaleDetail.getSellingPriceVatIncl());
			restaurantSaleDetailBillReceivableRepository.save(restaurantSaleDetailBillReceivable);
			
			Collection collection = new Collection();
			collection.setPayCode(payCode);
			collection.setPayRefNo(payRefNo);
			collection.setCollectionDateTime(dayService.getTimeStamp());
			collection.setCollectedByUser(userService.getUser(request));
			
			collection = collectionRepository.save(collection);
			
			BillReceivableCollection billReceivableCollection = new BillReceivableCollection();
			
			billReceivableCollection.setAmount(amount);
			billReceivableCollection.setPartial(false);
			billReceivableCollection.setReason("Restaurant Service");
			billReceivableCollection.setBillReceivable(billReceivable);
			billReceivableCollection.setCollection(collection);
			
			billReceivableCollectionRepository.save(billReceivableCollection);
			
		}
		
		return true;
	}
	
	@Override
	public boolean cancelRestaurantSalesOrder(Long restaurantOrderId, HttpServletRequest request) {
		RestaurantSalesOrder restaurantSalesOrder = restaurantSalesOrderRepository.findById(restaurantOrderId)
			    .orElseThrow(() -> new NotFoundException("Order not found, with id " + restaurantOrderId));
		if(!String.valueOf(restaurantSalesOrder.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		restaurantSalesOrder.setStatus(WorkFlowStatus.CANCELED);
		
		restaurantSalesOrder.setCanceledByUser(userService.getUser(request));
		restaurantSalesOrder.setCanceledDateTime(dayService.getTimeStamp());
		
		restaurantSalesOrderRepository.save(restaurantSalesOrder);
		
		return true;
	}
	
//	private boolean createRestaurantDineableLog(Restaurant restaurant, Dineable dineable, double qtyIn, double qtyOut, double balance, User createdByUser, LocalDateTime createdDateTime, String reference) {
//		// Create a restaurant dineable log
//		RestaurantDineableLog restaurantDineableLog = new RestaurantDineableLog();
//		restaurantDineableLog.setRestaurant(restaurant);
//		restaurantDineableLog.setDineable(dineable);
//		restaurantDineableLog.setQtyIn(qtyIn);
//		restaurantDineableLog.setQtyOut(qtyOut);
//		restaurantDineableLog.setBalance(balance);
//		restaurantDineableLog.setReference(reference);
//		restaurantDineableLog.setCreatedByUser(createdByUser);
//		restaurantDineableLog.setCreatedDateTime(createdDateTime);
//		restaurantDineableLogRepository.save(restaurantDineableLog);
//		return true;
//	}
}
