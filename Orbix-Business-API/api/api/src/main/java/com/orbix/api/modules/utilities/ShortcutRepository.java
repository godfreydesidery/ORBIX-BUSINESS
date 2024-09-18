/**
 * 
 */
package com.orbix.api.modules.utilities;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbix.api.modules.identityandaccess.User;

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
	Optional<User> findByLinkAndUser(String link, User user);

	/**
	 * @param user
	 * @return
	 */
	List<Shortcut> findByUser(User user);

	/**
	 * @param name
	 * @param user
	 * @return
	 */
	Optional<Shortcut> findByNameAndUser(String name, User user);

	

}
