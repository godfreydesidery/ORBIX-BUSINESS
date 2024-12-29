package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LpoDetailRepository extends JpaRepository<LpoDetail, Long> {

	List<LpoDetail> findAllByLpo(Lpo lpo);

	boolean existsByLpoAndProduct(Lpo lpo, Product product);

}
