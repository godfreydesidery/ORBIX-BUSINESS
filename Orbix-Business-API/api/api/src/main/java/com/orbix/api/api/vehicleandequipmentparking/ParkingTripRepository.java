package com.orbix.api.api.vehicleandequipmentparking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkingTripRepository extends JpaRepository<ParkingTrip, Long> {

	/** The trip a parking is currently out on, if any */
	Optional<ParkingTrip> findFirstByParkingAndStatusOrderByIdDesc(Parking parking, String status);

	List<ParkingTrip> findAllByParkingOrderByIdDesc(Parking parking);

	// Search on the columns the outside list shows (tr is the trip, p its parking)
	String TRIP_SEARCH = "(:search = '%%' OR LOWER(tr.no) LIKE :search OR LOWER(p.no) LIKE :search OR LOWER(p.chasisNo) LIKE :search"
			+ " OR LOWER(p.registrationNo) LIKE :search OR LOWER(p.ownerFirstName) LIKE :search OR LOWER(p.ownerLastName) LIKE :search"
			+ " OR LOWER(p.cardNo) LIKE :search OR LOWER(tr.driverName) LIKE :search OR LOWER(tr.driverPhoneNo) LIKE :search"
			+ " OR LOWER(tr.driverIdNo) LIKE :search OR LOWER(tr.reason) LIKE :search)";

	@Query(value = "SELECT tr FROM ParkingTrip tr JOIN tr.parking p WHERE tr.status = :status AND " + TRIP_SEARCH,
			countQuery = "SELECT COUNT(tr) FROM ParkingTrip tr JOIN tr.parking p WHERE tr.status = :status AND " + TRIP_SEARCH)
	Page<ParkingTrip> getPageByStatus(@Param("status") String status, @Param("search") String search, Pageable pageable);

	/** Trips sent out in the period, plus trips still out that were sent out earlier */
	@Query("SELECT tr FROM ParkingTrip tr WHERE (tr.sentOutAt BETWEEN :from AND :to) OR (tr.status = 'OUT' AND tr.sentOutAt < :from) ORDER BY tr.sentOutAt")
	List<ParkingTrip> getReport(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
