package com.orbix.api.modules.servicebay;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

public interface MachineServiceBillReceivableService {
	public List<MachineServiceBillReceivableResponseDTO> getAllByMachine(Long machineId, HttpServletRequest request);
	public MachineServiceBillReceivableResponseDTO getMachineServiceBillReceivable(Long id, HttpServletRequest request);
}
