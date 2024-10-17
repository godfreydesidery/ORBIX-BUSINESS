package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.finance.InvoiceReceivable;

public interface ParkingInvoiceReceivableRepository extends JpaRepository<ParkingInvoiceReceivable, Long> {

	List<ParkingInvoiceReceivable> findAllByParking(Parking parking);

	List<ParkingInvoiceReceivable> findAllByParkingIn(List<Parking> parkings);

	ParkingInvoiceReceivable findByInvoiceReceivable(InvoiceReceivable invoiceReceivable);
	
}
