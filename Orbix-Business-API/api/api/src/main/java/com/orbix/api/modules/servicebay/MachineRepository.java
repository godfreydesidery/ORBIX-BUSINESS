package com.orbix.api.modules.servicebay;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MachineRepository extends JpaRepository<Machine, Long> {

	//List<Machine> findByWorkshopId(Long workshopId);
	
	// 1️⃣ Recent machines by workshop and time, without fetching services
    @Query(
        "SELECT m FROM Machine m " +
        "WHERE m.workshop.id = :workshopId " +
        "AND m.createdDateTime >= :fromTime"
    )
    List<Machine> findRecentByWorkshopId(
            @Param("workshopId") Long workshopId,
            @Param("fromTime") LocalDateTime fromTime
    );

    // 2️⃣ Fetch a single machine by ID, including its services
    @Query(
        "SELECT m FROM Machine m " +
        "LEFT JOIN FETCH m.machineServices ms " +
        "LEFT JOIN FETCH ms.service " +
        "WHERE m.id = :machineId"
    )
    Optional<Machine> findWithServicesById(@Param("machineId") Long machineId);

}
