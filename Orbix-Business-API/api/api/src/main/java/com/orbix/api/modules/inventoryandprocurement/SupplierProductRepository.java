package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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

}
