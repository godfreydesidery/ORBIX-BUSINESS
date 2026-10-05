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

	Optional<ParkingBillReceivable> findFirstByParkingOrderByIdDesc(Parking parking);
	
	
	@Query("SELECT COUNT(p) FROM ParkingBillReceivable p WHERE p.billReceivable.payStatus IN ('PAID', 'VERIFIED') AND p.billReceivable.paidDateTime BETWEEN :startDate AND :endDate")
    long countByPayStatusAndDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	Optional<ParkingBillReceivable> findByBillReceivable(BillReceivable billReceivable);

	List<ParkingBillReceivable> findAllByBillReceivableIn(List<BillReceivable> billReceivables);

	List<ParkingBillReceivable> findByParkingAndDiscountStatus(Parking parking, String string);

	@Query("SELECT b FROM ParkingBillReceivable b JOIN b.parking p WHERE b.discountStatus = :discountStatus AND p.status IN :statuses ORDER BY p.id, b.id")
	List<ParkingBillReceivable> findAllByDiscountStatusAndParking_StatusIn(@Param("discountStatus") String discountStatus, @Param("statuses") List<String> statuses);

	@Query("SELECT b.id FROM ParkingBillReceivable b WHERE b.parking = :parking ORDER BY b.id")
	List<Long> getIdsByParking(@Param("parking") Parking parking);

	@Query("SELECT b FROM ParkingBillReceivable b LEFT JOIN FETCH b.billReceivable WHERE b.parking IN :parkings ORDER BY b.id")
	List<ParkingBillReceivable> findAllByParkingIn(@Param("parkings") List<Parking> parkings);


}
