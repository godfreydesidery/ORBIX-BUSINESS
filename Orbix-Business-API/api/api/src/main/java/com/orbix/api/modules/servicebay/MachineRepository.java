package com.orbix.api.modules.servicebay;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.orbix.api.modules.adminunits.Branch;

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

    @Query(
    	    "SELECT m FROM Machine m " +
    	    "WHERE m.branch = :branch " +
    	    "AND m.createdDateTime >= :fromTime " +
    	    "AND m.status = 'CHECKED-IN'"
    	)
        List<Machine> findRecentByBranch(
                @Param("branch") Branch branch,
                @Param("fromTime") LocalDateTime fromTime
        );

	// Search on the columns the machine lists show
	String MACHINE_SEARCH = "(:search = '%%' OR LOWER(m.no) LIKE :search OR LOWER(m.ownerName) LIKE :search OR LOWER(m.ownerPhoneNo) LIKE :search"
			+ " OR LOWER(m.regNo) LIKE :search OR LOWER(m.name) LIKE :search OR LOWER(m.status) LIKE :search)";

	// Same rows as findRecentByWorkshopId, one page at a time
	@Query("SELECT m FROM Machine m WHERE m.workshop.id = :workshopId AND m.createdDateTime >= :fromTime AND " + MACHINE_SEARCH)
	Page<Machine> getRecentPageByWorkshopId(@Param("workshopId") Long workshopId, @Param("fromTime") LocalDateTime fromTime, @Param("search") String search, Pageable pageable);

	// Same rows as findRecentByBranch, one page at a time
	@Query("SELECT m FROM Machine m WHERE m.branch = :branch AND m.createdDateTime >= :fromTime AND m.status = 'CHECKED-IN' AND " + MACHINE_SEARCH)
	Page<Machine> getRecentPageByBranch(@Param("branch") Branch branch, @Param("fromTime") LocalDateTime fromTime, @Param("search") String search, Pageable pageable);
}
