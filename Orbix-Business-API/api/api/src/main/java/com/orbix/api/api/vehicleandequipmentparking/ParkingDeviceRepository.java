package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkingDeviceRepository extends JpaRepository<ParkingDevice, Long> {

	List<ParkingDevice> findAllByParkingOrderById(Parking parking);

	// The device list is replaced as a whole when an inspection is edited or modified
	@Modifying
	@Query("DELETE FROM ParkingDevice d WHERE d.parking = :parking")
	int deleteAllOfParking(@Param("parking") Parking parking);
}
