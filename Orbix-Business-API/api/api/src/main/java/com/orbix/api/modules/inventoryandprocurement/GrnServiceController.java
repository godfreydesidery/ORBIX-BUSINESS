package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.modules.adminunits.DayService;
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
public class GrnServiceController implements GrnService {
	
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
	public List<GrnResponseDTO> getAllGrns(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GrnResponseDTO> getAllPendingGrns(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GrnResponseDTO> getAllVisibleGrnsByBranch(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GrnResponseDTO get(Long id, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GrnResponseDTO createGrn(GrnRequestDTO grnRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GrnResponseDTO updateGrn(GrnRequestDTO grnRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GrnDetailResponseDTO> getAllGrnDetails(Long grnId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createGrnDetail(GrnDetailRequestDTO grnDetailRequest, HttpServletRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGrnDetail(Long grnDetailId, Long grnId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean approveGrn(Long grnId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancelGrn(Long grnId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return false;
	}
}
