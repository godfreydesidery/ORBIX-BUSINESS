package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Shop;

public interface GrnRepository extends JpaRepository<Grn, Long> {

	List<Grn> findAllByStatusInAndBranch(List<WorkFlowStatus> statuses, Branch userBranch);

	List<Grn> findAllByStatus(WorkFlowStatus pending);

	List<Grn> findAllByStatusInAndBranchAndShop(List<WorkFlowStatus> statuses, Branch userBranch, Shop shop);

}
