package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Shop;

public interface ShopSalesOrderRepository extends JpaRepository<ShopSalesOrder, Long> {

	List<ShopSalesOrder> findAllByShopAndStatus(Shop shop, WorkFlowStatus pending);

	// Same rows as findAllByShopAndStatus, one page at a time, searched on the columns the order list shows
	@Query("SELECT o FROM ShopSalesOrder o WHERE o.shop = :shop AND o.status = :status"
			+ " AND (:search = '%%' OR LOWER(o.no) LIKE :search OR LOWER(o.customerName) LIKE :search OR LOWER(o.summary) LIKE :search"
			+ " OR LOWER(str(o.status)) LIKE :search)")
	Page<ShopSalesOrder> getPageByShopAndStatus(@Param("shop") Shop shop, @Param("status") WorkFlowStatus status, @Param("search") String search, Pageable pageable);
}
