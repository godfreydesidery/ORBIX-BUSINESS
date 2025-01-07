package com.orbix.api.modules.inventoryandprocurement;

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
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Shop;
import com.orbix.api.modules.adminunits.ShopRepository;
import com.orbix.api.modules.finance.BillReceivableCollectionRepository;
import com.orbix.api.modules.finance.BillReceivableRepository;
import com.orbix.api.modules.finance.CollectionRepository;
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
public class LpoServiceController implements LpoService {
	
	private final LpoRepository lpoRepository;
	private final LpoDetailRepository lpoDetailRepository;
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
	public List<LpoResponseDTO> getAllLpos(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LpoResponseDTO> getAllPendingLpos(HttpServletRequest request) {
//		Shop shop = shopRepository.findById(shopId)
//			    .orElseThrow(() -> new NotFoundException("Shop not found, with id " + shopId));

			List<Lpo> lpos = lpoRepository.findAllByStatus(WorkFlowStatus.PENDING);

			return lpos.stream()
			    .map(this::lpoResponseDTOMapper)
			    .collect(Collectors.toList());
	}
	
	@Override
	public List<LpoResponseDTO> getAllVisibleLposByBranch(HttpServletRequest request) {
		
		List<WorkFlowStatus> statuses = new ArrayList<>();
		statuses.add(WorkFlowStatus.PENDING);
		statuses.add(WorkFlowStatus.PROCESSING);
		statuses.add(WorkFlowStatus.APPROVED);

			List<Lpo> lpos = lpoRepository.findAllByStatusInAndBranch(statuses, userService.getUserBranch(request));

			return lpos.stream()
			    .map(this::lpoResponseDTOMapper)
			    .collect(Collectors.toList());
	}
	
	@Override
	public List<LpoResponseDTO> getAllVisibleLposByShop(Long shopId, HttpServletRequest request) {
		
		Optional<Shop> shop_ = shopRepository.findById(shopId);
		if(shop_.isEmpty()) {
			throw new NotFoundException("Shop not found");
		}
		
		List<WorkFlowStatus> statuses = new ArrayList<>();
		statuses.add(WorkFlowStatus.PENDING);
		statuses.add(WorkFlowStatus.PROCESSING);
		statuses.add(WorkFlowStatus.APPROVED);
		statuses.add(WorkFlowStatus.COMPLETED);

			List<Lpo> lpos = lpoRepository.findAllByStatusInAndBranchAndShop(statuses, userService.getUserBranch(request), shop_.get());

			return lpos.stream()
			    .map(this::lpoResponseDTOMapper)
			    .collect(Collectors.toList());
	}

	@Override
	public LpoResponseDTO get(Long id, HttpServletRequest request) {
		return lpoRepository.findById(id)
			    .map(this::lpoResponseDTOMapperWithDetails)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with ID " + id));
	}

	@Override
	public LpoResponseDTO createLpo(LpoRequestDTO lpoRequest, HttpServletRequest request) {
		Shop shop = null;
		if(lpoRequest.getShopId() == null) {
			throw new InvalidOperationException("No destination information provided. Please select destination.");
		}
		
		shop = shopRepository.findById(lpoRequest.getShopId())
			    .orElseThrow(() -> new NotFoundException("Shop not found, with id " + lpoRequest.getShopId()));
		
		Supplier supplier = supplierRepository.findById(lpoRequest.getSupplierId())
	    .orElseThrow(() -> new NotFoundException("Supplier not found, with id " + lpoRequest.getSupplierId()));
		
		Lpo lpo = new Lpo();
		
		lpo.setNo(String.valueOf(Math.random()));
		lpo.setShop(shop);
		lpo.setSupplier(supplier);
		lpo.setBranch(userService.getUserBranch(request));
		lpo.setStatus(WorkFlowStatus.PENDING);
//		lpo.setSummary(lpoRequest.getSummary());
		lpo.setCreatedByUser(userService.getUser(request));
		lpo.setCreatedDateTime(dayService.getTimeStamp());
		lpo = lpoRepository.save(lpo);
		lpo.setNo(lpo.getId().toString());
		
		lpo = lpoRepository.save(lpo);
		
		return lpoResponseDTOMapper(lpo);
	}

	@Override
	public LpoResponseDTO updateLpo(LpoRequestDTO lpoRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LpoDetailResponseDTO> getAllLpoDetails(Long lpoId, HttpServletRequest request) {
		Lpo lpo = lpoRepository.findById(lpoId)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + lpoId));
		
		List<LpoDetail> lpoDetails = lpoDetailRepository.findAllByLpo(lpo);
		
		List<LpoDetailResponseDTO> lpoDetailResponses = new ArrayList<>();
		
		for(LpoDetail lpoDetail : lpoDetails) {
			lpoDetailResponses.add(lpoDetailResponseDTOMapper(lpoDetail));
		}
		
		return lpoDetailResponses;
	}

	@Override
	public void createLpoDetail(LpoDetailRequestDTO lpoDetailRequest, HttpServletRequest request) {
		Lpo lpo = lpoRepository.findById(lpoDetailRequest.getLpoId())
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + lpoDetailRequest.getLpoId()));
		
		if(!String.valueOf(lpo.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		Product product = productRepository.findById(lpoDetailRequest.getProductId()).get();
		
		SupplierProduct supplierProduct = supplierProductRepository.findBySupplierAndProduct(lpo.getSupplier(), product)
				.orElseThrow(() -> new NotFoundException("Supplier Product not found"));
		
		LpoDetail lpoDetail = new LpoDetail();
		
		lpoDetail.setCostPriceVatIncl(supplierProduct.getCostPriceVatIncl());
		lpoDetail.setVatRate(supplierProduct.getVatRate());
		lpoDetail.setProduct(product);
		if(lpoDetailRequest.getQty() <= 0) {
			throw new InvalidOperationException("Invalid quantiy selected");
		}
		
		if(lpoDetailRepository.existsByLpoAndProduct(lpo, product)) {
			throw new InvalidOperationException("Product already present in LPO");
		}
		lpoDetail.setQty(lpoDetailRequest.getQty());
		lpoDetail.setLpo(lpo);
		
		lpoDetail.setCreatedByUser(userService.getUser(request));
		lpoDetail.setCreatedDateTime(dayService.getTimeStamp());

		
		lpoDetail = lpoDetailRepository.save(lpoDetail);
	}

	@Override
	public void removeLpoDetail(Long lpoDetailId, Long lpoId, HttpServletRequest request) {
		Lpo lpo = lpoRepository.findById(lpoId)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + lpoId));
		if(!String.valueOf(lpo.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending order");
		}
		
		LpoDetail lpoDetail = lpoDetailRepository.findById(lpoDetailId)
			    .orElseThrow(() -> new NotFoundException("Detail not found, with id " + lpoDetailId));
		
		if(lpoDetail.getLpo().getId() != lpoId) {
			throw new InvalidOperationException("Detail do not belong to this sales order");
		}
		
		lpoDetailRepository.delete(lpoDetail);	
	}

	@Override
	public boolean approveLpo(Long lpoId, HttpServletRequest request) {
		Lpo lpo = lpoRepository.findById(lpoId)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + lpoId));
		if(!String.valueOf(lpo.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending LPO");
		}
		
		if(lpo.getLpoDetails().isEmpty()) {
			throw new InvalidOperationException("Can not approve an empty LPO");
		}
		
		lpo.setStatus(WorkFlowStatus.APPROVED);
		
		lpo.setApprovedByUser(userService.getUser(request));
		lpo.setApprovedDateTime(dayService.getTimeStamp());
		
		lpoRepository.save(lpo);
		
		return true;
	}

	@Override
	public boolean cancelLpo(Long lpoId, HttpServletRequest request) {
		Lpo lpo = lpoRepository.findById(lpoId)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + lpoId));
		if(!String.valueOf(lpo.getStatus()).equals("PENDING")) {
			throw new InvalidOperationException("Not a pending LPO");
		}
		
		lpo.setStatus(WorkFlowStatus.CANCELED);
		
		lpo.setCanceledByUser(userService.getUser(request));
		lpo.setCanceledDateTime(dayService.getTimeStamp());
		
		lpoRepository.save(lpo);
		
		return true;
	}
	
	@Override
	public boolean archiveLpo(Long lpoId, HttpServletRequest request) {
		Lpo lpo = lpoRepository.findById(lpoId)
			    .orElseThrow(() -> new NotFoundException("LPO not found, with id " + lpoId));
		if(!String.valueOf(lpo.getStatus()).equals("APPROVED")) {
			throw new InvalidOperationException("LPO not approved");
		}
		
		lpo.setStatus(WorkFlowStatus.ARCHIVED);
		
		lpo.setArchivedByUser(userService.getUser(request));
		lpo.setArchivedDateTime(dayService.getTimeStamp());
		
		lpoRepository.save(lpo);
		
		return true;
	}
	
	private LpoResponseDTO lpoResponseDTOMapper(Lpo lpo) {
		LpoResponseDTO lpoResponse = new LpoResponseDTO();
		
		lpoResponse.setId(lpo.getId().toString());
		lpoResponse.setNo(lpo.getNo());
		if (lpo.getShop() != null && lpo.getShop().getId() != null) {
		    lpoResponse.setShopId(lpo.getShop().getId().toString());
		} else {
		    lpoResponse.setShopId(null); // or a default value
		}
		lpoResponse.setSupplierId(lpo.getSupplier().getId().toString());		
		lpoResponse.setSupplierCode(lpo.getSupplier().getCode());	
		lpoResponse.setSupplierName(lpo.getSupplier().getName());
		lpoResponse.setStatus(lpo.getStatus().toString());
		
		return lpoResponse;
	}
	
	private LpoResponseDTO lpoResponseDTOMapperWithDetails(Lpo lpo) {
		LpoResponseDTO lpoResponse = new LpoResponseDTO();
		List<LpoDetailResponseDTO> lpoDetailResponses = new ArrayList<>();
		
		lpoResponse.setId(lpo.getId().toString());
		lpoResponse.setNo(lpo.getNo());
		if (lpo.getShop() != null && lpo.getShop().getId() != null) {
		    lpoResponse.setShopId(lpo.getShop().getId().toString());
		    lpoResponse.setShopCode(lpo.getShop().getCode());
		    lpoResponse.setShopName(lpo.getShop().getName());
		} else {
		    lpoResponse.setShopId(""); // or a default value
		    lpoResponse.setShopCode("");
		    lpoResponse.setShopName("");
		}
		lpoResponse.setSupplierId(lpo.getSupplier().getId().toString());		
		lpoResponse.setSupplierCode(lpo.getSupplier().getCode());
		lpoResponse.setSupplierName(lpo.getSupplier().getName());
		lpoResponse.setStatus(lpo.getStatus().toString());
		
		for(LpoDetail lpoDetail : lpo.getLpoDetails()) {
			lpoDetailResponses.add(lpoDetailResponseDTOMapper(lpoDetail));
		}
		lpoResponse.setLpoDetails(lpoDetailResponses);
		
		return lpoResponse;
	}
	
	private LpoDetailResponseDTO lpoDetailResponseDTOMapper(LpoDetail lpoDetail) {
		LpoDetailResponseDTO lpoDetailResponse = new LpoDetailResponseDTO();
		
		lpoDetailResponse.setId(lpoDetail.getId().toString());
		lpoDetailResponse.setCostPriceVatIncl(String.valueOf(lpoDetail.getCostPriceVatIncl()));
		lpoDetailResponse.setVatRate(String.valueOf(lpoDetail.getVatRate()));
		lpoDetailResponse.setQty(String.valueOf(lpoDetail.getQty()));
		lpoDetailResponse.setProductId(lpoDetail.getProduct().getId().toString());
		lpoDetailResponse.setProductCode(lpoDetail.getProduct().getCode());
		lpoDetailResponse.setProductName(lpoDetail.getProduct().getName());
		lpoDetailResponse.setProductDescription(lpoDetail.getProduct().getDescription());
		lpoDetailResponse.setBaseUom(lpoDetail.getProduct().getBaseUom());
		lpoDetailResponse.setAmount(String.valueOf(lpoDetail.getCostPriceVatIncl() * lpoDetail.getQty()));
		
		return lpoDetailResponse;
	}

}
