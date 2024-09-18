/**
 * 
 */
package com.orbix.api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.domain.Privilege1;

/**
 * @author GODFREY
 *
 */
public interface PrivilegeRepository1 extends JpaRepository<Privilege1, Long> {
	Optional<Privilege1> findByName(String name);
	
	boolean existsByName(String name);
}
