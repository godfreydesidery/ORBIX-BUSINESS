package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.orbix.api.modules.adminunits.Shop;



public interface ShopProductRepository extends JpaRepository<ShopProduct, Long> {

	@Query("SELECT sp.product.id FROM ShopProduct sp WHERE sp.shop = :shop")
	List<Long> getProductIdsByShop(@Param("shop") Shop shop);

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

	// Stock filter of the stock screens: '' all, 'out' (none left), 'below_min' (below the minimum stock),
	// 'low' (some left but below the minimum, as get_under_stock_by_shop)
	String STOCK_FILTER = "(:stock = '' OR (:stock = 'out' AND x.currentStock <= 0) OR (:stock = 'below_min' AND x.currentStock < x.minStock)"
			+ " OR (:stock = 'low' AND x.currentStock > 0 AND x.currentStock < x.minStock))";

	// A shop's products, filtered by stock and searched on the product columns the stock screens show
	@Query("SELECT x FROM ShopProduct x LEFT JOIN x.product p WHERE x.shop = :shop AND " + STOCK_FILTER + " AND (:search = '%%' OR LOWER(p.code) LIKE :search OR LOWER(p.name) LIKE :search OR LOWER(p.description) LIKE :search OR LOWER(p.baseUom) LIKE :search)")
	Page<ShopProduct> getStockPageByShop(@Param("shop") Shop shop, @Param("stock") String stock, @Param("search") String search, Pageable pageable);
}
