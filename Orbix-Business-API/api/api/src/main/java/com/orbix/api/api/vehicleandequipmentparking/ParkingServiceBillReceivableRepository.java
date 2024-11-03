package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingServiceBillReceivableRepository extends JpaRepository<ParkingServiceBillReceivable, Long> {

	List<ParkingServiceBillReceivable> findAllByParking(Parking parking);

}
