package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkingSignatureRepository extends JpaRepository<ParkingSignature, Long> {

	List<ParkingSignature> findAllByParkingOrderBySignedAtAscIdAsc(Parking parking);

	/** Puts the driver's name, entered by the registrar, under the driver's signature given at inspection */
	@Modifying
	@Query("UPDATE ParkingSignature s SET s.signerName = :name WHERE s.parking = :parking AND s.role = 'DRIVER'")
	int nameDriver(@Param("parking") Parking parking, @Param("name") String name);
}
