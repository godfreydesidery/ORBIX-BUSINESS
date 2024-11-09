package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.finance.BillReceivable;

public interface ParkingServiceBillReceivableRepository extends JpaRepository<ParkingServiceBillReceivable, Long> {

	List<ParkingServiceBillReceivable> findAllByParking(Parking parking);

	Optional<ParkingServiceBillReceivable> findByBillReceivable(BillReceivable billReceivable);

}
