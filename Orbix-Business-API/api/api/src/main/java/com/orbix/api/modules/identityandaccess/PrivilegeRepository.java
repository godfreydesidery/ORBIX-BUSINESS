/**
 * 
 */
package com.orbix.api.modules.identityandaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


/**
 * @author Administrator
 *
 */
public interface PrivilegeRepository extends JpaRepository <Privilege, Long> {
Optional<Privilege> findByName(String name);
	
	boolean existsByName(String name);
	
	@Query("SELECT p.name FROM Privilege p")
	List<String> getPrivilegeNames();
}
