package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.adminunits.Branch;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {

	List<SupplierProduct> findAllBySupplier(Supplier supplier);

	Optional<SupplierProduct> findByIdAndSupplier(Long id, Supplier supplier);

	Optional<SupplierProduct> findByProductAndSupplier(Product product, Supplier supplier);

	boolean existsBySupplierAndProduct(Supplier supplier, Product product);

	Optional<SupplierProduct> findBySupplierAndProduct(Supplier supplier, Product product);

	List<SupplierProduct> findAllBySupplierAndProduct_NameContainingIgnoreCase(Supplier supplier, String productName);

	List<SupplierProduct> findAllBySupplierAndBranch(Supplier supplier, Branch branch);

	Optional<SupplierProduct> findBySupplierAndProductAndBranch(Supplier supplier, Product product, Branch userBranch);

	// A supplier's products in a branch, searched on the product columns the screens show
	@Query("SELECT x FROM SupplierProduct x LEFT JOIN x.product p WHERE x.supplier = :supplier AND x.branch = :branch AND (:search = '%%' OR LOWER(p.code) LIKE :search OR LOWER(p.name) LIKE :search OR LOWER(p.description) LIKE :search OR LOWER(p.baseUom) LIKE :search)")
	Page<SupplierProduct> getPageBySupplierAndBranch(@Param("supplier") Supplier supplier, @Param("branch") Branch branch, @Param("search") String search, Pageable pageable);
}
