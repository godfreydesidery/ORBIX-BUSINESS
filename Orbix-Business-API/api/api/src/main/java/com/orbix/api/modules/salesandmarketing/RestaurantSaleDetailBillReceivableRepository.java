package com.orbix.api.modules.salesandmarketing;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.finance.BillReceivable;

public interface RestaurantSaleDetailBillReceivableRepository extends JpaRepository<RestaurantSaleDetailBillReceivable, Long> {

	Optional<RestaurantSaleDetailBillReceivable> findByBillReceivable(BillReceivable billReceivable);

}
