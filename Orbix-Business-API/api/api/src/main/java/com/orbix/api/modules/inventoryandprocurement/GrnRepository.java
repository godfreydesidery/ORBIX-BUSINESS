package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Branch;

public interface GrnRepository extends JpaRepository<Grn, Long> {

	List<Grn> findAllByStatusInAndBranch(List<WorkFlowStatus> statuses, Branch userBranch);

	List<Grn> findAllByStatus(WorkFlowStatus pending);

}
