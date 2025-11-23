package com.orbix.api.modules.servicebay;

import javax.servlet.http.HttpServletRequest;

public interface MachineServiceService {
	
	public MachineServiceResponseDTO createMachineService(MachineServiceRequestDTO dto, HttpServletRequest request);
	
	public MachineServiceResponseDTO updateMachineService(MachineServiceRequestDTO dto, HttpServletRequest request);
	
	public boolean confirm(Long id, HttpServletRequest request);
}
