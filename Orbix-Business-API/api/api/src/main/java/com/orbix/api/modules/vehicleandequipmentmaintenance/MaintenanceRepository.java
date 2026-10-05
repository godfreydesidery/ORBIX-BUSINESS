package com.orbix.api.modules.vehicleandequipmentmaintenance;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.orbix.api.api.vehicleandequipmentparking.VehicleEquipment;
import com.orbix.api.modules.identityandaccess.User;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

	List<Maintenance> findAllByStatusIn(List<String> statuses);

	List<Maintenance> findAllByStatusInAndCheckedOutDateTimeBetween(List<String> statuses, LocalDateTime before,
			LocalDateTime now);

	List<Maintenance> findAllByVehicleEquipmentAndStatusIn(VehicleEquipment vehicleEquipment, List<String> statuses);

	@Query("SELECT m.id FROM Maintenance m WHERE m.vehicleEquipment = :vehicleEquipment AND m.status IN :statuses ORDER BY m.id")
	List<Long> getIdsByVehicleEquipmentAndStatusIn(@Param("vehicleEquipment") VehicleEquipment vehicleEquipment, @Param("statuses") List<String> statuses);
	
	@Query("SELECT DISTINCT m FROM Maintenance m JOIN m.maintenanceJobCards mjc JOIN mjc.maintenanceJobCardIssues mjci WHERE mjci.status = 'OPEN' AND m.status IN :statuses")
	List<Maintenance> findAllByStatusInAndOpenMaintenanceJobCardIssues(@Param("statuses") List<String> statuses);
	
	@Query("SELECT DISTINCT m FROM Maintenance m JOIN m.maintenanceJobCards mjc JOIN mjc.maintenanceJobCardIssues mjci WHERE mjci.status = 'OPEN' AND m.status IN :statuses AND mjci.serviceSpecialistUser = :user")
	List<Maintenance> findAllByStatusInAndOpenMaintenanceJobCardIssuesAndServiceSpecialistUser(@Param("statuses") List<String> statuses, User user);
	
	@Query("SELECT DISTINCT m FROM Maintenance m JOIN m.maintenanceJobCards mjc JOIN mjc.maintenanceJobCardIssues mjci WHERE mjci.status = 'CLOSED' AND m.status IN :statuses AND mjci.serviceSpecialistUser = :user AND mjci.closedDateTime >= :closedSince")
	List<Maintenance> findAllByStatusInAndClosedMaintenanceJobCardIssuesAndServiceSpecialistUser(@Param("statuses") List<String> statuses, User user, @Param("closedSince") LocalDateTime closedSince);

	// Search on the columns the maintenance lists show (m is the maintenance, t its vehicle or equipment type)
	String MAINTENANCE_SEARCH = "(:search = '%%' OR LOWER(m.no) LIKE :search OR LOWER(m.chasisNo) LIKE :search"
			+ " OR LOWER(m.ownerFirstName) LIKE :search OR LOWER(m.ownerMiddleName) LIKE :search OR LOWER(m.ownerLastName) LIKE :search"
			+ " OR LOWER(m.ownerPhoneNo) LIKE :search OR LOWER(m.vehicleEquipmentName) LIKE :search OR LOWER(m.status) LIKE :search"
			+ " OR LOWER(t.name) LIKE :search)";

	@Query("SELECT m FROM Maintenance m LEFT JOIN m.vehicleEquipmentType t WHERE m.status IN :statuses AND " + MAINTENANCE_SEARCH)
	Page<Maintenance> getPageByStatusIn(@Param("statuses") List<String> statuses, @Param("search") String search, Pageable pageable);

	// Maintenances with at least one open job card issue (same rows as findAllByStatusInAndOpenMaintenanceJobCardIssues)
	@Query("SELECT m FROM Maintenance m LEFT JOIN m.vehicleEquipmentType t WHERE m.status IN :statuses"
			+ " AND EXISTS (SELECT mjci.id FROM MaintenanceJobCardIssue mjci WHERE mjci.maintenanceJobCard.maintenance = m AND mjci.status = 'OPEN')"
			+ " AND " + MAINTENANCE_SEARCH)
	Page<Maintenance> getPageByStatusInAndOpenMaintenanceJobCardIssues(@Param("statuses") List<String> statuses, @Param("search") String search, Pageable pageable);

	// Maintenances with an open job card issue assigned to the user
	@Query("SELECT m FROM Maintenance m LEFT JOIN m.vehicleEquipmentType t WHERE m.status IN :statuses"
			+ " AND EXISTS (SELECT mjci.id FROM MaintenanceJobCardIssue mjci WHERE mjci.maintenanceJobCard.maintenance = m AND mjci.status = 'OPEN'"
			+ " AND mjci.serviceSpecialistUser = :user) AND " + MAINTENANCE_SEARCH)
	Page<Maintenance> getPageByStatusInAndOpenMaintenanceJobCardIssuesAndServiceSpecialistUser(@Param("statuses") List<String> statuses, @Param("user") User user, @Param("search") String search, Pageable pageable);

	// Maintenances with a job card issue the user closed since the given time
	@Query("SELECT m FROM Maintenance m LEFT JOIN m.vehicleEquipmentType t WHERE m.status IN :statuses"
			+ " AND EXISTS (SELECT mjci.id FROM MaintenanceJobCardIssue mjci WHERE mjci.maintenanceJobCard.maintenance = m AND mjci.status = 'CLOSED'"
			+ " AND mjci.serviceSpecialistUser = :user AND mjci.closedDateTime >= :closedSince) AND " + MAINTENANCE_SEARCH)
	Page<Maintenance> getPageByStatusInAndClosedMaintenanceJobCardIssuesAndServiceSpecialistUser(@Param("statuses") List<String> statuses, @Param("user") User user, @Param("closedSince") LocalDateTime closedSince, @Param("search") String search, Pageable pageable);
}
