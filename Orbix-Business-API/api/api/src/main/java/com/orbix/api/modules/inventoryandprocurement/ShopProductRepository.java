package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Shop;



public interface ShopProductRepository extends JpaRepository<ShopProduct, Long> {

	List<ShopProduct> findAllByShop(Object object);

	Optional<ShopProduct> findByIdAndShop(Long id, Shop shop);

	Optional<ShopProduct> findByShopAndProduct(Shop shop, Product product);

	boolean existsByShopAndProduct(Shop shop, Product product);
	
	List<ShopProduct> findAllByShopAndProduct_NameContainingIgnoreCase(Shop shop, String name);

	Optional<ShopProduct> findByProductAndShop(Product product, Shop shop);

	@Query("SELECT COUNT(sp) FROM ShopProduct sp WHERE sp.shop = :shop AND sp.currentStock < sp.minStock AND sp.currentStock > 0")
    long countProductsBelowMinStock(@Param("shop") Shop shop);
	
	@Query("SELECT COUNT(sp) FROM ShopProduct sp WHERE sp.shop = :shop AND (sp.currentStock = 0 OR sp.currentStock < 0)")
    long countProductsOutofStock(@Param("shop") Shop shop);
	
	
    @Query(value = "SELECT sp.* FROM shop_products sp " +
		            "JOIN products p ON sp.product_id = p.id " +
		            "JOIN shops s ON sp.shop_id = s.id " +
		            "WHERE sp.shop_id = :shopId " +
		            "AND sp.current_stock > 0 " +
		            "AND sp.current_stock < sp.min_stock", 
		    nativeQuery = true)
		List<ShopProduct> findProductsWithLowStock(@Param("shopId") Long shopId);
    
 // Find all ShopProducts for a specific shop where currentStock <= 0
    List<ShopProduct> findByShopAndCurrentStockLessThanEqual(Shop shop, double currentStock);
}
