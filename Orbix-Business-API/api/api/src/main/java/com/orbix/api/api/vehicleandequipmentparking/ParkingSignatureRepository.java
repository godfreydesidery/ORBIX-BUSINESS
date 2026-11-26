package com.orbix.api.api.vehicleandequipmentparking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSignatureRepository extends JpaRepository<ParkingSignature, Long> {

	List<ParkingSignature> findAllByParkingOrderBySignedAtAscIdAsc(Parking parking);
}
