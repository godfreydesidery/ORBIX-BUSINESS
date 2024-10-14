package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingInvoiceReceivableRepository extends JpaRepository<ParkingInvoiceReceivable, Long> {

	List<ParkingInvoiceReceivable> findAllByParking(Parking parking);
	
}
