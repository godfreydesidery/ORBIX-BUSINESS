package com.orbix.api.modules.inventoryandprocurement;

import java.time.LocalDateTime;
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
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Shop;
import com.orbix.api.modules.adminunits.ShopRepository;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShopProductServiceController implements ShopProductService {
	
	private final CompanyRepository companyRepository;
	private final ShopRepository shopRepository;
	private final ProductRepository productRepository;
	private final ShopProductRepository shopProductRepository;
	private final ShopProductLogRepository shopProductLogRepository;
	private final UserService userService;
	private final DayService dayService;

	@Override
	public List<ShopProductResponseDTO> getAllShopProducts(Long shopId, HttpServletRequest request) {
	    // Validate and fetch the shop
	    Shop shop = shopRepository.findById(shopId)
	                              .orElseThrow(() -> new NotFoundException("Shop not found"));
	    
	    // Fetch all shop products
	    List<ShopProduct> shopProducts = shopProductRepository.findAllByShop(shop);

	    // Map to response DTOs using streams
	    return shopProducts.stream()
	            .map(this::shopProductResponseDTOMapper)
	            .collect(Collectors.toList());
	}

	@Override
	public ShopProductResponseDTO get(Long id, Long shopId, HttpServletRequest request) {
		// Validate and fetch the shop
	    Shop shop = shopRepository.findById(shopId)
	                              .orElseThrow(() -> new NotFoundException("Shop not found"));
	 // Validate and fetch the shop
	    ShopProduct shopProduct = shopProductRepository.findByIdAndShop(id, shop)
	                              .orElseThrow(() -> new NotFoundException("Shop Product not found"));
	    
	    return this.shopProductResponseDTOMapper(shopProduct);
	}
	
	@Override
	public ShopProductResponseDTO getProductInShop(Long productId, Long shopId, HttpServletRequest request) {
		// Validate and fetch the shop
	    Shop shop = shopRepository.findById(shopId)
	                              .orElseThrow(() -> new NotFoundException("Shop not found"));
	 // Validate and fetch the shop
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Product not found"));
	    
	    ShopProduct shopProduct = shopProductRepository.findByProductAndShop(product, shop)
                .orElseThrow(() -> new NotFoundException("Product not found in shop"));
	    
	    return this.shopProductResponseDTOMapper(shopProduct);
	}

	@Override
	public ShopProductResponseDTO createShopProduct(ShopProductRequestDTO shopProductRequest,
			HttpServletRequest request) {
		
		Long shopId = shopProductRequest.getShopId();
		Long productId = shopProductRequest.getProductId();
		double vatRate = shopProductRequest.getVatRate();
		////////////////
		double costPriceVatIncl = shopProductRequest.getCostPriceVatIncl();
		double costPriceVatExcl = shopProductRequest.getCostPriceVatExcl();
		double sellingPriceVatIncl = shopProductRequest.getSellingPriceVatIncl();
		double sellingPriceVatExcl = shopProductRequest.getSellingPriceVatExcl();
		/////////////////
		double currentStock = shopProductRequest.getCurrentStock();
		double minStock = shopProductRequest.getMinStock();
		double maxStock = shopProductRequest.getMaxStock();
		double defaultReorderLevel = shopProductRequest.getDefaultReorderLevel();
		double defaultReorderQty = shopProductRequest.getDefaultReorderQty();
		// Validate and fetch the shop
	    Shop shop = shopRepository.findById(shopId)
	                              .orElseThrow(() -> new NotFoundException("Shop not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Shop Product not found"));
	   
	    if (shopProductRepository.existsByShopAndProduct(shop, product)) {
	        throw new InvalidOperationException(
	            String.format("Product '%s' already exists in shop '%s'", product.getName(), shop.getName())
	        );
	    }
		
		
		
		
		ShopProduct shopProduct = new ShopProduct();
		shopProduct.setShop(shop);
	    shopProduct.setProduct(product);
	    shopProduct.setCurrentStock(currentStock);
	    shopProduct.setMinStock(minStock);
	    shopProduct.setMaxStock(maxStock);
	    shopProduct.setVatRate(vatRate);
	    shopProduct.setCostPriceVatIncl(costPriceVatIncl);
	    shopProduct.setCostPriceVatExcl(costPriceVatExcl);
	    shopProduct.setSellingPriceVatIncl(sellingPriceVatIncl);
	    shopProduct.setSellingPriceVatExcl(sellingPriceVatExcl);
	    shopProduct.setDefaultReorderLevel(defaultReorderLevel);
	    shopProduct.setDefaultReorderQty(defaultReorderQty);
	    
	    shopProduct.setActive(true);
	    
	    shopProduct.setCreatedByUser(userService.getUser(request));
	    shopProduct.setCreatedDateTime(dayService.getTimeStamp());
	    
	    shopProduct = shopProductRepository.save(shopProduct);
	    
	    
	    
	    ////Update ShopProduct log for stock card
	    
	    this.createShopProductLog(shop, product, currentStock, 0, currentStock, userService.getUser(request), dayService.getTimeStamp(), "Opening stock");

		
		
	    return this.shopProductResponseDTOMapper(shopProduct);
	}

	@Override
	public ShopProductResponseDTO updateShopProduct(ShopProductRequestDTO shopProductRequest,
			HttpServletRequest request) {
		
		
		
		Long shopId = shopProductRequest.getShopId();
		Long productId = shopProductRequest.getProductId();
		double vatRate = shopProductRequest.getVatRate();
		////////////////
		double costPriceVatIncl = shopProductRequest.getCostPriceVatIncl();
		double costPriceVatExcl = shopProductRequest.getCostPriceVatExcl();
		double sellingPriceVatIncl = shopProductRequest.getSellingPriceVatIncl();
		double sellingPriceVatExcl = shopProductRequest.getSellingPriceVatExcl();
		///////////////// do not update stock
		double minStock = shopProductRequest.getMinStock();
		double maxStock = shopProductRequest.getMaxStock();
		double defaultReorderLevel = shopProductRequest.getDefaultReorderLevel();
		double defaultReorderQty = shopProductRequest.getDefaultReorderQty();
		// Validate and fetch the shop
	    Shop shop = shopRepository.findById(shopId)
	                              .orElseThrow(() -> new NotFoundException("Shop not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Shop Product not found"));
	    
	    Optional<ShopProduct> shopProduct_ = shopProductRepository.findByShopAndProduct(shop, product);
	    if(shopProduct_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Product '%s' does not exist in shop '%s'", product.getName(), shop.getName())
		        );
	    }
	   
		ShopProduct shopProduct = shopProduct_.get();
		
	    shopProduct.setMinStock(minStock);
	    shopProduct.setMaxStock(maxStock);
	    shopProduct.setVatRate(vatRate);
	    shopProduct.setCostPriceVatIncl(costPriceVatIncl);
	    shopProduct.setCostPriceVatExcl(costPriceVatExcl);
	    shopProduct.setSellingPriceVatIncl(sellingPriceVatIncl);
	    shopProduct.setSellingPriceVatExcl(sellingPriceVatExcl);
	    shopProduct.setDefaultReorderLevel(defaultReorderLevel);
	    shopProduct.setDefaultReorderQty(defaultReorderQty);
	    
	    shopProduct.setActive(true);
	    
	    shopProduct = shopProductRepository.save(shopProduct);
	    
	    
	    
	    return this.shopProductResponseDTOMapper(shopProduct);
	}
	
	
	@Override
	public ShopProductResponseDTO adjustShopStock(ShopProductRequestDTO shopProductRequest,
			HttpServletRequest request) {
		
		
		
		Long shopId = shopProductRequest.getShopId();
		Long productId = shopProductRequest.getProductId();
		
		/////////////////
		double currentStock = shopProductRequest.getCurrentStock();
		
		// Validate and fetch the shop
	    Shop shop = shopRepository.findById(shopId)
	                              .orElseThrow(() -> new NotFoundException("Shop not found"));
	    // Validate and fetch the product
	    Product product = productRepository.findById(productId)
	                              .orElseThrow(() -> new NotFoundException("Shop Product not found"));
	    
	    Optional<ShopProduct> shopProduct_ = shopProductRepository.findByShopAndProduct(shop, product);
	    if(shopProduct_.isEmpty()) {
	    	throw new InvalidOperationException(
		            String.format("Product '%s' does not exist in shop '%s'", product.getName(), shop.getName())
		        );
	    }
	   
		ShopProduct shopProduct = shopProduct_.get();
		boolean stockChanged = false;
		if(shopProduct.getCurrentStock() != currentStock) {
			stockChanged = true;
		}
	    shopProduct.setCurrentStock(currentStock);
	    
	    shopProduct.setActive(true);
	    
	    shopProduct = shopProductRepository.save(shopProduct);
	    
	    //Update ShopProduct log for stock card
	    
	    if(stockChanged == true)this.createShopProductLog(shop, product, currentStock, 0, currentStock, userService.getUser(request), dayService.getTimeStamp(), "Stock Adjustment");

	    
	    return this.shopProductResponseDTOMapper(shopProduct);
	}

	@Override
	public ApiCustomResponse activateShopProduct(ShopProductRequestDTO shopProductRequest, HttpServletRequest request) {
		Optional<ShopProduct> shopProduct_ = shopProductRepository.findById(shopProductRequest.getId());		
		if(shopProduct_.isEmpty()) {
			throw new NotFoundException("Shop Product not found");
		}		
		if(shopProduct_.get().isActive() == false) {
			throw new InvalidOperationException("Shop Product already inactive");
		}
		shopProduct_.get().setActive(false);
		shopProductRepository.save(shopProduct_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Shop Product Deactivated successifully");
	}

	@Override
	public ApiCustomResponse deactivateShopProduct(ShopProductRequestDTO shopProductRequest,
			HttpServletRequest request) {
		Optional<ShopProduct> shopProduct_ = shopProductRepository.findById(shopProductRequest.getId());		
		if(shopProduct_.isEmpty()) {
			throw new NotFoundException("Shop Product not found");
		}		
		if(shopProduct_.get().isActive() == true) {
			throw new InvalidOperationException("Shop Product already active");
		}
		shopProduct_.get().setActive(true);
		shopProductRepository.save(shopProduct_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Shop Product Activated successifully");
	}
	
	private ShopProductResponseDTO shopProductResponseDTOMapper(ShopProduct shopProduct) {
		ShopProductResponseDTO shopProductResponse = new ShopProductResponseDTO();
		
		shopProductResponse.setProductCode(shopProduct.getProduct().getCode());
		shopProductResponse.setProductName(shopProduct.getProduct().getName());
		shopProductResponse.setProductDescription(shopProduct.getProduct().getDescription());
		shopProductResponse.setBaseUom(shopProduct.getProduct().getBaseUom());
		
		// Convert entity ID to String
		shopProductResponse.setId(shopProduct.getId().toString());

		// Convert numerical values to String
		shopProductResponse.setCurrentStock(String.valueOf(shopProduct.getCurrentStock()));
		shopProductResponse.setMinStock(String.valueOf(shopProduct.getMinStock()));
		shopProductResponse.setMaxStock(String.valueOf(shopProduct.getMaxStock()));
		shopProductResponse.setDefaultReorderQty(String.valueOf(shopProduct.getDefaultReorderQty()));
		shopProductResponse.setDefaultReorderLevel(String.valueOf(shopProduct.getDefaultReorderLevel()));

		// Format vatRate and derive vatPercentage
		shopProductResponse.setVatRate(String.valueOf(shopProduct.getVatRate()));
		shopProductResponse.setVatPercentage(String.format("%.2f%%", shopProduct.getVatRate() * 100));

		// Convert prices to String
		shopProductResponse.setCostPriceVatIncl(String.valueOf(shopProduct.getCostPriceVatIncl()));
		shopProductResponse.setCostPriceVatExcl(String.valueOf(shopProduct.getCostPriceVatExcl()));
		shopProductResponse.setSellingPriceVatIncl(String.valueOf(shopProduct.getSellingPriceVatIncl()));
		shopProductResponse.setSellingPriceVatExcl(String.valueOf(shopProduct.getSellingPriceVatExcl()));

		// Convert boolean active to String
		shopProductResponse.setActive(String.valueOf(shopProduct.isActive()));

		// Map references to String
		shopProductResponse.setProductId(shopProduct.getProduct().getId().toString());
		shopProductResponse.setShopId(shopProduct.getShop().getId().toString());

		// Convert date/time to formatted String
		shopProductResponse.setCreatedDateTime(shopProduct.getCreatedDateTime().toString());
		shopProductResponse.setCreated(shopProduct.getCreatedDateTime().toLocalDate().toString());
		return shopProductResponse;
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
	@Override
	public List<ProductResponseDTO> getProductsByShopAndName(Long shopId, String productName) {
		
		// Validate and fetch the shop
	    Shop shop = shopRepository.findById(shopId)
	                              .orElseThrow(() -> new NotFoundException("Shop not found"));
		
	    List<ShopProduct> shopProducts = shopProductRepository.findAllByShopAndProduct_NameContainingIgnoreCase(shop, productName);
	    
	    List<ProductResponseDTO> productResponses = new ArrayList<>();
	    
	    for(ShopProduct shopProduct : shopProducts) {
	    	ProductResponseDTO productResponse = new ProductResponseDTO();
	    	productResponse.setId(shopProduct.getProduct().getId().toString());
	    	productResponse.setName(shopProduct.getProduct().getName());
	    	productResponses.add(productResponse);
	    }
	    return productResponses;
	}
	
	

}
