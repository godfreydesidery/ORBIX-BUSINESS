package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Restaurant;

public interface RestaurantProductRepository extends JpaRepository<RestaurantProduct, Long> {
	
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
}
