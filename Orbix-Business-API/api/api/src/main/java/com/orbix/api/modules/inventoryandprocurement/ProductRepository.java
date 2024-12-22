package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.adminunits.Company;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByCompanyAndNameContainingIgnoreCase(Company company, String productName);

	List<Product> findAllByCompanyAndSellable(Company company, boolean b);

}
