package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Shop;

public interface LpoRepository extends JpaRepository<Lpo, Long> {

	List<Lpo> findAllByStatus(WorkFlowStatus pending);

	List<Lpo> findAllByStatusAndBranch(WorkFlowStatus pending, Branch userBranch);

	List<Lpo> findAllByStatusInAndBranch(List<WorkFlowStatus> statuses, Branch userBranch);

	Optional<Lpo> findByNo(String lpoNo);

	List<Lpo> findAllByStatusInAndBranchAndShop(List<WorkFlowStatus> statuses, Branch userBranch, Shop shop);

}
