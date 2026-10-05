package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantProductRepository extends JpaRepository<RestaurantProduct, Long> {

	@Query("SELECT rp.product.id FROM RestaurantProduct rp WHERE rp.restaurant = :restaurant")
	List<Long> getProductIdsByRestaurant(@Param("restaurant") Restaurant restaurant);
	
	List<RestaurantProduct> findAllByRestaurant(Object object);

	Optional<RestaurantProduct> findByIdAndRestaurant(Long id, Restaurant restaurant);

	Optional<RestaurantProduct> findByRestaurantAndProduct(Restaurant restaurant, Product product);

	boolean existsByRestaurantAndProduct(Restaurant restaurant, Product product);
	
	List<RestaurantProduct> findAllByRestaurantAndProduct_NameContainingIgnoreCase(Restaurant restaurant, String name);

	Optional<RestaurantProduct> findByProductAndRestaurant(Product product, Restaurant restaurant);

	@Query("SELECT COUNT(sp) FROM RestaurantProduct sp WHERE sp.restaurant = :restaurant AND sp.currentStock < sp.minStock AND sp.currentStock > 0")
    long countProductsBelowMinStock(@Param("restaurant") Restaurant restaurant);
	
	@Query("SELECT COUNT(sp) FROM RestaurantProduct sp WHERE sp.restaurant = :restaurant AND (sp.currentStock = 0 OR sp.currentStock < 0)")
    long countProductsOutofStock(@Param("restaurant") Restaurant restaurant);
	
	
    @Query(value = "SELECT sp.* FROM restaurant_products sp " +
		            "JOIN products p ON sp.product_id = p.id " +
		            "JOIN restaurants s ON sp.restaurant_id = s.id " +
		            "WHERE sp.restaurant_id = :restaurantId " +
		            "AND sp.current_stock > 0 " +
		            "AND sp.current_stock < sp.min_stock", 
		    nativeQuery = true)
		List<RestaurantProduct> findProductsWithLowStock(@Param("restaurantId") Long restaurantId);
    
 // Find all RestaurantProducts for a specific restaurant where currentStock <= 0
    List<RestaurantProduct> findByRestaurantAndCurrentStockLessThanEqual(Restaurant restaurant, double currentStock);

	// Stock filter of the stock screens: '' all, 'out' (none left), 'below_min' (below the minimum stock),
	// 'low' (some left but below the minimum, as get_under_stock_by_shop)
	String STOCK_FILTER = "(:stock = '' OR (:stock = 'out' AND x.currentStock <= 0) OR (:stock = 'below_min' AND x.currentStock < x.minStock)"
			+ " OR (:stock = 'low' AND x.currentStock > 0 AND x.currentStock < x.minStock))";

	// A restaurant's products, filtered by stock and searched on the product columns the stock screens show
	@Query("SELECT x FROM RestaurantProduct x LEFT JOIN x.product p WHERE x.restaurant = :restaurant AND " + STOCK_FILTER + " AND (:search = '%%' OR LOWER(p.code) LIKE :search OR LOWER(p.name) LIKE :search OR LOWER(p.description) LIKE :search OR LOWER(p.baseUom) LIKE :search)")
	Page<RestaurantProduct> getStockPageByRestaurant(@Param("restaurant") Restaurant restaurant, @Param("stock") String stock, @Param("search") String search, Pageable pageable);
}
