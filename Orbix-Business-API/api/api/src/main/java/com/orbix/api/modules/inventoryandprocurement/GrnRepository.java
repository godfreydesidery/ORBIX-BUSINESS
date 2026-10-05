package com.orbix.api.modules.inventoryandprocurement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Branch;
import com.orbix.api.modules.adminunits.Shop;

public interface GrnRepository extends JpaRepository<Grn, Long> {

	List<Grn> findAllByStatusInAndBranch(List<WorkFlowStatus> statuses, Branch userBranch);

	List<Grn> findAllByStatus(WorkFlowStatus pending);

	List<Grn> findAllByStatusInAndBranchAndShop(List<WorkFlowStatus> statuses, Branch userBranch, Shop shop);

	Optional<Grn> findByLpo(Lpo lpo);
	
	@Query(value = 
		    "SELECT " +
		    "    g.approved_date_time AS approvedAt, " +
		    "    g.no AS no, " +
		    "    p.name AS productName, " +   
		    "    COALESCE(gd.ordered_qty, 0) AS orderedQty, " +
		    "    COALESCE(gd.received_qty, 0) AS receivedQty, " +
		    "    COALESCE(l.supplier_id, NULL) AS supplierId, " +
		    "    COALESCE(s.name, '') AS supplierName, " +
		    "    u.nickname AS approvedBy " +
		    "FROM grns g " +
		    "LEFT JOIN grn_details gd ON gd.grn_id = g.id " +
		    "LEFT JOIN products p ON gd.product_id = p.id " +
		    "LEFT JOIN lpos l ON l.id = g.lpo_id " +  // Join LPO table
		    "LEFT JOIN suppliers s ON COALESCE(l.supplier_id) = s.id " + // Handle nullable supplier
		    "LEFT JOIN users u ON g.approved_by_user_id = u.id " +
		    "WHERE g.status = 'APPROVED' AND g.approved_date_time BETWEEN :startDateTime AND :endDateTime " +
		    "ORDER BY g.approved_date_time DESC",
		    nativeQuery = true)

		List<IGrnProjection> getGrnReportByApprovalDateRange(
		    @Param("startDateTime") LocalDateTime startDateTime,
		    @Param("endDateTime") LocalDateTime endDateTime
		);

	// Search on the columns the GRN lists show (sp is the supplier of the GRN's LPO)
	String GRN_SEARCH = "(:search = '%%' OR LOWER(g.no) LIKE :search OR LOWER(sp.name) LIKE :search OR LOWER(str(g.status)) LIKE :search)";

	@Query("SELECT g FROM Grn g LEFT JOIN g.lpo lp LEFT JOIN lp.supplier sp WHERE g.status IN :statuses AND g.branch = :branch AND " + GRN_SEARCH)
	Page<Grn> getPageByStatusInAndBranch(@Param("statuses") List<WorkFlowStatus> statuses, @Param("branch") Branch branch, @Param("search") String search, Pageable pageable);

	@Query("SELECT g FROM Grn g LEFT JOIN g.lpo lp LEFT JOIN lp.supplier sp WHERE g.status IN :statuses AND g.branch = :branch AND g.shop = :shop AND " + GRN_SEARCH)
	Page<Grn> getPageByStatusInAndBranchAndShop(@Param("statuses") List<WorkFlowStatus> statuses, @Param("branch") Branch branch, @Param("shop") Shop shop, @Param("search") String search, Pageable pageable);
}

interface IGrnProjection {
    String getNo();
    String getProductName();
    String getOrderedQty();
    String getReceivedQty();
    String getSupplierName();
    String getApprovedBy();
    String getApprovedAt(); // Corrected type
}
