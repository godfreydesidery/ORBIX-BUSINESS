package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.api.commons.WorkFlowStatus;
import com.orbix.api.modules.adminunits.Shop;

public interface ShopSalesOrderRepository extends JpaRepository<ShopSalesOrder, Long> {

	List<ShopSalesOrder> findAllByShopAndStatus(Shop shop, WorkFlowStatus pending);

}
