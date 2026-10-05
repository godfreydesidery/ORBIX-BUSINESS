package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbix.api.modules.finance.BillReceivable;

public interface ParkingServiceBillReceivableRepository extends JpaRepository<ParkingServiceBillReceivable, Long> {

	List<ParkingServiceBillReceivable> findAllByParking(Parking parking);

	Optional<ParkingServiceBillReceivable> findByBillReceivable(BillReceivable billReceivable);

	List<ParkingServiceBillReceivable> findAllByBillReceivableIn(List<BillReceivable> billReceivables);

	@Query("SELECT b FROM ParkingServiceBillReceivable b LEFT JOIN FETCH b.billReceivable WHERE b.parking IN :parkings ORDER BY b.id")
	List<ParkingServiceBillReceivable> findAllByParkingIn(@Param("parkings") List<Parking> parkings);

}
