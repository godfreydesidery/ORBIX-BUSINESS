package com.orbix.api.modules.servicebay;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface MachineServiceInterface {
	
	public MachineResponseDTO get(Long id);
	
	public MachineResponseDTO createMachine(MachineRequestDTO machineRequest, HttpServletRequest request);
	
	public MachineResponseDTO updateMachine(MachineRequestDTO machineRequest, HttpServletRequest request);
	
	public List<MachineResponseDTO> getMachinesByWorkshop(Long workshopId);
	
	public List<MachineResponseDTO> getMachinesByBranch(HttpServletRequest request);
}
