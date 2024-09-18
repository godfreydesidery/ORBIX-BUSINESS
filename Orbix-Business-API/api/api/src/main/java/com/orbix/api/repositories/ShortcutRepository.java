/**
 * 
 */
package com.orbix.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.domain.Shortcut;
import com.orbix.api.domain.User1;

/**
 * @author GODFREY
 *
 */
public interface ShortcutRepository extends JpaRepository<Shortcut, Long> {

	/**
	 * @param link
	 * @param user
	 * @return
	 */
	Optional<User1> findByLinkAndUser(String link, User1 user);

	/**
	 * @param user
	 * @return
	 */
	List<Shortcut> findByUser(User1 user);

	/**
	 * @param name
	 * @param user
	 * @return
	 */
	Optional<Shortcut> findByNameAndUser(String name, User1 user);

	

}
