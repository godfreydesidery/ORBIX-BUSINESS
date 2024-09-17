/**
 * 
 */
package com.orbix.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.domain.Role;

/**
 * @author GODFREY
 *
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(String name);
	/**
	 * @param roleName
	 * @return
	 */
	boolean existsByName(String roleName);

	/**
	 * @param object
	 * @return
	 */
	List<Role> findAllByOwner(Object object);
}
