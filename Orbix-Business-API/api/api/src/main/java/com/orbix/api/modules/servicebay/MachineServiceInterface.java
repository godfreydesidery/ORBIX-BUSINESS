package com.orbix.api.modules.servicebay;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import com.orbix.api.api.commons.PageResponseDTO;

public interface MachineServiceInterface {
	
	public MachineResponseDTO get(Long id);
	
	public MachineResponseDTO createMachine(MachineRequestDTO machineRequest, HttpServletRequest request);
	
	public MachineResponseDTO updateMachine(MachineRequestDTO machineRequest, HttpServletRequest request);
	
	public List<MachineResponseDTO> getMachinesByWorkshop(Long workshopId);
	
	public List<MachineResponseDTO> getMachinesByBranch(HttpServletRequest request);

	public PageResponseDTO<MachineResponseDTO> getMachinePageByWorkshop(Long workshopId, int page, int size, String search, HttpServletRequest request);

	public PageResponseDTO<MachineResponseDTO> getMachinePageByBranch(int page, int size, String search, HttpServletRequest request);
}
