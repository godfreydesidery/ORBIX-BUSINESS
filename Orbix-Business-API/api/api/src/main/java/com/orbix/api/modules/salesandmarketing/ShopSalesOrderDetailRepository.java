package com.orbix.api.modules.salesandmarketing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.inventoryandprocurement.Product;

public interface ShopSalesOrderDetailRepository extends JpaRepository<ShopSalesOrderDetail, Long> {

	List<ShopSalesOrderDetail> findAllByShopSalesOrder(ShopSalesOrder shopSalesOrder);

	boolean existsByShopSalesOrderAndProduct(ShopSalesOrder shopSalesOrder, Product product);

}
