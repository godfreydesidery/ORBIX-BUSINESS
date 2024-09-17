/**
 * 
 */
package com.orbix.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.orbix.api.domain.User;

/**
 * @author GODFREY
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
	
	@Query("SELECT u.nickname FROM User u WHERE u.id =:id")
	String getNickname(Long id);
	/**
	 * @param rollNo
	 * @return
	 */
	Optional<User> findByCode(String c);
	
	@Query("SELECT MAX(u.id) FROM User u")
	Long getLastId();

	/**
	 * @param string
	 * @return
	 */
	boolean existsByUsername(String string);

	/**
	 * @param value
	 * @param value2
	 * @param value3
	 * @return
	 */
	List<User> findAllByFirstNameContainingOrMiddleNameContainingOrLastNameContaining(String value, String value2,
			String value3);

	Optional<User> findByNickname(String nickname);

	List<User> findAllByActive(boolean b);
}
