/**
 * 
 */
package com.orbix.api.modules.identityandaccess;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Administrator
 *
 */
public interface PrivilegeRepository extends JpaRepository <Privilege, Long> {
Optional<Privilege> findByName(String name);
	
	boolean existsByName(String name);
}
