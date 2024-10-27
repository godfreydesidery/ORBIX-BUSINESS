package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingBillReceivableRepository extends JpaRepository<ParkingBillReceivable, Long>{

	List<ParkingBillReceivable> findAllByParking(Parking parking);

	List<ParkingBillReceivable> findByParking(Parking parking);
	
}
