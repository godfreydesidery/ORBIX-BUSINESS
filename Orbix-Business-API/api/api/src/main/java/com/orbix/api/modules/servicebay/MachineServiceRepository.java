package com.orbix.api.modules.servicebay;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineServiceRepository extends JpaRepository<MachineService, Long> {

	List<MachineService> findAllByMachine(Machine machine);

	List<MachineService> findAllByMachineAndStatus(Machine machine, String string);

}
