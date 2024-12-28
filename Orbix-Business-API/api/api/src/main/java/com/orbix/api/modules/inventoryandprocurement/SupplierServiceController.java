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
import com.orbix.api.modules.adminunits.ShopRepository;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SupplierServiceController implements SupplierService {
	
	private final CompanyRepository companyRepository;
	private final ProductRepository productRepository;
	private final SupplierRepository supplierRepository;
	private final UserService userService;
	private final DayService dayService;
	
	private final ShopRepository shopRepository;
	
	private final ShopProductRepository shopProductRepository;

	@Override
	public List<SupplierResponseDTO> getAllSuppliers(HttpServletRequest request) {
		List<Supplier> suppliers = supplierRepository.findAll();
		List<SupplierResponseDTO> supplierResponses = new ArrayList<>();

		for(Supplier supplier : suppliers) {
			supplierResponses.add(supplierResponseDTOMapper(supplier));					
		}		
		return supplierResponses;
	}

	@Override
	public SupplierResponseDTO get(Long id, HttpServletRequest request) {
		Optional<Supplier> _supplier = supplierRepository.findById(id);
		if(_supplier.isEmpty()) {
			throw new NotFoundException("Supplier not found");
		}		
		return supplierResponseDTOMapper(_supplier.get());
	}

	@Override
	public SupplierResponseDTO createSupplier(SupplierRequestDTO supplierRequest, HttpServletRequest request) {
		Optional<Company> company_ = companyRepository.findById(userService.getUser(request).getCompany().getId());
		if(company_.isEmpty()) {
			throw new InvalidEntryException("Company not found in database");
		}
		
		Supplier supplier = new Supplier();
		//product.setCode(productRequest.getCode());
		supplier.setCode(String.valueOf(Math.random()));
		supplier.setName(supplierRequest.getName());
		supplier.setContactName(supplierRequest.getContactName());
		supplier.setAddress(supplierRequest.getAddress());
		supplier.setPhoneNo(supplierRequest.getPhoneNo());
				
		supplier.setCompany(company_.get());
	
		supplier.setCreatedByUser(userService.getUser(request));
		supplier.setCreatedDateTime(dayService.getTimeStamp());
		
		supplier = supplierRepository.save(supplier);
		/**Create  company code*/
		supplier.setCode("SP/"+ supplier.getId().toString());
		supplier = supplierRepository.save(supplier);
		
		return supplierResponseDTOMapper(supplier);
	}

	@Override
	public SupplierResponseDTO updateSupplier(SupplierRequestDTO supplierRequest, HttpServletRequest request) {
		
		Optional<Supplier> supplier_ = supplierRepository.findById(supplierRequest.getId());
		if(supplier_.isEmpty()) {
			throw new NotFoundException("Supplier not be found in database");
		}		
		if(!validateSupplierData(supplierRequest)) {
			throw new InvalidEntryException("Could not validate supplier data");
		}		
		Supplier supplier = supplier_.get();
		supplier.setName(supplierRequest.getName());
		supplier.setContactName(supplierRequest.getContactName());
		supplier.setAddress(supplierRequest.getAddress());
		supplier.setPhoneNo(supplierRequest.getPhoneNo());
		
		supplier = supplierRepository.save(supplier);		
		return supplierResponseDTOMapper(supplier);
	}

	@Override
	public ApiCustomResponse activateSupplier(SupplierRequestDTO supplier, HttpServletRequest request) {
		Optional<Supplier> supplier_ = supplierRepository.findById(supplier.getId());		
		if(supplier_.isEmpty()) {
			throw new NotFoundException("Supplier not found");
		}		
		if(supplier_.get().isActive() == true) {
			throw new InvalidOperationException("Supplier already active");
		}
		supplier_.get().setActive(true);
		supplierRepository.save(supplier_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Supplier Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateSupplier(SupplierRequestDTO supplier, HttpServletRequest request) {
		Optional<Supplier> supplier_ = supplierRepository.findById(supplier.getId());		
		if(supplier_.isEmpty()) {
			throw new NotFoundException("Supplier not found");
		}		
		if(supplier_.get().isActive() == false) {
			throw new InvalidOperationException("Supplier already inactive");
		}
		supplier_.get().setActive(false);
		supplierRepository.save(supplier_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Supplier Deactivated successifully");
	}

	@Override
	public List<SupplierResponseDTO> getSuppliersByCompany(String supplierName, HttpServletRequest request) {
		// Validate input
	    if (supplierName == null || supplierName.trim().isEmpty()) {
	        throw new IllegalArgumentException("Supplier name cannot be null or empty");
	    }

	    // Fetch company
	    Company company = userService.getUserCompany(request);
	    if (company == null) {
	        throw new NotFoundException("Company not found for the user");
	    }

	    // Fetch products
	    List<Supplier> suppliers = supplierRepository.findAllByCompanyAndNameContainingIgnoreCase(company, supplierName);

	    // Map to DTOs
	    return suppliers.stream()
	        .map(this::supplierResponseDTOMapper)
	        .collect(Collectors.toList());
	}
	
	
	/**
	 * 
	 * @param productRequest
	 * @return
	 */
	boolean validateSupplierData(SupplierRequestDTO supplierRequest) {
		
		return true;
	}
	
	private SupplierResponseDTO supplierResponseDTOMapper(Supplier supplier) {	
		SupplierResponseDTO supplierResponse = new SupplierResponseDTO();
		supplierResponse.setId(supplier.getId().toString());
		supplierResponse.setCode(supplier.getCode());
		supplierResponse.setName(supplier.getName());
		supplierResponse.setContactName(supplier.getContactName());
		supplierResponse.setAddress(supplier.getAddress());
		supplierResponse.setPhoneNo(supplier.getPhoneNo());
		supplierResponse.setCompanyId(supplier.getCompany().getId().toString());
		supplierResponse.setCompanyName(supplier.getCompany().getName());
		if(supplier.isActive()) {
			supplierResponse.setActive("Active");
		}else {
			supplierResponse.setActive("Inactive");
		}
		
		return supplierResponse;
	}
}
