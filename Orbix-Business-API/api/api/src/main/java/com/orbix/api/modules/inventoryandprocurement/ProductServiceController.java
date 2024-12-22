package com.orbix.api.modules.inventoryandprocurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Shop;
import com.orbix.api.modules.adminunits.ShopRepository;
import com.orbix.api.modules.adminunits.ShopService;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceController implements ProductService {
	private final CompanyRepository companyRepository;
	private final ProductRepository productRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final ShopRepository shopRepository;
	
	private final ShopProductRepository shopProductRepository;
	
	/**
	 * 
	 */
	@Override
	public List<ProductResponseDTO> getAllProductes(HttpServletRequest request) {
		List<Product> productes = productRepository.findAll();
		List<ProductResponseDTO> productResponses = new ArrayList<>();

		for(Product product : productes) {
			productResponses.add(productResponseDTOMapper(product));					
		}		
		return productResponses;
	}

	/**
	 * 
	 */
	@Override
	public ProductResponseDTO get(
			Long id, 
			HttpServletRequest request) {
		Optional<Product> _product = productRepository.findById(id);
		if(_product.isEmpty()) {
			throw new NotFoundException("Product not found");
		}		
		return productResponseDTOMapper(_product.get());	
	}
	
	/**
	 * 
	 */
	@Override
	public ProductResponseDTO createProduct(
			ProductRequestDTO productRequest, 
			HttpServletRequest request) {
		
		Optional<Company> company_ = companyRepository.findById(userService.getUser(request).getCompany().getId());
		if(company_.isEmpty()) {
			throw new InvalidEntryException("Company not found in database");
		}
		
		Product product = new Product();
		//product.setCode(productRequest.getCode());
		product.setCode(String.valueOf(Math.random()));
		product.setName(productRequest.getName());
		product.setDescription(productRequest.getDescription());
		product.setBaseUom(productRequest.getBaseUom());		
		product.setCompany(company_.get());
		
		product.setSellable(true);
		
		product.setCreatedByUser(userService.getUser(request));
		product.setCreatedDateTime(dayService.getTimeStamp());
		
		product = productRepository.save(product);
		/**Create  company code*/
		product.setCode("AC/"+ product.getId().toString());
		product = productRepository.save(product);
		
		return productResponseDTOMapper(product);
	}

	@Override
	public ProductResponseDTO updateProduct(ProductRequestDTO productRequest, HttpServletRequest request) {

		Optional<Product> product_ = productRepository.findById(productRequest.getId());
		if(product_.isEmpty()) {
			throw new NotFoundException("Product not be found in database");
		}		
		if(!validateProductData(productRequest)) {
			throw new InvalidEntryException("Could not validate product data");
		}		
		Product product = product_.get();
		product.setName(productRequest.getName());
		product.setDescription(productRequest.getDescription());
		product.setBaseUom(productRequest.getBaseUom());
		
		product.setSellable(true);
		
		
		product = productRepository.save(product);		
		return productResponseDTOMapper(product);
	}
	
	private ProductResponseDTO productResponseDTOMapper(Product product) {	
		ProductResponseDTO productResponse = new ProductResponseDTO();
		productResponse.setId(product.getId().toString());
		productResponse.setCode(product.getCode());
		productResponse.setName(product.getName());
		productResponse.setDescription(product.getDescription());
		productResponse.setBaseUom(product.getBaseUom());
		productResponse.setCompanyId(product.getCompany().getId().toString());		
		if(product.isActive()) {
			productResponse.setActive("Active");
		}else {
			productResponse.setActive("Inactive");
		}
		
		//productResponse.setOtherInfo("Company: " + product.getCompany().getName() + " Location: " + product.getLocationName());
		return productResponse;
	}
	
	private ProductResponseDTO productResponseDTOMapperWithImportedStatus(Product product, boolean importedToShop) {	
		ProductResponseDTO productResponse = new ProductResponseDTO();
		productResponse.setId(product.getId().toString());
		productResponse.setCode(product.getCode());
		productResponse.setName(product.getName());
		productResponse.setDescription(product.getDescription());
		productResponse.setBaseUom(product.getBaseUom());
		productResponse.setCompanyId(product.getCompany().getId().toString());		
		if(product.isActive()) {
			productResponse.setActive("Active");
		}else {
			productResponse.setActive("Inactive");
		}
		if(importedToShop == true) {
			productResponse.setImported("1");
		}else {
			productResponse.setImported("0");
		}
		
		//productResponse.setOtherInfo("Company: " + product.getCompany().getName() + " Location: " + product.getLocationName());
		return productResponse;
	}
	
	/**
	 * 
	 */
	@Override
	public ApiCustomResponse activateProduct(ProductRequestDTO product, HttpServletRequest request) {
		Optional<Product> product_ = productRepository.findById(product.getId());		
		if(product_.isEmpty()) {
			throw new NotFoundException("Product not found");
		}		
		if(product_.get().isActive() == true) {
			throw new InvalidOperationException("Product already active");
		}
		product_.get().setActive(true);
		productRepository.save(product_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Product Activated successifully");
	}

	/**
	 * 
	 */
	@Override
	public ApiCustomResponse deactivateProduct(ProductRequestDTO product, HttpServletRequest request) {
		Optional<Product> product_ = productRepository.findById(product.getId());		
		if(product_.isEmpty()) {
			throw new NotFoundException("Product not found");
		}		
		if(product_.get().isActive() == false) {
			throw new InvalidOperationException("Product already inactive");
		}
		product_.get().setActive(false);
		productRepository.save(product_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Product Deactivated successifully");
	}
	
	/**
	 * 
	 * @param productRequest
	 * @return
	 */
	boolean validateProductData(ProductRequestDTO productRequest) {
		
		return true;
	}
	
	@Override
	public List<ProductResponseDTO> getProductsByCompany(String productName, HttpServletRequest request) {
	    // Validate input
	    if (productName == null || productName.trim().isEmpty()) {
	        throw new IllegalArgumentException("Product name cannot be null or empty");
	    }

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }

	    // Fetch products
	    List<Product> products = productRepository.findAllByCompanyAndNameContainingIgnoreCase(company, productName);

	    // Map to DTOs
	    return products.stream()
	        .map(this::productResponseDTOMapper)
	        .collect(Collectors.toList());
	}
	
	
	
	@Override
	public List<ProductResponseDTO> getCompanySellableProducts(HttpServletRequest request) {
	    

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }

	    // Fetch products
	    List<Product> products = productRepository.findAllByCompanyAndSellable(company, true);

	    // Map to DTOs
	    return products.stream()
	        .map(this::productResponseDTOMapper)
	        .collect(Collectors.toList());
	}
	
	@Override
	public List<ProductResponseDTO> getCompanySellableProductsByShop(Long shopId, HttpServletRequest request) {
	    

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }
	    Shop shop = shopRepository.findById(shopId)
	    	    .orElseThrow(() -> new NotFoundException("Shop not found"));

	    // Fetch products
	    List<Product> products = productRepository.findAllByCompanyAndSellable(company, true);
	    List<ProductResponseDTO> productResponses = new ArrayList<>();
	    
	    for(Product product : products) {
	    	boolean imported = false;
	    	Optional<ShopProduct> shopProduct_ = shopProductRepository.findByProductAndShop(product, shop);
	    	if(shopProduct_.isPresent()) {
	    		imported = true;
	    	}	    	
	    	productResponses.add(productResponseDTOMapperWithImportedStatus(product, imported));
	    	
	    }

	    // Map to DTOs
	    return productResponses;
	}
}
