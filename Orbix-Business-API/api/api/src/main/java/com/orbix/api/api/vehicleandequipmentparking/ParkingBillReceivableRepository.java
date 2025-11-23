package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.finance.BillReceivable;


public interface ParkingBillReceivableRepository extends JpaRepository<ParkingBillReceivable, Long>{

	List<ParkingBillReceivable> findAllByParking(Parking parking);

	List<ParkingBillReceivable> findByParking(Parking parking);
	
	
	@Query("SELECT COUNT(p) FROM ParkingBillReceivable p WHERE p.billReceivable.payStatus IN ('PAID', 'VERIFIED') AND p.billReceivable.paidDateTime BETWEEN :startDate AND :endDate")
    long countByPayStatusAndDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	Optional<ParkingBillReceivable> findByBillReceivable(BillReceivable billReceivable);

	List<ParkingBillReceivable> findByParkingAndDiscountStatus(Parking parking, String string);

	
}
