package com.orbix.api.modules.inventoryandprocurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SupplierProductServiceController implements SupplierProductService {

	private final CompanyRepository companyRepository;
	private final SupplierRepository supplierRepository;
	private final ProductRepository productRepository;
	private final BranchRepository branchRepository;
	private final SupplierProductRepository supplierProductRepository;
//	private final SupplierProductLogRepository supplierProductLogRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public List<SupplierProductResponseDTO> getAllSupplierProductsByBranch(Long supplierId, HttpServletRequest request) {
		// Validate and fetch the supplier
	    Supplier supplier = supplierRepository.findById(supplierId)
	                              .orElseThrow(() -> new NotFoundException("Supplier not found"));
	    
	    // Fetch all supplier products
	    List<SupplierProduct> supplierProducts = supplierProductRepository.findAllBySupplierAndBranch(supplier, userService.getUserBranch(request));

	    // Map to response DTOs using streams
	    return supplierProducts.stream()
	            .map(this::supplierProductResponseDTOMapper)
	            .collect(Collectors.toList());
	}

	@Override
	public SupplierProductResponseDTO get(Long id, Long supplierId, HttpServletRequest request) {
		// Validate and fetch the supplier
	    Supplier supplier = supplierRepository.findById(supplierId)
	                              .orElseThrow(() -> new NotFoundException("Supplier not found"));
	 // Validate and fetch the supplier
	    SupplierProduct supplierProduct = supplierProductRepository.findByIdAndSupplier(id, supplier)
	                              .orElseThrow(() -> new NotFoundException("Supplier Product not found"));
	    
	    return this.supplierProductResponseDTOMapper(supplierProduct);
	}
	
	@Override
	public SupplierProductResponseDTO getSupplierProduct(Long supplierId, Long productId, HttpServletRequest request) {
		// Validate and fetch the supplier
	    Supplier supplier = supplierRepository.findById(supplierId)
	                              .orElseThrow(() -> new NotFoundException("Supplier not found"));
	    Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
	 // Validate and fetch the supplier
	    SupplierProduct supplierProduct = supplierProductRepository.findBySupplierAndProduct(supplier, product)
	                              .orElseThrow(() -> new NotFoundException("Supplier Product not found"));
	    
	    return this.supplierProductResponseDTOMapper(supplierProduct);
	}

	@Override
	public SupplierProductResponseDTO getProductInSupplier(Long productId, Long supplierId,
			HttpServletRequest request) {
		// Validate and fetch the supplier
	    Supplier supplier = supplierRepository.findById(supplierId)
	                              .orElseThrow(() -> new NotFoundException("Supplier not found"));
	 // Validate and fetch the supplier
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Product not found"));
	    
	    SupplierProduct supplierProduct = supplierProductRepository.findByProductAndSupplier(product, supplier)
                .orElseThrow(() -> new NotFoundException("Product not found in supplier"));
	    
	    return this.supplierProductResponseDTOMapper(supplierProduct);
	}

	@Override
	public SupplierProductResponseDTO createSupplierProduct(SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request) {
		Long supplierId = supplierProductRequest.getSupplierId();
		Long productId = supplierProductRequest.getProductId();
		double vatRate = supplierProductRequest.getVatRate();
		////////////////
		double costPriceVatIncl = supplierProductRequest.getCostPriceVatIncl();
		/////////////////
		double maxSupplyQty = supplierProductRequest.getMaxSupplyQty();

		// Validate and fetch the supplier
	    Supplier supplier = supplierRepository.findById(supplierId)
	                              .orElseThrow(() -> new NotFoundException("Supplier not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Supplier Product not found"));
	    
	   
	    if (supplierProductRepository.existsBySupplierAndProduct(supplier, product)) {
	        throw new InvalidOperationException(
	            String.format("Product '%s' already exists in supplier '%s'", product.getName(), supplier.getName())
	        );
	    }
		
		
		
		
		SupplierProduct supplierProduct = new SupplierProduct();
		supplierProduct.setSupplier(supplier);
	    supplierProduct.setProduct(product);
	    supplierProduct.setMaxSupplyQty(maxSupplyQty);
	    supplierProduct.setVatRate(vatRate);
	    supplierProduct.setCostPriceVatIncl(costPriceVatIncl);
	    
	    supplierProduct.setBranch(userService.getUserBranch(request));
	    
	    supplierProduct.setActive(true);
	    
	    supplierProduct.setCreatedByUser(userService.getUser(request));
	    supplierProduct.setCreatedDateTime(dayService.getTimeStamp());
	    
	    supplierProduct = supplierProductRepository.save(supplierProduct);
		
	    return this.supplierProductResponseDTOMapper(supplierProduct);
	}

	@Override
	public SupplierProductResponseDTO updateSupplierProduct(SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request) {
		Long supplierId = supplierProductRequest.getSupplierId();
		Long productId = supplierProductRequest.getProductId();
		double vatRate = supplierProductRequest.getVatRate();
		////////////////
		double costPriceVatIncl = supplierProductRequest.getCostPriceVatIncl();

		///////////////// do not update stock
		double maxSupplyQty = supplierProductRequest.getMaxSupplyQty();
		
		// Validate and fetch the supplier
	    Supplier supplier = supplierRepository.findById(supplierId)
	                              .orElseThrow(() -> new NotFoundException("Supplier not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Supplier Product not found"));
	    
	    Optional<SupplierProduct> supplierProduct_ = supplierProductRepository.findBySupplierAndProductAndBranch(supplier, product, userService.getUserBranch(request));
	    if(supplierProduct_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Product '%s' does not exist in supplier '%s'", product.getName(), supplier.getName())
		        );
	    }
	   
		SupplierProduct supplierProduct = supplierProduct_.get();
		
		supplierProduct.setMaxSupplyQty(maxSupplyQty);
	    supplierProduct.setVatRate(vatRate);
	    supplierProduct.setCostPriceVatIncl(costPriceVatIncl);

	    supplierProduct.setActive(true);
	    
	    supplierProduct = supplierProductRepository.save(supplierProduct);
	    
	    return this.supplierProductResponseDTOMapper(supplierProduct);
	}

	@Override
	public SupplierProductResponseDTO adjustSupplierStock(SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiCustomResponse activateSupplierProduct(SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request) {
		Optional<SupplierProduct> supplierProduct_ = supplierProductRepository.findById(supplierProductRequest.getId());		
		if(supplierProduct_.isEmpty()) {
			throw new NotFoundException("Supplier Product not found");
		}		
		if(supplierProduct_.get().isActive() == false) {
			throw new InvalidOperationException("Supplier Product already inactive");
		}
		supplierProduct_.get().setActive(false);
		supplierProductRepository.save(supplierProduct_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Supplier Product Deactivated successifully");
	}

	@Override
	public ApiCustomResponse deactivateSupplierProduct(SupplierProductRequestDTO supplierProductRequest,
			HttpServletRequest request) {
		Optional<SupplierProduct> supplierProduct_ = supplierProductRepository.findById(supplierProductRequest.getId());		
		if(supplierProduct_.isEmpty()) {
			throw new NotFoundException("Supplier Product not found");
		}		
		if(supplierProduct_.get().isActive() == true) {
			throw new InvalidOperationException("Supplier Product already active");
		}
		supplierProduct_.get().setActive(true);
		supplierProductRepository.save(supplierProduct_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Supplier Product Activated successifully");
	}

	@Override
	public List<ProductResponseDTO> getProductsBySupplierAndName(Long supplierId, String productName) {
		// Validate and fetch the supplier
	    Supplier supplier = supplierRepository.findById(supplierId)
	                              .orElseThrow(() -> new NotFoundException("Supplier not found"));
		
	    List<SupplierProduct> supplierProducts = supplierProductRepository.findAllBySupplierAndProduct_NameContainingIgnoreCase(supplier, productName);
	    
	    List<ProductResponseDTO> productResponses = new ArrayList<>();
	    
	    for(SupplierProduct supplierProduct : supplierProducts) {
	    	ProductResponseDTO productResponse = new ProductResponseDTO();
	    	productResponse.setId(supplierProduct.getProduct().getId().toString());
	    	productResponse.setName(supplierProduct.getProduct().getName());
	    	productResponses.add(productResponse);
	    }
	    return productResponses;
	}
	
	private SupplierProductResponseDTO supplierProductResponseDTOMapper(SupplierProduct supplierProduct) {
		SupplierProductResponseDTO supplierProductResponse = new SupplierProductResponseDTO();
		
		supplierProductResponse.setProductCode(supplierProduct.getProduct().getCode());
		supplierProductResponse.setProductName(supplierProduct.getProduct().getName());
		supplierProductResponse.setProductDescription(supplierProduct.getProduct().getDescription());
	
		
		// Convert entity ID to String
		supplierProductResponse.setId(supplierProduct.getId().toString());

		// Format vatRate and derive vatPercentage
		supplierProductResponse.setVatRate(String.valueOf(supplierProduct.getVatRate()));
		supplierProductResponse.setMaxSupplyQty(String.valueOf(supplierProduct.getMaxSupplyQty()));

		// Convert prices to String
		supplierProductResponse.setCostPriceVatIncl(String.valueOf(supplierProduct.getCostPriceVatIncl()));

		// Convert boolean active to String
		supplierProductResponse.setActive(String.valueOf(supplierProduct.isActive()));

		// Map references to String
		supplierProductResponse.setProductId(supplierProduct.getProduct().getId().toString());
		supplierProductResponse.setSupplierId(supplierProduct.getSupplier().getId().toString());

		// Convert date/time to formatted String
		supplierProductResponse.setCreatedDateTime(supplierProduct.getCreatedDateTime().toString());
		supplierProductResponse.setCreated(supplierProduct.getCreatedDateTime().toLocalDate().toString());
		return supplierProductResponse;
	}

}
