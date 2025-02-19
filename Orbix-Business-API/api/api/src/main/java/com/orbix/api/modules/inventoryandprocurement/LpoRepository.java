package com.orbix.api.modules.inventoryandprocurement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Shop;

public interface LpoRepository extends JpaRepository<Lpo, Long> {

	List<Lpo> findAllByStatus(WorkFlowStatus pending);

	List<Lpo> findAllByStatusAndBranch(WorkFlowStatus pending, Branch userBranch);

	List<Lpo> findAllByStatusInAndBranch(List<WorkFlowStatus> statuses, Branch userBranch);

	Optional<Lpo> findByNo(String lpoNo);

	List<Lpo> findAllByStatusInAndBranchAndShop(List<WorkFlowStatus> statuses, Branch userBranch, Shop shop);
	
	
	@Query(value = 
			"SELECT " +
			"    l.approved_date_time AS approvedAt, " +
			"    l.no AS no, " +
			"    p.name AS productName, " +    
			"    COALESCE(ld.qty, 0) AS qty, " +  // Convert NULL to 0
			"    s.name AS supplierName, " +
			"    u.nickname AS approvedBy " +
			"FROM lpos l " +
			"LEFT JOIN lpo_details ld ON ld.lpo_id = l.id " +
			"LEFT JOIN products p ON ld.product_id = p.id " +
			"LEFT JOIN suppliers s ON l.supplier_id = s.id " +
			"LEFT JOIN users u ON l.approved_by_user_id = u.id " +
			"WHERE l.status = 'APPROVED' AND l.approved_date_time BETWEEN :startDateTime AND :endDateTime " +
			"ORDER BY l.approved_date_time DESC",
		    nativeQuery = true)
		List<ILpoProjection> getLpoReportByApprovalDateRange(
		    @Param("startDateTime") LocalDateTime startDateTime,
		    @Param("endDateTime") LocalDateTime endDateTime
		);

}

interface ILpoProjection {
    String getNo();
    String getProductName();
    String getQty();
    String getSupplierName();
    String getApprovedBy();
    String getApprovedAt(); // Corrected type
}
