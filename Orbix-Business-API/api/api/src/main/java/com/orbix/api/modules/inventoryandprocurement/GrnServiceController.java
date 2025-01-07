package com.orbix.api.modules.inventoryandprocurement;

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
import com.orbix.api.modules.finance.BillReceivableCollectionRepository;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.finance.CollectionRepository;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;
import com.orbix.api.modules.salesandmarketing.SaleDetailBillReceivableRepository;
import com.orbix.api.modules.salesandmarketing.SaleRepository;
import com.orbix.api.modules.salesandmarketing.SaleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GrnServiceController implements GrnService {
	
	private final GrnRepository grnRepository;
	private final LpoRepository lpoRepository;
	private final GrnDetailRepository grnDetailRepository;
	private final ShopRepository shopRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final ShopProductRepository shopProductRepository;
	private final ProductRepository productRepository;
	
	private final SaleService saleService;
	private final ShopProductLogRepository shopProductLogRepository;
	
	private final BillReceivableRepository billReceivableRepository;
	private final SaleDetailBillReceivableRepository saleDetailBillReceivableRepository;
	
	private final CollectionRepository collectionRepository;
	private final BillReceivableCollectionRepository billReceivableCollectionRepository;
	
	private final SaleRepository saleRepository;
	
	private final SupplierRepository supplierRepository;
	private final SupplierProductRepository supplierProductRepository;
	
	@Override
	public List<GrnResponseDTO> getAllGrns(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GrnResponseDTO> getAllPendingGrns(HttpServletRequest request) {
//		Shop shop = shopRepository.findById(shopId)
//	    .orElseThrow(() -> new NotFoundException("Shop not found, with id " + shopId));

	List<Grn> grns = grnRepository.findAllByStatus(WorkFlowStatus.PENDING);

	return grns.stream()
	    .map(this::grnResponseDTOMapper)
	    .collect(Collectors.toList());
	}

	@Override
	public List<GrnResponseDTO> getAllVisibleGrnsByBranch(HttpServletRequest request) {
		List<WorkFlowStatus> statuses = new ArrayList<>();
		statuses.add(WorkFlowStatus.PENDING);
		//statuses.add(WorkFlowStatus.PROCESSING);
		statuses.add(WorkFlowStatus.APPROVED);

			List<Grn> grns = grnRepository.findAllByStatusInAndBranch(statuses, userService.getUserBranch(request));

			return grns.stream()
			    .map(this::grnResponseDTOMapper)
			    .collect(Collectors.toList());
	}
	
	@Override
	public List<GrnResponseDTO> getAllVisibleGrnsByShop(Long shopId, HttpServletRequest request) {
		
		Optional<Shop> shop_ = shopRepository.findById(shopId);
		if(shop_.isEmpty()) {
			throw new NotFoundException("Shop not found");
		}
		
		List<WorkFlowStatus> statuses = new ArrayList<>();
		statuses.add(WorkFlowStatus.PENDING);
		statuses.add(WorkFlowStatus.PROCESSING);
		statuses.add(WorkFlowStatus.APPROVED);
		statuses.add(WorkFlowStatus.COMPLETED);

			List<Grn> grns = grnRepository.findAllByStatusInAndBranchAndShop(statuses, userService.getUserBranch(request), shop_.get());

			return grns.stream()
			    .map(this::grnResponseDTOMapper)
			    .collect(Collectors.toList());
	}

	@Override
	public GrnResponseDTO get(Long id, HttpServletRequest request) {
		return grnRepository.findById(id)
			    .map(this::grnResponseDTOMapperWithDetails)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with ID " + id));
	}

	@Override
	public GrnResponseDTO createGrn(GrnRequestDTO grnRequest, HttpServletRequest request) {
		Shop shop = null;
		if(grnRequest.getShopId() == null) {
			throw new InvalidOperationException("No destination information provided. Please select destination.");
		}
		
		shop = shopRepository.findById(grnRequest.getShopId())
			    .orElseThrow(() -> new NotFoundException("Shop not found, with id " + grnRequest.getShopId()));
		
//		Supplier supplier = supplierRepository.findById(grnRequest.getSupplierId())
//				.orElseThrow(() -> new NotFoundException("Supplier not found, with id " + grnRequest.getSupplierId()));
//		
		Grn grn = new Grn();
		
		grn.setNo(String.valueOf(Math.random()));
		grn.setShop(shop);
//		grn.setSupplier(supplier);
		grn.setBranch(userService.getUserBranch(request));
		grn.setStatus(WorkFlowStatus.PENDING);
//		grn.setSummary(grnRequest.getSummary());
		grn.setCreatedByUser(userService.getUser(request));
		grn.setCreatedDateTime(dayService.getTimeStamp());
		grn = grnRepository.save(grn);
		grn.setNo(grn.getId().toString());
		
		grn = grnRepository.save(grn);
		
		return grnResponseDTOMapper(grn);
	}
	
	@Override
	public GrnResponseDTO createGrnByLpoNo(String lpoNo, HttpServletRequest request) {
		Shop shop = null;
		
		Optional<Lpo> lpo_ = lpoRepository.findByNo(lpoNo);
		if(lpo_.isEmpty()) {
			throw new NotFoundException("LPO not found");
		}
		if(!String.valueOf(lpo_.get().getStatus()).equals("APPROVED")) {
			throw new InvalidOperationException("LPO not approved");
		}

		if(lpo_.get().getShop() == null) {
			throw new InvalidOperationException("LPO do not belong to shop");
		}	
		
		Lpo lpo = lpo_.get();
		lpo.setStatus(WorkFlowStatus.COMPLETED);
		lpo = lpoRepository.save(lpo);
		
		shop = lpo.getShop();
				

		Grn grn = new Grn();
		
		grn.setNo(String.valueOf(Math.random()));
		grn.setShop(shop);
		grn.setBranch(userService.getUserBranch(request));
		grn.setStatus(WorkFlowStatus.PROCESSING);
//		grn.setSummary(grnRequest.getSummary());
		grn.setCreatedByUser(userService.getUser(request));
		grn.setCreatedDateTime(dayService.getTimeStamp());
		grn = grnRepository.save(grn);
		grn.setNo(grn.getId().toString());
		
		grn = grnRepository.save(grn);
		
		List<GrnDetail> grnDetails = new ArrayList<>();
		for(LpoDetail lpoDetail : lpo_.get().getLpoDetails()) {
			GrnDetail grnDetail = new GrnDetail();
			
			
			if(lpoDetail.getQty() <= 0) {
				throw new InvalidOperationException("Invalid quantiy selected");
			}
						
			grnDetail.setProduct(lpoDetail.getProduct());
			grnDetail.setQty(lpoDetail.getQty());
			grnDetail.setCostPriceVatIncl(lpoDetail.getCostPriceVatIncl());
			grnDetail.setVatRate(lpoDetail.getVatRate());
			grnDetail.setGrn(grn);
			
			grnDetail.setCreatedByUser(userService.getUser(request));
			grnDetail.setCreatedDateTime(dayService.getTimeStamp());

			grnDetail = grnDetailRepository.save(grnDetail);
			grnDetails.add(grnDetail);
			
		}
		
		grn.setGrnDetails(grnDetails);
		
		return grnResponseDTOMapperWithDetails(grn);
	}

	@Override
	public GrnResponseDTO updateGrn(GrnRequestDTO grnRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GrnDetailResponseDTO> getAllGrnDetails(Long grnId, HttpServletRequest request) {
		Grn grn = grnRepository.findById(grnId)
			    .orElseThrow(() -> new NotFoundException("GRN not found, with id " + grnId));
		
		List<GrnDetail> grnDetails = grnDetailRepository.findAllByGrn(grn);
		
		List<GrnDetailResponseDTO> grnDetailResponses = new ArrayList<>();
		
		for(GrnDetail grnDetail : grnDetails) {
			grnDetailResponses.add(grnDetailResponseDTOMapper(grnDetail));
		}
		
		return grnDetailResponses;
	}

	@Override
	public void createGrnDetail(GrnDetailRequestDTO grnDetailRequest, HttpServletRequest request) {
		Grn grn = grnRepository.findById(grnDetailRequest.getGrnId())
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + grnDetailRequest.getGrnId()));
		
		if(!String.valueOf(grn.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending GRN");
		}
		
		Product product = productRepository.findById(grnDetailRequest.getProductId()).get();
		
//		SupplierProduct supplierProduct = supplierProductRepository.findBySupplierAndProduct(grn.getSupplier(), product)
//				.orElseThrow(() -> new NotFoundException("Supplier Product not found"));
//		
		GrnDetail grnDetail = new GrnDetail();
		
//		grnDetail.setCostPriceVatIncl(supplierProduct.getCostPriceVatIncl());
//		grnDetail.setVatRate(supplierProduct.getVatRate());
		grnDetail.setProduct(product);
		if(grnDetailRequest.getQty() <= 0) {
			throw new InvalidOperationException("Invalid quantiy selected");
		}
		
		if(grnDetailRepository.existsByGrnAndProduct(grn, product)) {
			throw new InvalidOperationException("Product already present in LPO");
		}
		grnDetail.setQty(grnDetailRequest.getQty());
		grnDetail.setCostPriceVatIncl(grnDetailRequest.getCostPriceVatIncl());
		grnDetail.setVatRate(grnDetailRequest.getVatRate());
		grnDetail.setGrn(grn);
		
		grnDetail.setCreatedByUser(userService.getUser(request));
		grnDetail.setCreatedDateTime(dayService.getTimeStamp());

		
		grnDetail = grnDetailRepository.save(grnDetail);
		
	}

	@Override
	public void removeGrnDetail(Long grnDetailId, Long grnId, HttpServletRequest request) {
		Grn grn = grnRepository.findById(grnId)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + grnId));
		if(!String.valueOf(grn.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending GRN");
		}
		
		GrnDetail grnDetail = grnDetailRepository.findById(grnDetailId)
			    .orElseThrow(() -> new NotFoundException("Detail not found, with id " + grnDetailId));
		
		if(grnDetail.getGrn().getId() != grnId) {
			throw new InvalidOperationException("Detail do not belong to this sales order");
		}
		
		grnDetailRepository.delete(grnDetail);	
		
	}

	@Override
	public boolean approveGrn(Long grnId, HttpServletRequest request) {
		Grn grn = grnRepository.findById(grnId)
			    .orElseThrow(() -> new NotFoundException("GRN not found, with id " + grnId));
		if(!(String.valueOf(grn.getStatus()).equals("PENDING") || String.valueOf(grn.getStatus()).equals("PROCESSING"))) {
			throw new InvalidOperationException("Not a pending or processing GRN");
		}
		
		if(grn.getGrnDetails().isEmpty()) {
			throw new InvalidOperationException("Can not approve an empty GRN");
		}
		
		grn.setStatus(WorkFlowStatus.APPROVED);
		
		grn.setApprovedByUser(userService.getUser(request));
		grn.setApprovedDateTime(dayService.getTimeStamp());
		
		grn = grnRepository.save(grn);
		
		// Now update shop stock, if it is a shop grn
		
		Shop shop = null;
		
		if(grn.getShop() != null) {
			shop = grn.getShop();
			for(GrnDetail grnDetail : grn.getGrnDetails()) {
			
				ShopProduct shopProduct = shopProductRepository.findByProductAndShop(grnDetail.getProduct(), shop).orElseThrow();
				
				double newStock = shopProduct.getCurrentStock() + grnDetail.getQty();
				
				shopProduct.setCurrentStock(newStock);
				
				shopProduct = shopProductRepository.save(shopProduct);
				
				this.createShopProductLog(shop, grnDetail.getProduct(), grnDetail.getQty(), 0, newStock, userService.getUser(request), dayService.getTimeStamp(), "GRN: " + grn.getNo());

			}
		}
		
		return true;
	}
	
	@Override
	public boolean archiveGrn(Long grnId, HttpServletRequest request) {
		Grn grn = grnRepository.findById(grnId)
			    .orElseThrow(() -> new NotFoundException("GRN not found, with id " + grnId));
		if(!String.valueOf(grn.getStatus()).equals("APPROVED")) {
			throw new InvalidOperationException("GRN not approved");
		}
		
		grn.setStatus(WorkFlowStatus.ARCHIVED);
		
		grn.setArchivedByUser(userService.getUser(request));
		grn.setArchivedDateTime(dayService.getTimeStamp());
		
		grnRepository.save(grn);
		
		return true;
	}

	@Override
	public boolean cancelGrn(Long grnId, HttpServletRequest request) {
		Grn grn = grnRepository.findById(grnId)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + grnId));
		if(!String.valueOf(grn.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending GRN");
		}
		
		grn.setStatus(WorkFlowStatus.CANCELED);
		
		grn.setCanceledByUser(userService.getUser(request));
		grn.setCanceledDateTime(dayService.getTimeStamp());
		
		grnRepository.save(grn);
		
		return true;
	}
	
	private GrnResponseDTO grnResponseDTOMapper(Grn grn) {
		GrnResponseDTO grnResponse = new GrnResponseDTO();
		
		grnResponse.setId(grn.getId().toString());
		grnResponse.setNo(grn.getNo());
		if (grn.getShop() != null && grn.getShop().getId() != null) {
		    grnResponse.setShopId(grn.getShop().getId().toString());
		} else {
		    grnResponse.setShopId(null); // or a default value
		}
//		grnResponse.setSupplierId(grn.getSupplier().getId().toString());		
//		grnResponse.setSupplierCode(grn.getSupplier().getCode());	
//		grnResponse.setSupplierName(grn.getSupplier().getName());
		grnResponse.setStatus(grn.getStatus().toString());
		
		return grnResponse;
	}
	
	private GrnResponseDTO grnResponseDTOMapperWithDetails(Grn grn) {
		GrnResponseDTO grnResponse = new GrnResponseDTO();
		List<GrnDetailResponseDTO> grnDetailResponses = new ArrayList<>();
		
		grnResponse.setId(grn.getId().toString());
		grnResponse.setNo(grn.getNo());
		if (grn.getShop() != null && grn.getShop().getId() != null) {
		    grnResponse.setShopId(grn.getShop().getId().toString());
		    grnResponse.setShopCode(grn.getShop().getCode());
		    grnResponse.setShopName(grn.getShop().getName());
		} else {
		    grnResponse.setShopId(""); // or a default value
		    grnResponse.setShopCode("");
		    grnResponse.setShopName("");
		}
		if (grn.getLpo() != null) {
		    grnResponse.setSupplierId(grn.getLpo().getSupplier().getId().toString());
		    grnResponse.setSupplierCode(grn.getLpo().getSupplier().getCode());
		    grnResponse.setSupplierName(grn.getLpo().getSupplier().getName());
		} else {
			grnResponse.setSupplierId("");
		    grnResponse.setSupplierCode("");
		    grnResponse.setSupplierName("");
		}
		grnResponse.setStatus(grn.getStatus().toString());
		
		for(GrnDetail grnDetail : grn.getGrnDetails()) {
			grnDetailResponses.add(grnDetailResponseDTOMapper(grnDetail));
		}
		grnResponse.setGrnDetails(grnDetailResponses);
		
		return grnResponse;
	}
	
	private GrnDetailResponseDTO grnDetailResponseDTOMapper(GrnDetail grnDetail) {
		GrnDetailResponseDTO grnDetailResponse = new GrnDetailResponseDTO();
		
		grnDetailResponse.setId(grnDetail.getId().toString());
		grnDetailResponse.setCostPriceVatIncl(String.valueOf(grnDetail.getCostPriceVatIncl()));
		grnDetailResponse.setVatRate(String.valueOf(grnDetail.getVatRate()));
		grnDetailResponse.setQty(String.valueOf(grnDetail.getQty()));
		grnDetailResponse.setProductId(grnDetail.getProduct().getId().toString());
		grnDetailResponse.setProductCode(grnDetail.getProduct().getCode());
		grnDetailResponse.setProductName(grnDetail.getProduct().getName());
		grnDetailResponse.setProductDescription(grnDetail.getProduct().getDescription());
		grnDetailResponse.setBaseUom(grnDetail.getProduct().getBaseUom());
		grnDetailResponse.setAmount(String.valueOf(grnDetail.getCostPriceVatIncl() * grnDetail.getQty()));
		
		return grnDetailResponse;
	}
	
	// to be taken to a service
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
