package com.orbix.api.modules.warehouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.exceptions.InvalidEntryException;
import com.orbix.api.exceptions.InvalidOperationException;
import com.orbix.api.exceptions.NotFoundException;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.BranchRepository;
import com.orbix.api.modules.adminunits.Company;
import com.orbix.api.modules.adminunits.CompanyRepository;
import com.orbix.api.modules.adminunits.DayService;
import com.orbix.api.modules.adminunits.Shop;
import com.orbix.api.modules.adminunits.ShopResponseDTO;
import com.orbix.api.modules.identityandaccess.User;
import com.orbix.api.modules.identityandaccess.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WarehouseServiceController implements WarehouseService {
	private final WarehouseRepository warehouseRepository;
	private final CompanyRepository companyRepository;
	private final BranchRepository branchRepository;
	private final UserService userService;
	private final DayService dayService;
	
	@Override
	public List<WarehouseResponseDTO> getAllWarehouses(HttpServletRequest request) {
		List<Warehouse> warehouses = warehouseRepository.findAll();
		List<WarehouseResponseDTO> warehouseResponses = new ArrayList<>();

		for(Warehouse warehouse : warehouses) {
			warehouseResponses.add(warehouseResponseDTOMapper(warehouse));					
		}		
		return warehouseResponses;
	}
	
	@Override
	public List<WarehouseResponseDTO> getAllBranchActiveWarehouses(
			HttpServletRequest request) {
		Branch branch = userService.getUser(request).getBranch();	
		List<Warehouse> warehouses = warehouseRepository.findAllByBranchAndActive(branch, true);
		List<WarehouseResponseDTO> warehouseResponses = new ArrayList<>();

		for(Warehouse warehouse : warehouses) {
			warehouseResponses.add(warehouseResponseDTOMapper(warehouse));					
		}		
		return warehouseResponses;
	}

	@Override
	public WarehouseResponseDTO get(Long id, HttpServletRequest request) {		
	Optional<Warehouse> warehouse_ = warehouseRepository.findById(id);
	if(warehouse_.isEmpty()) {
		throw new NotFoundException("Parking Zone not found");
	}		
	return warehouseResponseDTOMapper(warehouse_.get());	
	}

	@Override
	public WarehouseResponseDTO createWarehouse(WarehouseRequestDTO warehouseRequest, HttpServletRequest request) {
		
		/**Validate data*/		
		if(!validateWarehouseData(warehouseRequest)) {
			throw new InvalidEntryException("Validation failed");
		}
		
		
		
		Optional<Company> _company = companyRepository.findById(userService.getUserCompany(request).getId());
		if(_company.isEmpty()) {
			throw new NotFoundException("Company not found");
		}
		Optional<Branch> _branch = branchRepository.findById(userService.getUserBranch(request).getId());
		if(_branch.isEmpty()) {
			throw new NotFoundException("Branch not found");
		}
		
		/**New parking zone*/
		Warehouse warehouse = new Warehouse();		
		warehouse.setCode(String.valueOf(Math.random()));
		warehouse.setName(warehouseRequest.getName());
		warehouse.setNoOfSections(warehouseRequest.getNoOfSections());
		//warehouse.setCompany(_company.get());
		warehouse.setBranch(_branch.get());
		
		warehouse.setCreatedByUser(userService.getUser(request));
		warehouse.setCreatedDateTime(dayService.getTimeStamp());
		
		warehouse = warehouseRepository.save(warehouse);
		/**Create  parking zone code*/
		warehouse.setCode("PKZ/"+ warehouse.getId().toString());
		warehouse = warehouseRepository.save(warehouse);
		
		return warehouseResponseDTOMapper(warehouse);		
	}

	@Override
	public WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO warehouseRequest, HttpServletRequest request) {
		
		Optional<Warehouse> warehouse_ = warehouseRepository.findById(warehouseRequest.getId());
		if(warehouse_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found in database");
		}
		
		if(!validateWarehouseData(warehouseRequest)) {
			throw new InvalidEntryException("Could not validate data");
		}
		
		Warehouse warehouse = warehouse_.get();
		warehouse.setName(warehouseRequest.getName());
		warehouse.setNoOfSections(warehouseRequest.getNoOfSections());
				
		warehouse = warehouseRepository.save(warehouse);
		
		return warehouseResponseDTOMapper(warehouse);	
		
	}

	@Override
	public ApiCustomResponse activateWarehouse(WarehouseRequestDTO warehouseRequest, HttpServletRequest request) {
		Optional<Warehouse> warehouse_ = warehouseRepository.findById(warehouseRequest.getId());		
		if(warehouse_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found");
		}		
		if(warehouse_.get().isActive() == true) {
			throw new InvalidOperationException("Parking Zone already active");
		}
		warehouse_.get().setActive(true);
		warehouseRepository.save(warehouse_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Parking Zone Activated successifully");
	}

	@Override
	public ApiCustomResponse deactivateWarehouse(WarehouseRequestDTO warehouseRequest, HttpServletRequest request) {
		Optional<Warehouse> warehouse_ = warehouseRepository.findById(warehouseRequest.getId());		
		if(warehouse_.isEmpty()) {
			throw new NotFoundException("Parking Zone not found");
		}		
		if(warehouse_.get().isActive() == false) {
			throw new InvalidOperationException("Parking Zone already inactive");
		}
		warehouse_.get().setActive(false);
		warehouseRepository.save(warehouse_.get());		
		return new ApiCustomResponse(200, "OK", "Success", "Parking Zone dectivated successifully");
	}
	
	
	private WarehouseResponseDTO warehouseResponseDTOMapper(Warehouse warehouse) {
		WarehouseResponseDTO warehouseResponse = new WarehouseResponseDTO();
		
		warehouseResponse.setId(warehouse.getId().toString());
		warehouseResponse.setCode(warehouse.getCode());
		warehouseResponse.setName(warehouse.getName());
		warehouseResponse.setNoOfSections(String.valueOf(warehouse.getNoOfSections()));
		//warehouseResponse.setCompanyName(warehouse.getCompany().getName());
		warehouseResponse.setBranchName(warehouse.getBranch().getName());
	
		if(warehouse.isActive()) {
			warehouseResponse.setActive("Active");
		}else {
			warehouseResponse.setActive("Inactive");
		}
		
		return warehouseResponse;
	}
	
	boolean validateWarehouseData(WarehouseRequestDTO warehouseRequest) {
		
		return true;
	}
	
	@Override
	public List<WarehouseResponseDTO> getBranchAvailableWarehousesByUser(HttpServletRequest request) {
		User user = userService.getUser(request);
		List<Warehouse> warehouses = warehouseRepository.findAllByBranch(user.getBranch());
		
		List<WarehouseResponseDTO> warehouseResponses = new ArrayList<>();

		for(Warehouse warehouse : warehouses) {
			warehouseResponses.add(warehouseResponseDTOMapper(warehouse));					
		}		
		return warehouseResponses;
	}


}
