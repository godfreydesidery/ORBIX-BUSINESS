package com.orbix.api.modules.inventoryandprocurement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GrnDetailRepository extends JpaRepository<GrnDetail, Long> {

	List<GrnDetail> findAllByGrn(Grn grn);

	boolean existsByGrnAndProduct(Grn grn, Product product);

}
