/**
 * 
 */
package com.orbix.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.domain.Role1;

/**
 * @author GODFREY
 *
 */
public interface RoleRepository1 extends JpaRepository<Role1, Long> {
	Optional<Role1> findByName(String name);
	/**
	 * @param roleName
	 * @return
	 */
	boolean existsByName(String roleName);

	/**
	 * @param object
	 * @return
	 */
	List<Role1> findAllByOwner(Object object);
}
