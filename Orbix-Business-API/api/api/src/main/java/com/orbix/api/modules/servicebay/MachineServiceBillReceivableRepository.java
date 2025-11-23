package com.orbix.api.modules.servicebay;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.finance.BillReceivable;

public interface MachineServiceBillReceivableRepository extends JpaRepository<MachineServiceBillReceivable, Long> {

	List<MachineServiceBillReceivable> findAllByMachineServiceIn(List<MachineService> machineServices);

	Optional<MachineServiceBillReceivable> findByBillReceivable(BillReceivable billReceivable);

}
