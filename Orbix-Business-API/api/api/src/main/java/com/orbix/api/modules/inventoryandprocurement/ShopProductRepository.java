package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Shop;



public interface ShopProductRepository extends JpaRepository<ShopProduct, Long> {

	List<ShopProduct> findAllByShop(Object object);

	Optional<ShopProduct> findByIdAndShop(Long id, Shop shop);

	Optional<ShopProduct> findByShopAndProduct(Shop shop, Product product);

	boolean existsByShopAndProduct(Shop shop, Product product);
	
	List<ShopProduct> findAllByShopAndProduct_NameContainingIgnoreCase(Shop shop, String name);

	
}
