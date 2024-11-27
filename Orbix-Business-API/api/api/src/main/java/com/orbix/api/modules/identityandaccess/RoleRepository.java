package com.orbix.api.modules.identityandaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, Long> {

/**
 * @author GODFREY
 *
 */
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
