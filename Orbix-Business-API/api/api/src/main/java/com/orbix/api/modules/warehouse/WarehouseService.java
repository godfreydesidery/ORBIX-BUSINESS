package com.orbix.api.modules.warehouse;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.orbix.api.api.commons.ApiCustomResponse;
import com.orbix.api.modules.adminunits.ShopResponseDTO;

public interface WarehouseService {
	List<WarehouseResponseDTO> getAllWarehouses(HttpServletRequest request);
	List<WarehouseResponseDTO> getAllBranchActiveWarehouses(HttpServletRequest request);	
	WarehouseResponseDTO get(Long id, HttpServletRequest request);
	WarehouseResponseDTO createWarehouse(WarehouseRequestDTO warehouseRequest, HttpServletRequest request);
	WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO warehouseRequest, HttpServletRequest request);
	ApiCustomResponse activateWarehouse(WarehouseRequestDTO warehouseRequest, HttpServletRequest request);
	ApiCustomResponse deactivateWarehouse(WarehouseRequestDTO warehouseRequest, HttpServletRequest request);
	
	List<WarehouseResponseDTO> getBranchAvailableWarehousesByUser(HttpServletRequest request);
}
